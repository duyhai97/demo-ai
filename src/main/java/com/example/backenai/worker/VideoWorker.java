package com.example.backenai.worker;

import com.example.backenai.constant.JobStatus;
import com.example.backenai.model.VideoJob;
import com.example.backenai.queue.JobQueue;
import com.example.backenai.service.*;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Component
@AllArgsConstructor
public class VideoWorker {

    private final JobQueue queue;
    private final JobService jobService;

    private final VideoRenderService renderService;
    private final ScriptService scriptService;
    private final TtsService ttsService;
    private final SubtitleService subtitleService;

    @PostConstruct
    public void start() {

        Thread worker = new Thread(() -> {

            System.out.println("VIDEO WORKER STARTED");

            while (true) {

                VideoJob job = null;

                try {

                    job = queue.pop();

                    job.setStatus(JobStatus.PROCESSING);
                    jobService.save(job);

                    // 1. GPT Script
                    String script =
                            scriptService.generateScript(
                                    job.getProductName()
                            );

                    // 2. TTS
                    String voicePath =
                            ttsService.generate(
                                    script
                            );

                    // 3. Subtitle
                    String subtitlePath =
                            subtitleService.create(
                                    script
                            );

                    // 4. Image
                    String imagePath =
                            "storage/images/"
                                    + new File(
                                    job.getImagePath()
                            ).getName();

                    // 5. Render video
                    String videoPath =
                            renderService.render(
                                    imagePath,
                                    voicePath,
                                    subtitlePath
                            );

                    job.setScript(script);
                    job.setVoicePath(voicePath);
                    job.setSubtitlePath(subtitlePath);

                    job.setVideoPath(videoPath);

                    String fileName =
                            new File(videoPath)
                                    .getName();

                    job.setVideoUrl(
                            "/videos/" + fileName
                    );

                    job.setStatus(
                            JobStatus.DONE
                    );

                } catch (Exception e) {

                    e.printStackTrace();

                    if (job != null) {
                        job.setStatus(
                                JobStatus.FAILED
                        );
                    }

                } finally {

                    if (job != null) {
                        jobService.save(job);
                    }
                }
            }
        });

        worker.setDaemon(true);
        worker.start();
    }

    public String generateVoice() throws Exception {

        Files.createDirectories(
                Paths.get("storage/audio")
        );

        String output =
                "storage/audio/"
                        + UUID.randomUUID()
                        + ".mp3";

        ProcessBuilder pb =
                new ProcessBuilder(
                        "ffmpeg",
                        "-y",
                        "-f", "lavfi",
                        "-i", "anullsrc=r=44100:cl=stereo",
                        "-t", "5",
                        "-q:a", "9",
                        output
                );

        pb.redirectErrorStream(true);

        Process p = pb.start();

        int code = p.waitFor();

        if (code != 0) {
            throw new RuntimeException(
                    "Create voice failed"
            );
        }

        return output;
    }
}