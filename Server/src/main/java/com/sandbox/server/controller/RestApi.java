package com.sandbox.server.controller;

import com.sandbox.server.aspect.ThreadLog;
import com.sandbox.server.client.PostsWebClient;
import com.sandbox.server.dto.PostDto;
import com.sandbox.server.exception.BasicException;
import com.sandbox.server.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest")
@RequiredArgsConstructor
public class RestApi {

    private final FileService fileService;
    private final PostsWebClient postsWebClient;

    @GetMapping(value = "/cookies", produces = "application/json")
    public ResponseEntity<String> cookies() {

        val cookies = """
            [
                {"name": "Cookie", "value": 1},
                {"name": "Cookie", "value": 2},
                {"name": "Cookie", "value": 3},
                {"name": "Cookie", "value": 4},
                {"name": "Cookie", "value": 5}
            ]
               \s""";

        return ResponseEntity.ok(cookies);
    }

    @PostMapping("/file/{content}")
    public ResponseEntity<Void> file(@PathVariable("content") String content) {
        fileService.save(content);
        return ResponseEntity.ok().build();
    }

    @ThreadLog
    @GetMapping("/posts")
    public ResponseEntity<List<PostDto>> posts() {
        val posts = postsWebClient.getAllPosts();
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/passphrase")
    public ResponseEntity<String> passphrase() {
        val pass = postsWebClient.getPassphrase();
        return ResponseEntity.ok(pass);
    }

    @GetMapping("/risk")
    public ResponseEntity<Void> risk() {
        throw new BasicException("Exception here!!");
    }
}
