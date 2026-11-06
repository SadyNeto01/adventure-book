package com.adventurebook.book;

import com.adventurebook.book.dto.BookSummaryDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookService {

    private static final List<String> SORTABLE = List.of("id", "title", "author", "difficulty");

    private final BookRepository books;

    @Transactional(readOnly = true)
    public Page<BookSummaryDTO> list(Pageable pageable) {
        log.info("Listing books page={} size={}", pageable.getPageNumber(), pageable.getPageSize());
        for (Sort.Order order : pageable.getSort()) {
            if (!SORTABLE.contains(order.getProperty())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Cannot sort by '" + order.getProperty() + "'. Allowed: " + SORTABLE);
            }
        }
        Page<BookSummaryDTO> page = books.findAll(pageable).map(BookSummaryDTO::from);
        log.debug("Listed {} of {} books, sort={}", page.getNumberOfElements(), page.getTotalElements(), pageable.getSort());
        return page;
    }
}
