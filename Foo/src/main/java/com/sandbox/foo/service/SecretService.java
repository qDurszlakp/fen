package com.sandbox.foo.service;

import lombok.val;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class SecretService {

    public String prepareSecret() {
        val characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        val random = new SecureRandom();

        val sb = new StringBuilder(20);
        for (int i = 0; i < 20; i++) {
            val index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }

        return sb.toString();
    }

}
