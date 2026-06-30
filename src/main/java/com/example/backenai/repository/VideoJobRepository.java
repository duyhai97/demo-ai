package com.example.backenai.repository;

import com.example.backenai.entity.VideoJobEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VideoJobRepository extends JpaRepository<VideoJobEntity, Long> {

    Optional<VideoJobEntity> findByJobId(String jobId);

    Page<VideoJobEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
}