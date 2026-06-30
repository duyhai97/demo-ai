package com.example.backenai.model;

public record VideoQuotaResponse(
        int dailyLimit,
        long usedToday,
        long extraToday,
        long totalToday,
        long remainingToday
) {
}