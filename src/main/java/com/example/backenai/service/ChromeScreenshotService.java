package com.example.backenai.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ChromeScreenshotService {

    public String screenshot(String htmlPath) throws Exception {

        Files.createDirectories(Path.of("storage/frames"));

        String output = "storage/frames/frame_" + UUID.randomUUID() + ".png";

        String chrome = findChrome();

        File chromeFile = new File(chrome);

        System.out.println("CHROME SERVICE VERSION = GOOGLE_CHROME_001");
        System.out.println("CHROME BIN = " + chrome);
        System.out.println("CHROME EXISTS = " + chromeFile.exists());

        String htmlUrl = Path.of(htmlPath)
                .toAbsolutePath()
                .toUri()
                .toString();

        List<String> cmd = new ArrayList<>();

        cmd.add(chrome);
        cmd.add("--headless=new");
        cmd.add("--no-sandbox");
        cmd.add("--disable-dev-shm-usage");
        cmd.add("--disable-gpu");
        cmd.add("--disable-software-rasterizer");
        cmd.add("--hide-scrollbars");
        cmd.add("--window-size=1080,1920");
        cmd.add("--screenshot=" + Path.of(output).toAbsolutePath());
        cmd.add(htmlUrl);

        System.out.println("CHROME CMD = " + cmd);

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);

        Process process = pb.start();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {

            String line;

            while ((line = br.readLine()) != null) {
                System.out.println("[CHROME] " + line);
            }
        }

        int exit = process.waitFor();

        System.out.println("CHROME EXIT = " + exit);

        if (exit != 0) {
            throw new RuntimeException("Chrome screenshot failed. ExitCode=" + exit);
        }

        File image = new File(output);

        if (!image.exists()) {
            throw new RuntimeException("Screenshot not generated");
        }

        if (image.length() < 1024) {
            throw new RuntimeException("Screenshot invalid. Size=" + image.length());
        }

        return output;
    }

    private String findChrome() {

        String env = System.getenv("CHROME_BIN");

        if (env != null && !env.isBlank()) {
            return env;
        }

        if (System.getProperty("os.name").toLowerCase().contains("mac")) {

            String chrome =
                    "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome";

            if (new File(chrome).exists()) {
                return chrome;
            }

            String chromium =
                    "/Applications/Chromium.app/Contents/MacOS/Chromium";

            if (new File(chromium).exists()) {
                return chromium;
            }
        }

        throw new IllegalStateException(
                "Chrome executable not found. Please set CHROME_BIN."
        );
    }
}