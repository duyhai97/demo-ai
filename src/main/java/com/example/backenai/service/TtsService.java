package com.example.backenai.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class TtsService {

    public String generate(String text) throws Exception {

        Files.createDirectories(Paths.get("storage/audio"));
        Files.createDirectories(Paths.get("storage/tmp"));

        String output =
                "storage/audio/"
                        + UUID.randomUUID()
                        + ".mp3";

        text = text
                .replace("\r", " ")
                .replace("\n", " ")
                .replaceAll("\\s+", " ")
                .trim();

        Path txtFile =
                Paths.get(
                        "storage/tmp/"
                                + UUID.randomUUID()
                                + ".txt"
                );

        Files.writeString(
                txtFile,
                text,
                StandardCharsets.UTF_8
        );

        String edgeTts;

        if (System.getProperty("os.name")
                .toLowerCase()
                .contains("mac")) {

            edgeTts =
                    "/Users/local/Library/Python/3.9/bin/edge-tts";

        } else {

            edgeTts = "edge-tts";
        }

        ProcessBuilder pb =
                new ProcessBuilder(
                        edgeTts,
                        "--voice",
                        "vi-VN-HoaiMyNeural",
                        "--file",
                        txtFile.toString(),
                        "--write-media",
                        output
                );

        pb.redirectErrorStream(true);

        System.out.println("EDGE CMD = " + pb.command());
        System.out.println("TEXT = " + text);

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

                System.out.println("[EDGE] " + line);
            }
        }

        int code = process.waitFor();

        System.out.println("EDGE EXIT = " + code);

        Path path = Paths.get(output);
        boolean exists =
                Files.exists(path);
        long size = exists
                ? Files.size(Paths.get(output))
                : 0;
        System.out.println("VOICE SIZE = " + size);

        if (!exists || size < 2048) {
            throw new RuntimeException("Voice file invalid");
        }

        if (code != 0) {
            System.out.println("Edge TTS warning: exit code != 0 but audio file exists, continue.");
        }

        return output;
    }
}