package com.example.backenai.model;

import com.example.backenai.entity.PurchaseOrderEntity;

import java.util.List;

public record PurchasePageResponse(
        List<PurchaseOrderEntity> content,
        int number,
        int size,
        int totalPages,
        long totalElements,
        boolean first,
        boolean last
) {
}