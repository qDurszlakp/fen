package com.sandbox.server.playground.books;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
public class BooksController {

    private final BookService bookService;

    @GetMapping("/books")
    public Map<String, Long> books() {
        return bookService.getBookRatings();
    }

}
