package com.example.backenai.service;

import com.example.backenai.constant.JobStatus;
import com.example.backenai.entity.JobLogEntity;
import com.example.backenai.entity.UploadedImageEntity;
import com.example.backenai.entity.UserEntity;
import com.example.backenai.entity.VideoJobEntity;
import com.example.backenai.model.VideoJob;
import com.example.backenai.model.VideoQuotaResponse;
import com.example.backenai.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class JobService {

    private final VideoJobRepository videoJobRepository;
    private final UploadedImageRepository uploadedImageRepository;
    private final JobLogRepository jobLogRepository;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;


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

    public void validateDailyVideoLimit(Authentication authentication) {
        VideoQuotaResponse quota = getQuota(authentication);

        if (quota.dailyLimit() == -1) {
            return;
        }

        if (quota.remainingToday() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Bạn đã hết lượt tạo video hôm nay. Vui lòng mua thêm lượt."
            );
        }
    }

    public VideoQuotaResponse getQuota(Authentication authentication) {
        String username = authentication.getName();

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(a ->
                        "ADMIN".equals(a.getAuthority())
                                || "ROLE_ADMIN".equals(a.getAuthority())
                );

        if (isAdmin) {
            return new VideoQuotaResponse(
                    -1,
                    0,
                    0,
                    -1,
                    -1
            );
        }

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "User not found"
                ));

        int dailyLimit = user.getDailyVideoLimit() == null
                ? 0
                : user.getDailyVideoLimit();

        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        long usedToday = videoJobRepository.countByCreatedByAndCreatedAtBetween(
                username,
                start,
                end
        );

        long extraToday = purchaseOrderRepository.sumPaidExtraVideosToday(
                username,
                start,
                end
        );

        long totalToday = dailyLimit + extraToday;
        long remainingToday = Math.max(0, totalToday - usedToday);

        return new VideoQuotaResponse(
                dailyLimit,
                usedToday,
                extraToday,
                totalToday,
                remainingToday
        );
    }

}