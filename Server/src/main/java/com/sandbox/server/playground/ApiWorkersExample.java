package com.sandbox.server.playground;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class ApiWorkersExample {

    public static void main(String[] args) {
        var client = HttpClient.newHttpClient();
        var limiter = new Semaphore(5);

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 1; i <= 5; i++) {
                int id = i;
                executor.submit(() -> {
                    limiter.acquire();
                    try {
                        var req = HttpRequest.newBuilder(URI.create("https://dummyjson.com/quotes/" + id)).build();
                        var res = client.send(req, HttpResponse.BodyHandlers.ofString());
                        System.out.println(Thread.currentThread() + " -> " + res.body());
                    } finally {
                        limiter.release();
                    }
                    return null;
                });
            }
        }
    }
}
