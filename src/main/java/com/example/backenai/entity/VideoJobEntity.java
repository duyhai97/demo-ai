package com.example.backenai.entity;

import com.example.backenai.constant.JobStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "video_jobs")
public class VideoJobEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_id", nullable = false, unique = true, length = 80)
    private String jobId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "product_name", columnDefinition = "TEXT")
    private String productName;

    @Column(name = "affiliate_link", columnDefinition = "TEXT")
    private String affiliateLink;

    @Column(name = "script", columnDefinition = "LONGTEXT")
    private String script;

    @Column(name = "voice_path", columnDefinition = "TEXT")
    private String voicePath;

    @Column(name = "subtitle_path", columnDefinition = "TEXT")
    private String subtitlePath;

    @Column(name = "video_path", columnDefinition = "TEXT")
    private String videoPath;

    @Column(name = "video_url", columnDefinition = "TEXT")
    private String videoUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30)
    private JobStatus status;

    private Integer progress;

    @Column(name = "current_step", columnDefinition = "TEXT")
    private String currentStep;

    @Column(name = "error", columnDefinition = "LONGTEXT")
    private String error;

    @Column(name = "frame_paths", columnDefinition = "LONGTEXT")
    private String framePaths;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}