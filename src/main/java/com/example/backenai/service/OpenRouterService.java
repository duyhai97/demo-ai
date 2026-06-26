package com.example.backenai.service;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class OpenRouterService {

    private String apiKey = "abcd";

    private final RestClient client = RestClient.builder()
            .baseUrl("https://openrouter.ai/api/v1")
            .build();

    public String generate(String productName, String tone, String duration) {

        try {

            Map<String, Object> body = getStringObjectMap(productName);

            Map response = client.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            List<?> choices = (List<?>) response.get("choices");

            if (choices == null || choices.isEmpty()) {
                return "Không nhận được dữ liệu từ OpenRouter";
            }

            Map<?, ?> firstChoice = (Map<?, ?>) choices.get(0);
            Map<?, ?> message = (Map<?, ?>) firstChoice.get("message");

            return String.valueOf(message.get("content"));

        } catch (Exception e) {
            e.printStackTrace();
            return "Lỗi: " + e.getMessage();
        }
    }

    private static @NonNull Map<String, Object> getStringObjectMap(String productName) {
        String prompt = """
                Bạn là TikTok Affiliate Creator.
                
                Viết lời thoại cho video TikTok.
                
                Yêu cầu:
                
                - Tiếng Việt tự nhiên.
                - Khoảng 40-60 từ.
                - 10-15 giây.
                - Có Hook.
                - Có CTA.
                - Không markdown.
                - Không tiêu đề.
                - Không đánh số.
                - Chỉ trả về đúng lời thoại.
                
                Sản phẩm:
                %s
                """.formatted(productName);

        Map<String, Object> body = Map.of(
                "model", "openrouter/auto",
                "messages", List.of(
                        Map.of(
                                "role", "user",
                                "content", prompt
                        )
                ),
                "temperature", 0.7
        );
        return body;
    }

    public String generateVoiceScript(String productName) {

        try {

            String prompt = """
                    Bạn là TikTok Affiliate Creator hàng đầu Việt Nam.
                    
                    Viết lời thoại ngắn để đọc voice cho video TikTok Affiliate.
                    
                    Sản phẩm: %s
                    
                    Yêu cầu bắt buộc:
                    - 100%% tiếng Việt tự nhiên.
                    - Không dùng markdown.
                    - Không đánh số.
                    - Không hashtag.
                    - Không tiêu đề.
                    - Không emoji.
                    - Không dùng dấu ngoặc kép.
                    - Không dùng từ quá lóng như xịn sò, lổm ngổm, đỉnh của chóp.
                    - Không bịa thông số sản phẩm.
                    - Văn phong bán hàng TikTok tự nhiên.
                    - Độ dài 5 đến 7 câu ngắn.
                    - Tổng khoảng 45 đến 70 từ.
                    - Có hook mở đầu.
                    - Có CTA cuối video.
                    - Chỉ trả về lời thoại để đọc.
                    
                    Ví dụ style:
                    Bạn đang tìm một sản phẩm tiện lợi cho việc sử dụng hằng ngày.
                    Mẫu này nhỏ gọn, dễ dùng và phù hợp mang theo.
                    Nếu bạn thích sự nhanh gọn thì rất đáng để xem thử.
                    Bấm vào giỏ hàng để xem chi tiết hôm nay.
                    
                    Chỉ trả về nội dung lời thoại.
                    """.formatted(productName);

            Map<String, Object> body = Map.of(
                    "model", "openrouter/auto",
                    "messages", List.of(
                            Map.of(
                                    "role", "user",
                                    "content", prompt
                            )
                    ),
                    "temperature", 0.6
            );

            Map response = client.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            List<?> choices = (List<?>) response.get("choices");

            if (choices == null || choices.isEmpty()) {
                return fallbackVoiceScript(productName);
            }

            Map<?, ?> firstChoice = (Map<?, ?>) choices.get(0);
            Map<?, ?> message = (Map<?, ?>) firstChoice.get("message");

            String content = String.valueOf(message.get("content"));

            if (content == null || content.isBlank()) {
                return fallbackVoiceScript(productName);
            }

            return content.trim();

        } catch (Exception e) {
            e.printStackTrace();
            return fallbackVoiceScript(productName);
        }
    }

    private String fallbackVoiceScript(String productName) {

        return """
                            Bạn đang tìm một %s tiện lợi cho nhu cầu hằng ngày.
                            Sản phẩm này nhỏ gọn và dễ sử dụng.
                            Phù hợp để dùng nhanh tại nhà hoặc mang theo khi cần.
                            Nếu bạn thích sự gọn nhẹ thì có thể xem thử mẫu này.
                            Bấm vào giỏ hàng để xem chi tiết hôm nay.
                """.formatted(productName);
    }
}