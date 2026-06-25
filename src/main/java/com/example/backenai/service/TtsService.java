package com.example.backenai.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class TtsService {

    public String generate(String text) throws Exception {

        Files.createDirectories(
                Paths.get("storage/audio")
        );

        String output =
                "storage/audio/"
                        + UUID.randomUUID()
                        + ".mp3";

        ProcessBuilder pb =
                new ProcessBuilder(
                        "edge-tts",
                        "--voice",
                        "vi-VN-HoaiMyNeural",
                        "--text",
                        text,
                        "--write-media",
                        output
                );

        pb.redirectErrorStream(true);

        Process process = pb.start();

        try (
                BufferedReader br =
                        new BufferedReader(
                                new InputStreamReader(
                                        process.getInputStream()
                                )
                        )
        ) {

            String line;

            while ((line = br.readLine()) != null) {

                System.out.println(
                        "[EDGE-TTS] " + line
                );
            }
        }

        int code = process.waitFor();

        if (code != 0) {

            throw new RuntimeException(
                    "Edge TTS failed"
            );
        }

        if (!Files.exists(Paths.get(output))) {

            throw new RuntimeException(
                    "Voice file not generated"
            );
        }

        System.out.println(
                "VOICE GENERATED = " + output
        );

        return output;
    }
}