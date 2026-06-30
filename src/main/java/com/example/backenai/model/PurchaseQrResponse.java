package com.example.backenai.model;

public record PurchaseQrResponse(
        String orderId,
        String packageCode,
        Integer extraVideos,
        Long amount,
        String transferContent,
        String qrUrl
) {
}