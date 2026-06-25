package com.example.backenai.service;

import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class SubtitleService {

    public String create(
            String script
    ) throws Exception {

        String file =
                "storage/subtitles/"
                        + UUID.randomUUID()
                        + ".srt";

        Files.createDirectories(
                Paths.get(
                        "storage/subtitles"
                )
        );

        String srt = """
                1
                00:00:00,000 --> 00:00:05,000
                %s
                """
                .formatted(script);

        Files.writeString(
                Path.of(file),
                srt
        );

        return file;
    }
}