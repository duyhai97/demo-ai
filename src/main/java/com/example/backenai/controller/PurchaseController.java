package com.example.backenai.controller;

import com.example.backenai.model.PurchasePageResponse;
import com.example.backenai.model.PurchaseQrResponse;
import com.example.backenai.service.PurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;

    @PostMapping("/video10/qr")
    public PurchaseQrResponse generateVideo10Qr(Authentication authentication) {
        return purchaseService.generateQr(authentication.getName());
    }

    @GetMapping
    public PurchasePageResponse myPurchases(
            Pageable pageable,
            Authentication authentication) {
        return purchaseService.findByUsername(authentication.getName(), pageable);
    }

    @GetMapping("/{orderId}")
    public PurchaseQrResponse getMyPurchase(
            @PathVariable String orderId,
            Authentication authentication) {
        return purchaseService.findOneForUser(authentication.getName(), orderId);
    }

}