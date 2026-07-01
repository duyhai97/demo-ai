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

    private static final int WIDTH = 720;
    private static final int HEIGHT = 1280;
    private static final int FPS = 30;

    public String render(
            List<String> frames,
            String voicePath
    ) throws Exception {

        if (frames == null || frames.isEmpty()) {
            throw new RuntimeException("Frames is empty");
        }

        Files.createDirectories(Path.of("storage/videos"));
        Files.createDirectories(Path.of("storage/tmp"));
        Files.createDirectories(Path.of("storage/tmp/scenes"));

        String absoluteVoicePath = normalizePath(voicePath);

        double audioDuration =
                ttsService.getAudioDurationSeconds(absoluteVoicePath);

        if (audioDuration <= 0) {
            throw new RuntimeException("Invalid audio duration: " + audioDuration);
        }

        double sceneDuration = audioDuration / frames.size();

        List<String> sceneVideos =
                renderSceneVideos(frames, sceneDuration);

        String concatVideo =
                concatScenes(sceneVideos);

        String output =
                "storage/videos/video_"
                        + UUID.randomUUID()
                        + ".mp4";

        muxAudio(
                concatVideo,
                absoluteVoicePath,
                output,
                audioDuration
        );

        return output;
    }

    private List<String> renderSceneVideos(
            List<String> frames,
            double sceneDuration
    ) throws Exception {

        List<String> sceneVideos = new java.util.ArrayList<>();

        for (int i = 0; i < frames.size(); i++) {
            String frame = normalizePath(frames.get(i));

            String output =
                    "storage/tmp/scenes/scene_"
                            + UUID.randomUUID()
                            + ".mp4";

            String absoluteOutput = normalizePath(output);

            String effect = pickEffect(i);

            String vf = buildVideoFilter(effect, sceneDuration);

            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg",
                    "-y",

                    "-loop", "1",
                    "-i", frame,

                    "-t", formatDuration(sceneDuration),

                    "-vf", vf,

                    "-an",
                    "-pix_fmt", "yuv420p",
                    "-c:v", "libx264",
                    "-preset", "ultrafast",
                    "-crf", "25",
                    "-r", String.valueOf(FPS),

                    absoluteOutput
            );

            run(pb, "FFMPEG SCENE " + (i + 1) + " " + effect);

            sceneVideos.add(output);
        }

        return sceneVideos;
    }

    private String concatScenes(List<String> sceneVideos) throws Exception {
        String listFile =
                "storage/tmp/concat_"
                        + UUID.randomUUID()
                        + ".txt";

        String output =
                "storage/tmp/concat_"
                        + UUID.randomUUID()
                        + ".mp4";

        StringBuilder sb = new StringBuilder();

        for (String scene : sceneVideos) {
            sb.append("file '")
                    .append(normalizePath(scene))
                    .append("'\n");
        }

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
                "-i", normalizePath(listFile),

                "-c", "copy",

                normalizePath(output)
        );

        run(pb, "FFMPEG CONCAT SCENES");

        return output;
    }

    private void muxAudio(
            String videoPath,
            String voicePath,
            String output,
            double audioDuration
    ) throws Exception {

        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-y",

                "-i", normalizePath(videoPath),
                "-i", normalizePath(voicePath),

                "-t", formatDuration(audioDuration),

                "-map", "0:v:0",
                "-map", "1:a:0",

                "-c:v", "copy",
                "-c:a", "aac",
                "-b:a", "128k",

                "-movflags", "+faststart",
                "-shortest",

                normalizePath(output)
        );

        run(pb, "FFMPEG MUX AUDIO");
    }

    private String buildVideoFilter(
            String effect,
            double duration
    ) {
        int frames = Math.max(1, (int) Math.round(duration * FPS));

        String base =
                "scale=900:1600:force_original_aspect_ratio=increase," +
                        "crop=900:1600,";

        String zoomPan;

        switch (effect) {
            case "zoom_out" -> zoomPan =
                    "zoompan=" +
                            "z='if(eq(on,0),1.18,max(1.0,zoom-0.0012))':" +
                            "x='iw/2-(iw/zoom/2)':" +
                            "y='ih/2-(ih/zoom/2)':" +
                            "d=" + frames + ":" +
                            "s=" + WIDTH + "x" + HEIGHT + ":" +
                            "fps=" + FPS;

            case "pan_left" -> zoomPan =
                    "zoompan=" +
                            "z='1.13':" +
                            "x='(iw-iw/zoom)*(on/" + frames + ")':" +
                            "y='ih/2-(ih/zoom/2)':" +
                            "d=" + frames + ":" +
                            "s=" + WIDTH + "x" + HEIGHT + ":" +
                            "fps=" + FPS;

            case "pan_right" -> zoomPan =
                    "zoompan=" +
                            "z='1.13':" +
                            "x='(iw-iw/zoom)*(1-on/" + frames + ")':" +
                            "y='ih/2-(ih/zoom/2)':" +
                            "d=" + frames + ":" +
                            "s=" + WIDTH + "x" + HEIGHT + ":" +
                            "fps=" + FPS;

            default -> zoomPan =
                    "zoompan=" +
                            "z='min(1.18,zoom+0.0012)':" +
                            "x='iw/2-(iw/zoom/2)':" +
                            "y='ih/2-(ih/zoom/2)':" +
                            "d=" + frames + ":" +
                            "s=" + WIDTH + "x" + HEIGHT + ":" +
                            "fps=" + FPS;
        }

        return base + zoomPan + ",setsar=1,format=yuv420p";
    }

    private String pickEffect(int index) {
        return switch (index % 4) {
            case 0 -> "zoom_in";
            case 1 -> "pan_left";
            case 2 -> "zoom_out";
            default -> "pan_right";
        };
    }

    private void run(
            ProcessBuilder pb,
            String title
    ) throws Exception {

        pb.redirectErrorStream(true);

        System.out.println("==================");
        System.out.println(title);
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
            throw new RuntimeException(title + " failed, exitCode=" + exit);
        }
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