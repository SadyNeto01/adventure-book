package com.adventurebook.book.dto;

import com.adventurebook.book.entity.Difficulty;

import java.util.List;

/** Optional query filters for GET /books, combined with AND; a book must have every given category. */
public record BookFilterDTO(String title, String author, List<String> category, Difficulty difficulty) {
}
