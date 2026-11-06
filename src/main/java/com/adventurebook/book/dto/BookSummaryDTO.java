package com.adventurebook.book.dto;

import com.adventurebook.book.entity.Book;
import com.adventurebook.book.entity.Difficulty;

import java.util.SortedSet;
import java.util.TreeSet;

public record BookSummaryDTO(Long id, String title, String author, Difficulty difficulty, SortedSet<String> categories) {

    public static BookSummaryDTO from(Book book) {
        return new BookSummaryDTO(book.getId(), book.getTitle(), book.getAuthor(), book.getDifficulty(),
                new TreeSet<>(book.getCategories()));
    }
}
