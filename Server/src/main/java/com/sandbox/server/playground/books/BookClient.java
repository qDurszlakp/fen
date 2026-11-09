package com.sandbox.server.playground.books;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BookClient {

    @Value("${books.basicUrl}")
    private String basicUrl;

    private final  WebClient webClient;

    public List<Book> books() {

        return webClient.get()
                .uri(basicUrl)
                .retrieve()
                .bodyToFlux(Book.class)
                .collectList()
                .block(Duration.ofSeconds(5));
    }

}
