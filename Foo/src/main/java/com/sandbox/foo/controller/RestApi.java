package com.sandbox.foo.controller;

import com.sandbox.foo.service.SecretService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class RestApi {

    private final SecretService secretService;

    @GetMapping("/secret")
    public ResponseEntity<String> secret() {
        return ResponseEntity.ok(secretService.prepareSecret());
    }

}
