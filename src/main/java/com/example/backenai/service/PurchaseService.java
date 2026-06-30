package com.example.backenai.service;

import com.example.backenai.entity.PurchaseOrderEntity;
import com.example.backenai.model.PurchasePageResponse;
import com.example.backenai.model.PurchaseProperties;
import com.example.backenai.model.PurchaseQrResponse;
import com.example.backenai.repository.PurchaseOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseProperties purchaseProperties;
    private final PurchaseNotificationService purchaseNotificationService;
    private final ResendEmailService resendEmailService;

    public PurchaseQrResponse generateQr(String username) {
        String orderId = UUID.randomUUID().toString();

        int extraVideos = purchaseProperties.getVideo10().getExtraVideos();
        long amount = purchaseProperties.getVideo10().getAmount();

        String transferContent = "BUYVIDEO " + username + " " + orderId.substring(0, 8);

        PurchaseOrderEntity entity = new PurchaseOrderEntity();
        entity.setId(orderId);
        entity.setUsername(username);
        entity.setPackageCode("VIDEO_10");
        entity.setExtraVideos(extraVideos);
        entity.setAmount(amount);
        entity.setTransferContent(transferContent);
        entity.setStatus("PENDING");
        entity.setCreatedAt(LocalDateTime.now());

        PurchaseOrderEntity saved = purchaseOrderRepository.save(entity);

        try {
            resendEmailService.sendNewPurchaseToAdmin(saved);
        } catch (Exception e) {
            System.err.println("Send purchase email failed: " + e.getMessage());
        }

        return toQrResponse(saved);
    }

    public PurchasePageResponse findByUsername(String username, Pageable pageable) {
        Page<PurchaseOrderEntity> page =
                purchaseOrderRepository.findByUsernameOrderByCreatedAtDesc(username, pageable);

        return new PurchasePageResponse(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.isFirst(),
                page.isLast()
        );
    }

    public PurchaseQrResponse findOneForUser(String username, String orderId) {
        PurchaseOrderEntity order = purchaseOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Order not found"
                ));

        if (!username.equals(order.getUsername())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Bạn không có quyền xem đơn này"
            );
        }

        return toQrResponse(order);
    }

    private PurchaseQrResponse toQrResponse(PurchaseOrderEntity order) {
        String qrUrl = buildQrUrl(
                order.getAmount(),
                order.getTransferContent()
        );

        return new PurchaseQrResponse(
                order.getId(),
                order.getPackageCode(),
                order.getExtraVideos(),
                order.getAmount(),
                order.getTransferContent(),
                qrUrl
        );
    }

    private String buildQrUrl(Long amount, String transferContent) {
        return "https://img.vietqr.io/image/"
                + purchaseProperties.getBank().getBin()
                + "-"
                + purchaseProperties.getBank().getAccountNo()
                + "-compact2.png"
                + "?amount=" + amount
                + "&addInfo=" + URLEncoder.encode(
                transferContent,
                StandardCharsets.UTF_8
        )
                + "&accountName=" + URLEncoder.encode(
                purchaseProperties.getBank().getAccountName(),
                StandardCharsets.UTF_8
        );
    }
}