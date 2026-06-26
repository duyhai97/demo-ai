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

        String absoluteVoicePath = normalizePath(voicePath);

        double audioDuration =
                ttsService.getAudioDurationSeconds(absoluteVoicePath);

        if (audioDuration <= 0) {
            throw new RuntimeException("Invalid audio duration: " + audioDuration);
        }

        double frameDuration =
                audioDuration / frames.size();

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
            sb.append("file '")
                    .append(normalizePath(frame))
                    .append("'\n");

            sb.append("duration ")
                    .append(formatDuration(frameDuration))
                    .append("\n");
        }

        sb.append("file '")
                .append(normalizePath(frames.get(frames.size() - 1)))
                .append("'\n");

        Files.writeString(
                Path.of(listFile),
                sb.toString(),
                StandardCharsets.UTF_8
        );

        String absoluteListFile = normalizePath(listFile);
        String absoluteOutput = normalizePath(output);

        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-y",

                "-f", "concat",
                "-safe", "0",
                "-i", absoluteListFile,

                "-i", absoluteVoicePath,

                "-t", formatDuration(audioDuration),

                "-vf", "scale=720:1280:force_original_aspect_ratio=increase,crop=720:1280,setsar=1,fps=24",

                "-pix_fmt", "yuv420p",
                "-c:v", "libx264",
                "-preset", "ultrafast",
                "-crf", "28",

                "-c:a", "aac",
                "-b:a", "128k",

                "-movflags", "+faststart",
                "-shortest",

                absoluteOutput
        );

        pb.redirectErrorStream(true);

        System.out.println("==================");
        System.out.println("FFMPEG LIGHT RENDER");
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

    private String normalizePath(String path) {
        return Path.of(path)
                .toAbsolutePath()
                .toString()
                .replace("\\", "/");
    }

    private String formatDuration(double seconds) {
        return String.format(Locale.US, "%.3f", seconds);
    }
}