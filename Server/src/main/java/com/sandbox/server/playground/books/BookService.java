package com.sandbox.server.playground.books;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class BookService {

    private final BookClient bookClient;

    public Map<String, Long> getBookRatings() {
        return bookClient.books().stream()
                .collect(
                    Collectors.groupingBy(
                            Book::title,
                            Collectors.collectingAndThen(
                                    Collectors.averagingDouble(Book::rating),
                                    Math::round
                            )
                ));
    }

}
