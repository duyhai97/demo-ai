package com.example.backenai.worker;

import com.example.backenai.constant.JobStatus;
import com.example.backenai.model.VideoJob;
import com.example.backenai.queue.JobQueue;
import com.example.backenai.service.*;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Component
@AllArgsConstructor
public class VideoWorker {

    private final JobQueue queue;
    private final JobService jobService;

    private final OpenRouterService openRouterService;
    private final ScriptCleanerService scriptCleanerService;
    private final ScriptSegmentService scriptSegmentService;
    private final TtsService ttsService;
    private final HtmlFrameService htmlFrameService;
    private final VideoRenderService renderService;

    @PostConstruct
    public void start() {

        Thread worker = new Thread(() -> {

            System.out.println("VIDEO WORKER STARTED");

            while (true) {

                VideoJob job = null;

                try {
                    job = queue.pop();

                    System.out.println("PROCESSING JOB = " + job.getJobId());

                    update(job, JobStatus.PROCESSING, 5, "Bắt đầu xử lý video");

                    validateJob(job);

                    System.out.println("IMAGE COUNT IN WORKER = " + job.getImagePaths().size());
                    System.out.println("IMAGE PATHS IN WORKER = " + job.getImagePaths());

                    update(job, JobStatus.PROCESSING, 15, "Đang tạo lời thoại AI");

                    String rawScript = openRouterService.generateVoiceScript(job.getProductName());

                    update(job, JobStatus.PROCESSING, 25, "Đang làm sạch lời thoại");

                    String cleanScript = scriptCleanerService.clean(rawScript);

                    if (cleanScript == null || cleanScript.isBlank()) {
                        throw new RuntimeException("Script is empty");
                    }

                    job.setScript(cleanScript);
                    jobService.save(job);

                    System.out.println("SCRIPT = " + cleanScript);

                    update(job, JobStatus.PROCESSING, 40, "Đang tạo giọng đọc AI");

                    String voicePath = ttsService.generate(cleanScript);

                    double voiceDuration = ttsService.getAudioDurationSeconds(voicePath);

                    job.setVoicePath(voicePath);
                    jobService.save(job);

                    System.out.println("VOICE = " + voicePath);
                    System.out.println("VOICE DURATION = " + voiceDuration);

                    update(job, JobStatus.PROCESSING, 55, "Đang tạo phụ đề");

                    List<String> captions = scriptSegmentService.splitAndWrap(cleanScript);

                    if (captions == null || captions.isEmpty()) {
                        throw new RuntimeException("Captions is empty");
                    }

                    System.out.println("CAPTION COUNT = " + captions.size());
                    System.out.println("CAPTIONS = " + captions);

                    update(job, JobStatus.PROCESSING, 70, "Đang render ảnh thành frame");

                    List<String> frames = htmlFrameService.generateFrames(job.getImagePaths(), job.getProductName(), captions);

                    if (frames == null || frames.isEmpty()) {
                        throw new RuntimeException("Frames is empty");
                    }

                    job.setFramePaths(frames);
                    jobService.save(job);

                    System.out.println("FRAME COUNT IN WORKER = " + frames.size());
                    System.out.println("FRAMES = " + frames);

                    update(job, JobStatus.PROCESSING, 88, "Đang ghép video theo thời lượng giọng đọc");

                    String videoPath = renderService.render(frames, voicePath);

                    job.setVideoPath(videoPath);

                    String fileName = new File(videoPath).getName();

                    job.setVideoUrl("/videos/" + fileName);

                    update(job, JobStatus.DONE, 100, "Hoàn tất video");

                    System.out.println("DONE JOB = " + job.getJobId());
                    System.out.println("VIDEO = " + videoPath);

                } catch (Exception e) {

                    e.printStackTrace();

                    if (job != null) {
                        job.setStatus(JobStatus.FAILED);
                        job.setProgress(100);
                        job.setCurrentStep("Render video thất bại");
                        job.setError(e.getMessage());
                        jobService.save(job);
                    }
                }
            }
        });

        worker.setDaemon(true);
        worker.start();
    }

    private void validateJob(VideoJob job) {

        if (job.getJobId() == null || job.getJobId().isBlank()) {
            throw new RuntimeException("Job ID is empty");
        }

        if (job.getProductName() == null || job.getProductName().isBlank()) {
            throw new RuntimeException("Product name is empty");
        }

        if (job.getImagePaths() == null || job.getImagePaths().isEmpty()) {
            throw new RuntimeException("Image paths is empty");
        }
    }

    private void update(VideoJob job, JobStatus status, int progress, String currentStep) {
        job.setStatus(status);
        job.setProgress(progress);
        job.setCurrentStep(currentStep);
        jobService.save(job);
    }
}