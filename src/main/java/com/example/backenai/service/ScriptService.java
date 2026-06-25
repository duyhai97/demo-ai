package com.example.backenai.service;

import org.springframework.stereotype.Service;

@Service
public class ScriptService {

    public String generateScript(
            String productName
    ) {

        return """
                Bạn vẫn đang tìm một %s tiện lợi?

                Thiết kế nhỏ gọn.

                Dễ mang theo mọi nơi.

                Giá đang rất tốt.

                Bấm giỏ hàng để xem ngay.
                """
                .formatted(productName);
    }
}