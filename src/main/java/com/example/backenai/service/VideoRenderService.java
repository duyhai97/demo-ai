package com.example.backenai.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VideoRenderService {

    private final TtsService ttsService;

    public String render(
            List<String> frames,
            String voicePath
    ) throws Exception {

        if (frames == null || frames.isEmpty()) {
            throw new RuntimeException("Frames is empty");
        }

        Files.createDirectories(Path.of("storage/videos"));
        Files.createDirectories(Path.of("storage/tmp"));

        double audioDuration = ttsService.getAudioDurationSeconds(voicePath);

        if (audioDuration <= 0) {
            throw new RuntimeException("Invalid audio duration: " + audioDuration);
        }

        double frameDuration = audioDuration / frames.size();

        if (frameDuration < 0.3) {
            frameDuration = 0.3;
        }

        System.out.println("AUDIO DURATION = " + audioDuration);
        System.out.println("FRAME COUNT = " + frames.size());
        System.out.println("FRAME DURATION = " + frameDuration);

        String listFile =
                "storage/tmp/frame_"
                        + UUID.randomUUID()
                        + ".txt";

        String output =
                "storage/videos/video_"
                        + UUID.randomUUID()
                        + ".mp4";

        StringBuilder sb = new StringBuilder();

        for (String frame : frames) {

            String framePath = Path.of(frame)
                    .toAbsolutePath()
                    .toString()
                    .replace("\\", "/");

            sb.append("file '")
                    .append(framePath)
                    .append("'\n");

            sb.append("duration ")
                    .append(formatDuration(frameDuration))
                    .append("\n");
        }

        String lastFramePath = Path.of(frames.get(frames.size() - 1))
                .toAbsolutePath()
                .toString()
                .replace("\\", "/");

        sb.append("file '")
                .append(lastFramePath)
                .append("'\n");

        Files.writeString(
                Path.of(listFile),
                sb.toString(),
                StandardCharsets.UTF_8
        );

        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-y",

                "-f", "concat",
                "-safe", "0",
                "-i", listFile,

                "-i", voicePath,

                "-t", formatDuration(audioDuration),

                "-vsync", "vfr",
                "-pix_fmt", "yuv420p",

                "-c:v", "libx264",
                "-preset", "veryfast",

                "-c:a", "aac",
                "-b:a", "192k",

                "-movflags", "+faststart",
                "-shortest",

                output
        );

        pb.redirectErrorStream(true);

        System.out.println("==================");
        System.out.println("FFMPEG");
        pb.command().forEach(System.out::println);
        System.out.println("==================");

        Process process = pb.start();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        process.getInputStream(),
                        StandardCharsets.UTF_8
                )
        )) {
            String line;

            while ((line = br.readLine()) != null) {
                System.out.println("[FFMPEG] " + line);
            }
        }

        int exit = process.waitFor();

        if (exit != 0) {
            throw new RuntimeException("FFmpeg render failed, exitCode=" + exit);
        }

        return output;
    }

    private String formatDuration(double seconds) {
        return String.format(Locale.US, "%.3f", seconds);
    }
}