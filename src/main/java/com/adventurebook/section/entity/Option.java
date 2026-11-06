package com.adventurebook.section.entity;

/** Stored as JSON in {@code section.options}. {@code consequence} is null when the choice has none. */
public record Option(String description, int gotoId, Consequence consequence) {
}
