package com.adventurebook.book.dto;

import com.adventurebook.book.entity.Book;
import com.adventurebook.book.entity.Difficulty;

import java.util.SortedSet;
import java.util.TreeSet;

public record BookDetailsDTO(Long id, String title, String author, Difficulty difficulty,
                             SortedSet<String> categories, int beginSectionId) {

    public static BookDetailsDTO from(Book book, int beginSectionId) {
        return new BookDetailsDTO(book.getId(), book.getTitle(), book.getAuthor(), book.getDifficulty(),
                new TreeSet<>(book.getCategories()), beginSectionId);
    }
}
