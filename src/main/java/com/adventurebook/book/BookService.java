package com.adventurebook.book;

import com.adventurebook.book.dto.BookDetailsDTO;
import com.adventurebook.book.dto.BookFilterDTO;
import com.adventurebook.book.dto.BookSummaryDTO;
import com.adventurebook.book.entity.Book;
import com.adventurebook.section.entity.SectionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookService {

    private static final List<String> SORTABLE = List.of("id", "title", "author", "difficulty");

    private final BookRepository books;

    @Transactional(readOnly = true)
    public Page<BookSummaryDTO> list(BookFilterDTO filter, Pageable pageable) {
        log.info("Listing books page={} size={}", pageable.getPageNumber(), pageable.getPageSize());
        log.debug("Book filters {}", filter);
        for (Sort.Order order : pageable.getSort()) {
            if (!SORTABLE.contains(order.getProperty())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Cannot sort by '" + order.getProperty() + "'. Allowed: " + SORTABLE);
            }
        }
        Page<BookSummaryDTO> page = books.findAll(toSpecification(filter), pageable).map(BookSummaryDTO::from);
        log.debug("Listed {} of {} books, sort={}", page.getNumberOfElements(), page.getTotalElements(), pageable.getSort());
        return page;
    }

    @Transactional(readOnly = true)
    public BookDetailsDTO details(Long id) {
        log.info("Get book details id={}", id);
        return toDetails(findBook(id));
    }

    @Transactional
    public BookDetailsDTO addCategory(Long id, String category) {
        Book book = findBook(id);
        String normalized = normalizeCategory(category);
        if (!book.getCategories().add(normalized)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Book " + id + " already has category " + normalized);
        }
        log.info("Category {} added to book {}", normalized, id);
        return toDetails(book);
    }

    @Transactional
    public void removeCategory(Long id, String category) {
        Book book = findBook(id);
        String normalized = normalizeCategory(category);
        if (!book.getCategories().remove(normalized)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Book " + id + " has no category " + normalized);
        }
        log.info("Category {} removed from book {}", normalized, id);
    }

    private Book findBook(Long id) {
        return books.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book " + id + " not found"));
    }

    private static BookDetailsDTO toDetails(Book book) {
        int beginSectionId = book.getSections().stream()
                .filter(s -> s.getType() == SectionType.BEGIN)
                .findFirst().orElseThrow()
                .getId().sectionId();
        log.debug("Book {} begins at section {}", book.getId(), beginSectionId);
        return BookDetailsDTO.from(book, beginSectionId);
    }

    private static String normalizeCategory(String category) {
        return category.trim().toUpperCase(Locale.ROOT);
    }

    private static Specification<Book> toSpecification(BookFilterDTO filter) {
        Specification<Book> spec = Specification.unrestricted();
        if (StringUtils.hasText(filter.title())) {
            spec = spec.and(contains("title", filter.title()));
        }
        if (StringUtils.hasText(filter.author())) {
            spec = spec.and(contains("author", filter.author()));
        }
        if (filter.category() != null) {
            List<String> categories = filter.category().stream()
                    .filter(StringUtils::hasText).map(BookService::normalizeCategory).distinct().toList();
            // one join per category, so the book must have all of them
            for (String category : categories) {
                spec = spec.and((root, query, cb) -> cb.equal(root.join("categories"), category));
            }
        }
        if (filter.difficulty() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("difficulty"), filter.difficulty()));
        }
        return spec;
    }

    private static Specification<Book> contains(String field, String text) {
        String escaped = text.trim().toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return (root, query, cb) -> cb.like(cb.lower(root.get(field)), "%" + escaped + "%", '\\');
    }
}
