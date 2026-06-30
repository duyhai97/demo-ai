package com.example.backenai.repository;

import com.example.backenai.entity.UploadedImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UploadedImageRepository extends JpaRepository<UploadedImageEntity, Long> {

    List<UploadedImageEntity> findByJobIdOrderBySortOrderAsc(String jobId);

    void deleteByJobId(String jobId);
}