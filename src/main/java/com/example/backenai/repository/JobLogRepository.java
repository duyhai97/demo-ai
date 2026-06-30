package com.example.backenai.repository;

import com.example.backenai.entity.JobLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobLogRepository extends JpaRepository<JobLogEntity, Long> {

    List<JobLogEntity> findByJobIdOrderByCreatedAtAsc(String jobId);
}