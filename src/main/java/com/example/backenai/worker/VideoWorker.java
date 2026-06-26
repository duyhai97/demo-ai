package com.example.backenai.worker;

import com.example.backenai.constant.JobStatus;
import com.example.backenai.model.VideoJob;
import com.example.backenai.queue.JobQueue;
import com.example.backenai.service.JobService;
import com.example.backenai.worker.step.*;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class VideoWorker {

    private final JobQueue queue;
    private final JobService jobService;

    private final GenerateScriptStep generateScriptStep;
    private final GenerateVoiceStep generateVoiceStep;
    private final GenerateFrameStep generateFrameStep;
    private final RenderVideoStep renderVideoStep;
    private final CleanupStep cleanupStep;

    @PostConstruct
    public void start() {

        Thread worker = new Thread(() -> {

            System.out.println("VIDEO WORKER STARTED");

            while (true) {

                VideoJob job = null;

                try {
                    job = queue.pop();

                    System.out.println("PROCESSING JOB = " + job.getJobId());

                    validateJob(job);

                    System.out.println("IMAGE COUNT IN WORKER = " + job.getImagePaths().size());
                    System.out.println("IMAGE PATHS IN WORKER = " + job.getImagePaths());

                    update(job, JobStatus.PROCESSING, 5, "Bắt đầu xử lý video");

                    update(job, JobStatus.PROCESSING, 15, "Đang tạo lời thoại AI");
                    generateScriptStep.execute(job);

                    update(job, JobStatus.PROCESSING, 40, "Đang tạo giọng đọc AI");
                    generateVoiceStep.execute(job);

                    update(job, JobStatus.PROCESSING, 70, "Đang render ảnh thành frame");
                    generateFrameStep.execute(job);

                    update(job, JobStatus.PROCESSING, 88, "Đang ghép video theo thời lượng giọng đọc");
                    renderVideoStep.execute(job);

                    update(job, JobStatus.DONE, 100, "Hoàn tất video");

                    cleanupStep.execute(job);

                    System.out.println("DONE JOB = " + job.getJobId());

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

        if (job == null) {
            throw new RuntimeException("Job is null");
        }

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

    private void update(
            VideoJob job,
            JobStatus status,
            int progress,
            String currentStep
    ) {
        job.setStatus(status);
        job.setProgress(progress);
        job.setCurrentStep(currentStep);
        jobService.save(job);
    }
}