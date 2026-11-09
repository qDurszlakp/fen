package com.sandbox.server.playground.books;

public record Book(
    int id,
    String title,
    String author,
    String genre,
    int year,
    float rating
) {}
