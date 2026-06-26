package com.example.backenai.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class TtsService {

    private static final long MIN_AUDIO_SIZE = 2048;
    private static final int MAX_RETRY_PER_VOICE = 3;

    private static final String EDGE_TTS_DOCKER_PATH = "/opt/venv/bin/edge-tts";
    private static final String EDGE_TTS_LOCAL_MAC_PATH = "/Users/local/Library/Python/3.9/bin/edge-tts";

    private static final List<String> VOICES = List.of(
            "vi-VN-HoaiMyNeural",
            "vi-VN-NamMinhNeural"
    );

    public String generate(String text) throws Exception {

        System.out.println("TTS SERVICE VERSION = ABSOLUTE_EDGE_TTS_002");

        Files.createDirectories(Paths.get("storage/audio"));
        Files.createDirectories(Paths.get("storage/tmp"));

        String cleanText = normalizeText(text);

        if (cleanText.isBlank()) {
            throw new RuntimeException("TTS text is empty");
        }

        System.out.println("TEXT = " + cleanText);

        Exception lastError = null;

        for (String voice : VOICES) {
            for (int attempt = 1; attempt <= MAX_RETRY_PER_VOICE; attempt++) {
                try {
                    System.out.println("TTS VOICE = " + voice + ", ATTEMPT = " + attempt);

                    String output = generateOnce(cleanText, voice);
                    Path audioPath = Paths.get(output);

                    if (isValidAudio(audioPath)) {
                        System.out.println("VOICE OK = " + output);
                        return output;
                    }

                    deleteIfExists(audioPath);
                    throw new RuntimeException("Voice file invalid after generate");

                } catch (Exception e) {
                    lastError = e;

                    System.out.println("TTS FAILED voice=" + voice + ", attempt=" + attempt);
                    e.printStackTrace();

                    Thread.sleep(800L * attempt);
                }
            }
        }

        throw new RuntimeException("Voice file invalid", lastError);
    }

    private String generateOnce(String text, String voice) throws Exception {

        String output = "storage/audio/" + UUID.randomUUID() + ".mp3";
        Path txtFile = Paths.get("storage/tmp/" + UUID.randomUUID() + ".txt");

        Files.writeString(txtFile, text, StandardCharsets.UTF_8);

        String edgeTts = resolveEdgeTtsCommand();

        System.out.println("EDGE TTS RESOLVED PATH = " + edgeTts);
        System.out.println("EDGE TTS FILE EXISTS = " + new File(edgeTts).exists());

        ProcessBuilder pb = new ProcessBuilder(
                edgeTts,
                "--voice", voice,
                "--text", text,
                "--write-media", output
        );

        pb.redirectErrorStream(true);

        System.out.println("EDGE CMD = " + pb.command());

        Process process = pb.start();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)
        )) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println("[EDGE] " + line);
            }
        }

        boolean finished = process.waitFor(60, TimeUnit.SECONDS);

        if (!finished) {
            process.destroyForcibly();
            deleteIfExists(Paths.get(output));
            throw new RuntimeException("Edge TTS timeout");
        }

        int code = process.exitValue();

        System.out.println("EDGE EXIT = " + code);

        Path audioPath = Paths.get(output);
        long size = Files.exists(audioPath) ? Files.size(audioPath) : 0;

        System.out.println("VOICE SIZE = " + size);

        if (code != 0 || !isValidAudio(audioPath)) {
            deleteIfExists(audioPath);
            throw new RuntimeException("Edge TTS failed, exitCode=" + code + ", size=" + size);
        }

        deleteIfExists(txtFile);

        return output;
    }

    private String normalizeText(String text) {

        if (text == null) {
            return "";
        }

        String result = text
                .replace("\r", " ")
                .replace("\n", " ")
                .replace("“", "\"")
                .replace("”", "\"")
                .replace("‘", "'")
                .replace("’", "'")
                .replaceAll("\\s+", " ")
                .trim();

        result = removeRepeatedWords(result);

        if (result.length() > 700) {
            result = result.substring(0, 700);
        }

        return result;
    }

    private String removeRepeatedWords(String text) {

        String[] words = text.split("\\s+");
        StringBuilder sb = new StringBuilder();

        String lastWord = "";
        int repeatCount = 0;

        for (String word : words) {
            String clean = word.toLowerCase();

            if (clean.equals(lastWord)) {
                repeatCount++;
            } else {
                repeatCount = 1;
                lastWord = clean;
            }

            if (repeatCount <= 3) {
                sb.append(word).append(" ");
            }
        }

        return sb.toString().trim();
    }

    private boolean isValidAudio(Path path) throws Exception {

        if (!Files.exists(path)) {
            return false;
        }

        long size = Files.size(path);

        System.out.println("CHECK AUDIO SIZE = " + size);

        return size >= MIN_AUDIO_SIZE;
    }

    private void deleteIfExists(Path path) {

        try {
            Files.deleteIfExists(path);
        } catch (Exception ignored) {
        }
    }

    private String resolveEdgeTtsCommand() {

        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.contains("mac")) {
            File macPath = new File(EDGE_TTS_LOCAL_MAC_PATH);

            if (macPath.exists()) {
                return macPath.getAbsolutePath();
            }
        }

        return EDGE_TTS_DOCKER_PATH;
    }
}