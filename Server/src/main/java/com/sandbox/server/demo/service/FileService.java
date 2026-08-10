package com.sandbox.server.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;

@Slf4j
@Service
public class FileService {

    @Value("${files.baseDir}")
    private String filePath;

    public void save(String content) {

        String fileName = filePath + "_" + System.currentTimeMillis() + "_";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
            log.info("Attempt of writing to the file: [{}]", fileName);
            writer.append(content);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
