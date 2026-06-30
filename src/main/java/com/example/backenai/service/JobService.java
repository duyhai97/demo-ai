package com.example.backenai.service;

import com.example.backenai.constant.JobStatus;
import com.example.backenai.entity.JobLogEntity;
import com.example.backenai.entity.UploadedImageEntity;
import com.example.backenai.entity.VideoJobEntity;
import com.example.backenai.model.VideoJob;
import com.example.backenai.repository.JobLogRepository;
import com.example.backenai.repository.UploadedImageRepository;
import com.example.backenai.repository.VideoJobRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class JobService {

    private final VideoJobRepository videoJobRepository;
    private final UploadedImageRepository uploadedImageRepository;
    private final JobLogRepository jobLogRepository;
    private final ObjectMapper objectMapper;

    public JobService(
            VideoJobRepository videoJobRepository,
            UploadedImageRepository uploadedImageRepository,
            JobLogRepository jobLogRepository,
            ObjectMapper objectMapper
    ) {
        this.videoJobRepository = videoJobRepository;
        this.uploadedImageRepository = uploadedImageRepository;
        this.jobLogRepository = jobLogRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
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

        save(job);
        log(job.getJobId(), "INFO", "Tạo job mới");

        return job;
    }

    @Transactional(readOnly = true)
    public VideoJob get(String jobId) {
        return videoJobRepository.findByJobId(jobId)
                .map(this::toModel)
                .orElse(null);
    }

    @Transactional
    public void save(VideoJob job) {
        VideoJobEntity entity =
                videoJobRepository.findByJobId(job.getJobId())
                        .orElseGet(VideoJobEntity::new);

        entity.setJobId(job.getJobId());
        entity.setProductName(job.getProductName());
        entity.setAffiliateLink(job.getAffiliateLink());
        entity.setScript(job.getScript());
        entity.setVoicePath(job.getVoicePath());
        entity.setSubtitlePath(job.getSubtitlePath());
        entity.setVideoPath(job.getVideoPath());
        entity.setVideoUrl(job.getVideoUrl());
        entity.setStatus(job.getStatus());
        entity.setProgress(job.getProgress());
        entity.setCurrentStep(job.getCurrentStep());
        entity.setError(job.getError());
        entity.setFramePaths(toJson(job.getFramePaths()));
        entity.setCreatedBy(job.getCreatedBy());

        videoJobRepository.save(entity);

        if (job.getImagePaths() != null) {
            uploadedImageRepository.deleteByJobId(job.getJobId());

            for (int i = 0; i < job.getImagePaths().size(); i++) {
                UploadedImageEntity image = new UploadedImageEntity();
                image.setJobId(job.getJobId());
                image.setImagePath(job.getImagePaths().get(i));
                image.setSortOrder(i);
                uploadedImageRepository.save(image);
            }
        }
    }

    @Transactional
    public void updateProgress(
            VideoJob job,
            int progress,
            String currentStep
    ) {
        job.setProgress(progress);
        job.setCurrentStep(currentStep);
        save(job);
        log(job.getJobId(), "INFO", progress + "% - " + currentStep);
    }

    @Transactional
    public void log(
            String jobId,
            String level,
            String message
    ) {
        JobLogEntity log = new JobLogEntity();
        log.setJobId(jobId);
        log.setLevel(level);
        log.setMessage(message);
        jobLogRepository.save(log);
    }

    private VideoJob toModel(VideoJobEntity entity) {
        VideoJob job = new VideoJob();

        job.setJobId(entity.getJobId());
        job.setProductName(entity.getProductName());
        job.setAffiliateLink(entity.getAffiliateLink());

        List<String> imagePaths =
                uploadedImageRepository.findByJobIdOrderBySortOrderAsc(entity.getJobId())
                        .stream()
                        .map(UploadedImageEntity::getImagePath)
                        .toList();

        job.setImagePaths(imagePaths);
        job.setFramePaths(fromJson(entity.getFramePaths()));
        job.setScript(entity.getScript());
        job.setVoicePath(entity.getVoicePath());
        job.setSubtitlePath(entity.getSubtitlePath());
        job.setVideoPath(entity.getVideoPath());
        job.setVideoUrl(entity.getVideoUrl());
        job.setStatus(entity.getStatus());
        job.setProgress(entity.getProgress() == null ? 0 : entity.getProgress());
        job.setCurrentStep(entity.getCurrentStep());
        job.setError(entity.getError());
        job.setCreatedBy(entity.getCreatedBy());
        job.setCreatedAt(entity.getCreatedAt() == null ? null : entity.getCreatedAt().toString());
        return job;
    }

    private String toJson(List<String> list) {
        try {
            if (list == null) {
                return "[]";
            }

            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            throw new RuntimeException("Convert list to JSON failed", e);
        }
    }

    private List<String> fromJson(String json) {
        try {
            if (json == null || json.isBlank()) {
                return new ArrayList<>();
            }

            return objectMapper.readValue(
                    json,
                    new TypeReference<List<String>>() {}
            );
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Transactional(readOnly = true)
    public Page<VideoJob> findAll(Pageable pageable) {
        return videoJobRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toModel);
    }

    public Page<VideoJob> findByCreatedBy(String username, Pageable pageable) {
        return videoJobRepository
                .findByCreatedByOrderByCreatedAtDesc(username, pageable)
                .map(this::toModel);
    }

}