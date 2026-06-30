package com.example.backenai.controller;

import com.example.backenai.entity.PurchaseOrderEntity;
import com.example.backenai.model.PurchasePageResponse;
import com.example.backenai.repository.PurchaseOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin/purchases")
@RequiredArgsConstructor
public class AdminPurchaseController {

    private final PurchaseOrderRepository purchaseOrderRepository;

    @GetMapping
    public PurchasePageResponse listPurchases(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status
    ) {

        Pageable pageable = PageRequest.of(page, size);

        Page<PurchaseOrderEntity> result;

        if (status == null || status.isBlank()) {
            result = purchaseOrderRepository.findAllByOrderByCreatedAtDesc(pageable);
        } else {
            result = purchaseOrderRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        }

        return new PurchasePageResponse(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalPages(),
                result.getTotalElements(),
                result.isFirst(),
                result.isLast()
        );
    }

    @PutMapping("/{orderId}/paid")
    public PurchaseOrderEntity markPaid(@PathVariable String orderId) {
        PurchaseOrderEntity order = purchaseOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Order not found"
                ));

        if ("PAID".equals(order.getStatus())) {
            return order;
        }

        order.setStatus("PAID");

        return purchaseOrderRepository.save(order);
    }
}