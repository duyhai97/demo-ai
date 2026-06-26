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

    private static final int WIDTH = 1080;
    private static final int HEIGHT = 1920;
    private static final int FPS = 30;

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

        String absoluteVoicePath =
                normalizePath(voicePath);

        double audioDuration =
                ttsService.getAudioDurationSeconds(
                        absoluteVoicePath
                );

        if (audioDuration <= 0) {
            throw new RuntimeException("Invalid audio duration: " + audioDuration);
        }

        int frameCount =
                frames.size();

        double transitionDuration =
                frameCount > 1
                        ? calculateTransitionDuration(audioDuration, frameCount)
                        : 0;

        double clipDuration =
                frameCount > 1
                        ? (audioDuration + transitionDuration * (frameCount - 1)) / frameCount
                        : audioDuration;

        if (clipDuration <= transitionDuration) {
            transitionDuration = 0;
            clipDuration = audioDuration / frameCount;
        }

        String output =
                "storage/videos/video_"
                        + UUID.randomUUID()
                        + ".mp4";

        String absoluteOutput =
                normalizePath(output);

        System.out.println("AUDIO DURATION = " + audioDuration);
        System.out.println("IMAGE / FRAME COUNT = " + frameCount);
        System.out.println("CLIP DURATION = " + clipDuration);
        System.out.println("TRANSITION DURATION = " + transitionDuration);

        ProcessBuilder pb =
                buildFfmpegCommand(
                        frames,
                        absoluteVoicePath,
                        absoluteOutput,
                        audioDuration,
                        clipDuration,
                        transitionDuration
                );

        pb.redirectErrorStream(true);

        System.out.println("==================");
        System.out.println("FFMPEG KEN BURNS + FADE");
        pb.command().forEach(System.out::println);
        System.out.println("==================");

        Process process =
                pb.start();

        try (BufferedReader br =
                     new BufferedReader(
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

        int exit =
                process.waitFor();

        if (exit != 0) {
            throw new RuntimeException(
                    "FFmpeg render failed, exitCode=" + exit
            );
        }

        return output;
    }

    private ProcessBuilder buildFfmpegCommand(
            List<String> frames,
            String absoluteVoicePath,
            String absoluteOutput,
            double audioDuration,
            double clipDuration,
            double transitionDuration
    ) {
        List<String> command =
                new java.util.ArrayList<>();

        command.add("ffmpeg");
        command.add("-y");

        for (String frame : frames) {
            command.add("-loop");
            command.add("1");

            command.add("-t");
            command.add(formatDuration(clipDuration));

            command.add("-i");
            command.add(normalizePath(frame));
        }

        command.add("-i");
        command.add(absoluteVoicePath);

        String filter =
                buildFilterComplex(
                        frames.size(),
                        clipDuration,
                        transitionDuration
                );

        command.add("-filter_complex");
        command.add(filter);

        String finalVideoLabel =
                frames.size() == 1
                        ? "[v0]"
                        : "[vx" + (frames.size() - 1) + "]";

        command.add("-map");
        command.add(finalVideoLabel);

        command.add("-map");
        command.add(frames.size() + ":a");

        command.add("-t");
        command.add(formatDuration(audioDuration));

        command.add("-r");
        command.add(String.valueOf(FPS));

        command.add("-pix_fmt");
        command.add("yuv420p");

        command.add("-c:v");
        command.add("libx264");

        command.add("-preset");
        command.add("veryfast");

        command.add("-crf");
        command.add("23");

        command.add("-c:a");
        command.add("aac");

        command.add("-b:a");
        command.add("192k");

        command.add("-movflags");
        command.add("+faststart");

        command.add("-shortest");

        command.add(absoluteOutput);

        return new ProcessBuilder(command);
    }

    private String buildFilterComplex(
            int frameCount,
            double clipDuration,
            double transitionDuration
    ) {
        StringBuilder filter =
                new StringBuilder();

        int zoomFrames =
                Math.max(1, (int) Math.ceil(clipDuration * FPS));

        for (int i = 0; i < frameCount; i++) {

            String pan =
                    i % 2 == 0
                            ? "x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)'"
                            : "x='iw/2-(iw/zoom/2)+20*sin(on/30)':y='ih/2-(ih/zoom/2)'";

            filter.append("[")
                    .append(i)
                    .append(":v]")
                    .append("scale=")
                    .append(WIDTH)
                    .append(":")
                    .append(HEIGHT)
                    .append(":force_original_aspect_ratio=increase,")
                    .append("crop=")
                    .append(WIDTH)
                    .append(":")
                    .append(HEIGHT)
                    .append(",")
                    .append("setsar=1,")
                    .append("zoompan=")
                    .append("z='min(zoom+0.0015,1.08)':")
                    .append("d=")
                    .append(zoomFrames)
                    .append(":")
                    .append("s=")
                    .append(WIDTH)
                    .append("x")
                    .append(HEIGHT)
                    .append(":")
                    .append("fps=")
                    .append(FPS)
                    .append(":")
                    .append(pan)
                    .append(",")
                    .append("trim=duration=")
                    .append(formatDuration(clipDuration))
                    .append(",")
                    .append("setpts=PTS-STARTPTS")
                    .append("[v")
                    .append(i)
                    .append("];");
        }

        if (frameCount == 1) {
            return filter.toString();
        }

        double offsetStep =
                clipDuration - transitionDuration;

        filter.append("[v0][v1]")
                .append("xfade=transition=fade:")
                .append("duration=")
                .append(formatDuration(transitionDuration))
                .append(":offset=")
                .append(formatDuration(offsetStep))
                .append("[vx1];");

        for (int i = 2; i < frameCount; i++) {
            double offset =
                    offsetStep * i;

            filter.append("[vx")
                    .append(i - 1)
                    .append("][v")
                    .append(i)
                    .append("]")
                    .append("xfade=transition=fade:")
                    .append("duration=")
                    .append(formatDuration(transitionDuration))
                    .append(":offset=")
                    .append(formatDuration(offset))
                    .append("[vx")
                    .append(i)
                    .append("];");
        }

        return filter.toString();
    }

    private double calculateTransitionDuration(
            double audioDuration,
            int frameCount
    ) {
        double base =
                audioDuration / frameCount;

        if (base >= 3) {
            return 0.45;
        }

        if (base >= 2) {
            return 0.35;
        }

        if (base >= 1) {
            return 0.25;
        }

        return 0.12;
    }

    private String normalizePath(String path) {
        return Path.of(path)
                .toAbsolutePath()
                .toString()
                .replace("\\", "/");
    }

    private String formatDuration(double seconds) {
        return String.format(
                Locale.US,
                "%.3f",
                seconds
        );
    }
}