package com.adventurebook.book;

import com.adventurebook.book.dto.BookDetailsDTO;
import com.adventurebook.book.dto.BookFilterDTO;
import com.adventurebook.book.dto.BookSummaryDTO;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping
    public PagedModel<BookSummaryDTO> list(@ParameterObject BookFilterDTO filter,
                                           @ParameterObject @PageableDefault(sort = "title") Pageable pageable) {
        return new PagedModel<>(bookService.list(filter, pageable));
    }

    @GetMapping("/{id}")
    public BookDetailsDTO details(@PathVariable Long id) {
        return bookService.details(id);
    }
}
