package com.example.backenai.service;

import com.example.backenai.entity.PurchaseOrderEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class ResendEmailService {

    @Value("${resend.api-key}")
    private String resendApiKey;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.review-url}")
    private String reviewUrl;

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://api.resend.com")
            .build();

    public void sendNewPurchaseToAdmin(PurchaseOrderEntity order) {
        if (resendApiKey == null || resendApiKey.isBlank()) {
            System.err.println("RESEND_API_KEY is empty, skip send email");
            return;
        }

        if (adminEmail == null || adminEmail.isBlank()) {
            System.err.println("APP_ADMIN_EMAIL is empty, skip send email");
            return;
        }

        String body = """
                {
                  "from": "TikTok AI <onboarding@resend.dev>",
                  "to": ["%s"],
                  "subject": "Có đơn mua lượt video mới",
                  "html": "%s"
                }
                """.formatted(adminEmail, buildHtml(order));

        restClient.post()
                .uri("/emails")
                .header("Authorization", "Bearer " + resendApiKey)
                .header("Content-Type", "application/json")
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    private String buildHtml(PurchaseOrderEntity order) {
        return """
                <h2>Có đơn mua lượt video mới</h2>
                <p><b>User:</b> %s</p>
                <p><b>Gói:</b> %s</p>
                <p><b>Số video cộng thêm:</b> %s</p>
                <p><b>Số tiền:</b> %,d VND</p>
                <p><b>Nội dung CK:</b> %s</p>
                <p><b>Trạng thái:</b> %s</p>
                <p>
                    <a href="%s">Mở màn duyệt đơn</a>
                </p>
                """.formatted(
                order.getUsername(),
                order.getPackageCode(),
                order.getExtraVideos(),
                order.getAmount(),
                order.getTransferContent(),
                order.getStatus(),
                reviewUrl
        ).replace("\"", "\\\"");
    }
}