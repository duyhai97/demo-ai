package com.example.backenai.service;

import com.example.backenai.model.VideoPlan;
import com.example.backenai.model.VideoScene;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OpenRouterService {

    private final ObjectMapper objectMapper;

    @Value("${openrouter.api-key}")
    private String apiKey;

    private final RestClient client = RestClient.builder()
            .baseUrl("https://openrouter.ai/api/v1")
            .build();

    public String generate(String productName, String tone, String duration) {
        try {
            Map<String, Object> body = getStringObjectMap(productName);

            String content = callOpenRouter(body);

            if (content == null || content.isBlank()) {
                return "Không nhận được dữ liệu từ OpenRouter";
            }

            return content.trim();

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

        return Map.of(
                "model", "openrouter/auto",
                "messages", List.of(
                        Map.of(
                                "role", "user",
                                "content", prompt
                        )
                ),
                "temperature", 0.7
        );
    }

    public String generateVoiceScript(String productName) {
        try {
            VideoPlan plan = generateVideoPlan(productName, productName, 1);
            return buildVoiceScriptFromPlan(plan);
        } catch (Exception e) {
            e.printStackTrace();
            return fallbackVoiceScript(productName);
        }
    }

    public VideoPlan generateVideoPlan(String productName, String productDescription, int imageCount) {
        try {
            int safeImageCount = Math.max(imageCount, 1);

            String prompt = """
                    Bạn là đạo diễn video TikTok Affiliate Việt Nam.

                    Hãy tạo video plan dạng JSON cho sản phẩm sau:

                    Tên sản phẩm: %s
                    Mô tả sản phẩm: %s
                    Số ảnh người dùng upload: %d

                    Yêu cầu:
                    - Chỉ trả về JSON hợp lệ.
                    - Không markdown.
                    - Không giải thích.
                    - Không viết thêm chữ ngoài JSON.
                    - Video dài 25 đến 35 giây.
                    - Có 5 đến 7 scenes.
                    - Mỗi scene có duration từ 3 đến 6 giây.
                    - imageIndex bắt đầu từ 0 và nhỏ hơn số ảnh upload.
                    - Nếu có nhiều ảnh, hãy phân bổ imageIndex đều giữa các scene.
                    - Voice tiếng Việt tự nhiên, giống người thật review TikTok.
                    - Mỗi voice là 1 câu ngắn, dễ đọc bằng TTS.
                    - Subtitle ngắn hơn voice, dễ đọc trên TikTok.
                    - Hook mạnh ở scene đầu.
                    - CTA tự nhiên ở scene cuối.
                    - Không dùng emoji.
                    - Không hashtag.
                    - Không markdown.
                    - Không nói quá lố.
                    - Không cam kết chữa bệnh.
                    - Không cam kết 100%%.
                    - Không bịa thông số kỹ thuật nếu mô tả không có.
                    - effect chỉ được chọn một trong:
                      zoom_in, zoom_out, pan_left, pan_right

                    JSON format bắt buộc:
                    {
                      "title": "...",
                      "hook": "...",
                      "duration": 30,
                      "scenes": [
                        {
                          "imageIndex": 0,
                          "duration": 4,
                          "voice": "...",
                          "subtitle": "...",
                          "effect": "zoom_in"
                        }
                      ],
                      "cta": "..."
                    }
                    """.formatted(productName, productDescription, safeImageCount);

            Map<String, Object> body = Map.of(
                    "model", "openrouter/auto",
                    "messages", List.of(
                            Map.of(
                                    "role", "user",
                                    "content", prompt
                            )
                    ),
                    "temperature", 0.75
            );

            String json = callOpenRouter(body);

            if (json == null || json.isBlank()) {
                return fallbackVideoPlan(productName, safeImageCount);
            }

            json = cleanJson(json);

            VideoPlan plan = objectMapper.readValue(json, VideoPlan.class);
            validateVideoPlan(plan, safeImageCount);

            return plan;

        } catch (Exception e) {
            e.printStackTrace();
            return fallbackVideoPlan(productName, Math.max(imageCount, 1));
        }
    }

    private String callOpenRouter(Map<String, Object> body) {
        Map response = client.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(body)
                .retrieve()
                .body(Map.class);

        if (response == null) {
            return null;
        }

        List<?> choices = (List<?>) response.get("choices");

        if (choices == null || choices.isEmpty()) {
            return null;
        }

        Map<?, ?> firstChoice = (Map<?, ?>) choices.get(0);
        Map<?, ?> message = (Map<?, ?>) firstChoice.get("message");

        if (message == null) {
            return null;
        }

        Object content = message.get("content");

        return content == null ? null : String.valueOf(content);
    }

    private String cleanJson(String json) {
        String cleaned = json
                .replace("```json", "")
                .replace("```", "")
                .trim();

        int start = cleaned.indexOf("{");
        int end = cleaned.lastIndexOf("}");

        if (start >= 0 && end > start) {
            cleaned = cleaned.substring(start, end + 1);
        }

        return cleaned;
    }

    private String buildVoiceScriptFromPlan(VideoPlan plan) {
        if (plan == null || plan.getScenes() == null || plan.getScenes().isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();

        for (VideoScene scene : plan.getScenes()) {
            if (scene.getVoice() != null && !scene.getVoice().isBlank()) {
                builder.append(scene.getVoice().trim()).append(" ");
            }
        }

        return builder.toString().trim();
    }

    private void validateVideoPlan(VideoPlan plan, int imageCount) {
        if (plan == null || plan.getScenes() == null || plan.getScenes().isEmpty()) {
            throw new RuntimeException("VideoPlan scenes is empty");
        }

        if (plan.getScenes().size() > 7) {
            plan.setScenes(plan.getScenes().subList(0, 7));
        }

        for (int i = 0; i < plan.getScenes().size(); i++) {
            VideoScene scene = plan.getScenes().get(i);

            if (scene.getImageIndex() == null || scene.getImageIndex() < 0 || scene.getImageIndex() >= imageCount) {
                scene.setImageIndex(i % imageCount);
            }

            if (scene.getDuration() == null || scene.getDuration() < 3 || scene.getDuration() > 6) {
                scene.setDuration(5);
            }

            if (scene.getEffect() == null || scene.getEffect().isBlank()) {
                scene.setEffect("zoom_in");
            }

            if (!List.of("zoom_in", "zoom_out", "pan_left", "pan_right").contains(scene.getEffect())) {
                scene.setEffect("zoom_in");
            }

            if (scene.getVoice() == null || scene.getVoice().isBlank()) {
                scene.setVoice(scene.getSubtitle());
            }

            if (scene.getSubtitle() == null || scene.getSubtitle().isBlank()) {
                scene.setSubtitle(scene.getVoice());
            }

            if (scene.getVoice() == null || scene.getVoice().isBlank()) {
                scene.setVoice("Sản phẩm này rất tiện lợi cho nhu cầu hằng ngày.");
            }

            if (scene.getSubtitle() == null || scene.getSubtitle().isBlank()) {
                scene.setSubtitle("Tiện lợi hằng ngày");
            }
        }
    }

    private VideoPlan fallbackVideoPlan(String productName, int imageCount) {
        int safeImageCount = Math.max(imageCount, 1);

        VideoPlan plan = new VideoPlan();
        plan.setTitle(productName);
        plan.setHook("Khoan lướt, sản phẩm này khá đáng để xem thử.");
        plan.setDuration(30);
        plan.setCta("Bấm vào giỏ hàng để xem chi tiết hôm nay.");

        VideoScene s1 = scene(0 % safeImageCount, 4,
                "Khoan lướt, nếu bạn đang tìm một sản phẩm tiện lợi thì xem thử món này.",
                "Khoan lướt",
                "zoom_in");

        VideoScene s2 = scene(1 % safeImageCount, 5,
                "Sản phẩm này phù hợp cho nhu cầu sử dụng hằng ngày, đơn giản và dễ dùng.",
                "Dễ dùng hằng ngày",
                "pan_left");

        VideoScene s3 = scene(2 % safeImageCount, 5,
                "Điểm mình thích là thiết kế gọn, không chiếm nhiều diện tích khi sử dụng.",
                "Thiết kế gọn",
                "zoom_out");

        VideoScene s4 = scene(3 % safeImageCount, 5,
                "Dùng trong nhà, mang theo khi cần hoặc làm quà đều khá hợp lý.",
                "Dùng rất linh hoạt",
                "pan_right");

        VideoScene s5 = scene(4 % safeImageCount, 5,
                "Nếu bạn thích sự nhanh gọn và tiện lợi thì mẫu này rất đáng để tham khảo.",
                "Đáng tham khảo",
                "zoom_in");

        VideoScene s6 = scene(5 % safeImageCount, 4,
                "Bấm vào giỏ hàng để xem chi tiết hôm nay nhé.",
                "Xem ở giỏ hàng",
                "zoom_in");

        plan.setScenes(List.of(s1, s2, s3, s4, s5, s6));

        return plan;
    }

    private VideoScene scene(Integer imageIndex, Integer duration, String voice, String subtitle, String effect) {
        VideoScene scene = new VideoScene();
        scene.setImageIndex(imageIndex);
        scene.setDuration(duration);
        scene.setVoice(voice);
        scene.setSubtitle(subtitle);
        scene.setEffect(effect);
        return scene;
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