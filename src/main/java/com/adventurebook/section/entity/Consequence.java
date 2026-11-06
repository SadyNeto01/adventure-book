package com.adventurebook.section.entity;

/** Stored as JSON inside {@link Option}. */
public record Consequence(ConsequenceType type, int value, String text) {
}
