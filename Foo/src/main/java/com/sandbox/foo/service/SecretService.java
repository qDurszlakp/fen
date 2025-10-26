package com.sandbox.foo.service;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Slf4j
@Service
public class SecretService {

    @Value("${secret.delayInSecondsRange:0_0}")
    private String delayInSecondsRange;

    public String prepareSecret() {
        val characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        val random = new SecureRandom();

        val sb = new StringBuilder(20);
        for (int i = 0; i < 20; i++) {
            val index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }

        applyRandomDelay(random);

        return sb.toString();
    }

    private void applyRandomDelay(SecureRandom random) {
        try {
            val parts = delayInSecondsRange.trim().split("_");
            if (parts.length != 2) {
                log.warn("Invalid delayInSecondsRange format: '{}'. Expected format 'min_max'.", delayInSecondsRange);
                return;
            }

            val min = Long.parseLong(parts[0]);
            val max = Long.parseLong(parts[1]);

            if (min < 0 || max < min) {
                log.warn("Invalid delay range: {} - {}", min, max);
                return;
            }

            val delaySeconds = min + (long) (random.nextDouble() * (max - min));
            Thread.sleep(delaySeconds * 1000);

        } catch (Exception e) {
            log.warn("Error while applying delay: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

}
