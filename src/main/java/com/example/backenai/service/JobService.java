package com.example.backenai.service;

import com.example.backenai.constant.JobStatus;
import com.example.backenai.model.VideoJob;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class JobService {

    private final Map<String, VideoJob> jobs = new ConcurrentHashMap<>();

    public VideoJob createJob(
            String productName,
            String affiliateLink,
            List<String> imagePaths
    ) {
        VideoJob job = new VideoJob();

        job.setJobId(UUID.randomUUID().toString());
        job.setProductName(productName);
        job.setAffiliateLink(affiliateLink);
        job.setImagePaths(imagePaths);
        job.setStatus(JobStatus.PENDING);
        job.setProgress(0);
        job.setCurrentStep("Đang chờ xử lý");

        jobs.put(job.getJobId(), job);

        return job;
    }

    public VideoJob get(String jobId) {
        return jobs.get(jobId);
    }

    public void save(VideoJob job) {
        jobs.put(job.getJobId(), job);
    }

    public void updateProgress(
            VideoJob job,
            int progress,
            String currentStep
    ) {
        job.setProgress(progress);
        job.setCurrentStep(currentStep);
        save(job);
    }
}