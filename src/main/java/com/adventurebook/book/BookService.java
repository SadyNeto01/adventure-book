package com.adventurebook.book;

import com.adventurebook.book.dto.BookDetailsDTO;
import com.adventurebook.book.dto.ConsequenceRequestDTO;
import com.adventurebook.book.dto.CreateBookRequestDTO;
import com.adventurebook.book.dto.SectionRequestDTO;
import com.adventurebook.book.dto.BookFilterDTO;
import com.adventurebook.book.dto.BookSummaryDTO;
import com.adventurebook.book.entity.Book;
import com.adventurebook.section.entity.Consequence;
import com.adventurebook.section.entity.Option;
import com.adventurebook.section.entity.Section;
import com.adventurebook.section.entity.SectionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookService {

    private static final List<String> SORTABLE = List.of("id", "title", "author", "difficulty");

    private final BookRepository books;
    private final BookValidator validator;

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
    public BookDetailsDTO create(CreateBookRequestDTO request) {
        log.info("Creating book title={} author={} sections={}", request.title(), request.author(), request.sections().size());
        Book book = toEntity(request);
        List<String> errors = validator.validate(book);
        if (!errors.isEmpty()) {
            log.info("Book rejected title={} errors={}", book.getTitle(), errors);
            ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Book is invalid");
            problem.setProperty("errors", errors);
            throw new ErrorResponseException(HttpStatus.BAD_REQUEST, problem, null);
        }
        if (books.existsByTitleAndAuthor(book.getTitle(), book.getAuthor())) {
            log.info("Book rejected title={}: duplicate title/author", book.getTitle());
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Book '" + book.getTitle() + "' by " + book.getAuthor() + " already exists");
        }
        books.save(book);
        log.info("Book created id={}", book.getId());
        return toDetails(book);
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

    private static Book toEntity(CreateBookRequestDTO request) {
        Book book = new Book(request.title().trim(), request.author().trim(), request.difficulty());
        if (request.categories() != null) {
            request.categories().forEach(category -> book.getCategories().add(normalizeCategory(category)));
        }
        for (SectionRequestDTO section : request.sections()) {
            List<Option> options = section.options() == null ? null : section.options().stream()
                    .map(option -> new Option(option.description(), option.gotoId(), toConsequence(option.consequence())))
                    .toList();
            book.addSection(new Section(section.id(), section.text(), section.type(), options));
        }
        return book;
    }

    private static Consequence toConsequence(ConsequenceRequestDTO consequence) {
        return consequence == null ? null : new Consequence(consequence.type(), consequence.value(), consequence.text());
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
