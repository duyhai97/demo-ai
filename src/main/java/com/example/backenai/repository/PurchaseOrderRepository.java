package com.example.backenai.repository;

import com.example.backenai.entity.PurchaseOrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrderEntity, String> {

    Page<PurchaseOrderEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<PurchaseOrderEntity> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);

    @Query("""
        select coalesce(sum(p.extraVideos), 0)
        from PurchaseOrderEntity p
        where p.username = :username
          and p.status = 'PAID'
          and p.createdAt >= :start
          and p.createdAt < :end
    """)
    long sumPaidExtraVideosToday(
            String username,
            LocalDateTime start,
            LocalDateTime end
    );

    Page<PurchaseOrderEntity> findByUsernameOrderByCreatedAtDesc(
            String username,
            Pageable pageable
    );
}