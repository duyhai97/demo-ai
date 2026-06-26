package com.example.backenai.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Service
public class ChromeScreenshotService {

    public String screenshot(String htmlPath) throws Exception {

        Files.createDirectories(Path.of("storage/frames"));

        String output =
                "storage/frames/frame_"
                        + UUID.randomUUID()
                        + ".png";

        String chrome = findChrome();

        String htmlUrl =
                Path.of(htmlPath)
                        .toAbsolutePath()
                        .toUri()
                        .toString();

        List<String> cmd = List.of(
                chrome,
                "--headless=new",
                "--disable-gpu",
                "--hide-scrollbars",
                "--window-size=1080,1920",
                "--screenshot=" + Path.of(output).toAbsolutePath(),
                htmlUrl
        );

        System.out.println("CHROME CMD = " + cmd);

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);

        Process process = pb.start();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(process.getInputStream())
        )) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println("[CHROME] " + line);
            }
        }

        int exit = process.waitFor();

        System.out.println("CHROME EXIT = " + exit);

        if (exit != 0) {
            throw new RuntimeException("Chrome screenshot failed");
        }

        if (!new File(output).exists()) {
            throw new RuntimeException("Screenshot not generated");
        }

        return output;
    }

    private String findChrome() {

        String os =
                System.getProperty("os.name").toLowerCase();

        if (os.contains("mac")) {

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

        if (os.contains("win")) {
            String chrome1 =
                    "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe";

            if (new File(chrome1).exists()) {
                return chrome1;
            }

            String chrome2 =
                    "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe";

            if (new File(chrome2).exists()) {
                return chrome2;
            }
        }

        return "google-chrome";
    }
}