package com.example.backenai.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "purchase_order")
@Getter
@Setter
public class PurchaseOrderEntity {

    @Id
    private String id;

    private String username;

    private String packageCode;

    private Integer extraVideos;

    private Long amount;

    private String transferContent;

    private String status; // PENDING, PAID, CANCELLED

    private LocalDateTime createdAt;
}