package com.example.backenai.service;

import com.example.backenai.entity.PurchaseOrderEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PurchaseNotificationService {

    private final JavaMailSender mailSender;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.review-url}")
    private String reviewUrl;

    public void sendNewPurchaseToAdmin(PurchaseOrderEntity order) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(adminEmail);
        message.setSubject("Có đơn mua lượt video mới");

        message.setText("""
                Có user vừa tạo đơn mua lượt video.

                User: %s
                Gói: %s
                Số video cộng thêm: %s
                Số tiền: %,d VND
                Nội dung chuyển khoản: %s
                Trạng thái: %s

                Link duyệt:
                %s
                """.formatted(
                order.getUsername(),
                order.getPackageCode(),
                order.getExtraVideos(),
                order.getAmount(),
                order.getTransferContent(),
                order.getStatus(),
                reviewUrl
        ));

        mailSender.send(message);
    }
}