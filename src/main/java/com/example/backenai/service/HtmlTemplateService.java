package com.example.backenai.service;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class HtmlTemplateService {

    public String createHtml(
            String imagePath,
            String productName,
            String caption
    ) throws Exception {

        Files.createDirectories(Path.of("storage/html"));

        String htmlPath =
                "storage/html/frame_"
                        + UUID.randomUUID()
                        + ".html";

        String imageUrl =
                Path.of(imagePath)
                        .toAbsolutePath()
                        .toUri()
                        .toString();

        String safeProductName =
                escape(productName);

        String safeCaption =
                escape(caption);

        String html = """
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<style>
* {
  box-sizing: border-box;
}

html, body {
  margin: 0;
  width: 1080px;
  height: 1920px;
  overflow: hidden;
  font-family: Arial, Helvetica, sans-serif;
  background: #020617;
}

.page {
  position: relative;
  width: 1080px;
  height: 1920px;
  overflow: hidden;
  background:
    radial-gradient(circle at 20%% 10%%, rgba(255,0,80,0.32), transparent 34%%),
    radial-gradient(circle at 85%% 0%%, rgba(0,242,234,0.22), transparent 32%%),
    linear-gradient(180deg, #111827 0%%, #020617 100%%);
}

.bg {
  position: absolute;
  inset: -80px;
  background-image: url('%s');
  background-size: cover;
  background-position: center;
  filter: blur(42px) saturate(1.25);
  transform: scale(1.2);
  opacity: 0.5;
}

.vignette {
  position: absolute;
  inset: 0;
  background:
    linear-gradient(180deg, rgba(0,0,0,0.15), rgba(0,0,0,0.12) 45%%, rgba(0,0,0,0.72)),
    radial-gradient(circle at center, transparent 38%%, rgba(0,0,0,0.55));
}

.card {
  position: absolute;
  top: 165px;
  left: 80px;
  width: 920px;
  height: 1040px;
  border-radius: 64px;
  background: rgba(255,255,255,0.10);
  backdrop-filter: blur(14px);
  box-shadow:
    0 50px 130px rgba(0,0,0,0.62),
    inset 0 0 0 2px rgba(255,255,255,0.18);
  overflow: hidden;
  border: 2px solid rgba(255,255,255,0.20);
}

.product {
  width: 100%%;
  height: 100%%;
  object-fit: cover;
  transform: scale(1.06) translateY(-8px);
}

.shine {
  position: absolute;
  inset: 0;
  background:
    linear-gradient(115deg, transparent 0%%, transparent 38%%, rgba(255,255,255,0.18) 48%%, transparent 60%%);
  opacity: 0.55;
}

.top-row {
  position: absolute;
  top: 72px;
  left: 72px;
  right: 72px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.badge {
  padding: 22px 38px;
  border-radius: 999px;
  background: linear-gradient(90deg, #ff0050, #ff7a00);
  color: white;
  font-size: 46px;
  font-weight: 950;
  letter-spacing: -1px;
  box-shadow:
    0 14px 44px rgba(255,0,80,0.5),
    inset 0 0 0 2px rgba(255,255,255,0.18);
}

.tag {
  padding: 18px 30px;
  border-radius: 999px;
  background: rgba(0,0,0,0.45);
  color: #e0f2fe;
  font-size: 34px;
  font-weight: 900;
  border: 1px solid rgba(255,255,255,0.18);
  box-shadow: 0 10px 32px rgba(0,0,0,0.35);
}

.title-wrap {
  position: absolute;
  top: 1235px;
  left: 62px;
  right: 62px;
  padding: 28px 36px;
  border-radius: 42px;
  background: linear-gradient(180deg, rgba(15,23,42,0.72), rgba(2,6,23,0.58));
  border: 1px solid rgba(255,255,255,0.12);
  box-shadow: 0 24px 70px rgba(0,0,0,0.42);
}

.title {
  color: white;
  font-size: 68px;
  font-weight: 950;
  line-height: 1.08;
  text-align: center;
  letter-spacing: -1.8px;
  text-shadow:
    0 6px 0 rgba(0,0,0,0.55),
    0 14px 32px rgba(0,0,0,0.95);
}

.caption {
  position: absolute;
  top: 1425px;
  left: 70px;
  right: 70px;
  min-height: 190px;
  padding: 26px 32px;
  border-radius: 38px;
  color: white;
  font-size: 56px;
  font-weight: 900;
  line-height: 1.16;
  text-align: center;
  background: rgba(0,0,0,0.42);
  border: 1px solid rgba(255,255,255,0.16);
  text-shadow:
    0 5px 0 #000,
    0 10px 28px rgba(0,0,0,0.9);
  box-shadow: 0 20px 70px rgba(0,0,0,0.38);
}

.caption strong {
  color: #fde047;
}

.cta {
  position: absolute;
  left: 150px;
  right: 150px;
  bottom: 148px;
  height: 136px;
  border-radius: 76px;
  background: linear-gradient(90deg, #ff0050, #ff7a00);
  color: white;
  font-size: 56px;
  font-weight: 950;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow:
    0 24px 70px rgba(255,0,80,0.55),
    inset 0 0 0 2px rgba(255,255,255,0.2);
  text-shadow: 0 4px 16px rgba(0,0,0,0.45);
}

.arrow {
  display: inline-block;
  margin-right: 14px;
  font-size: 66px;
}

.progress {
  position: absolute;
  left: 80px;
  right: 80px;
  bottom: 66px;
  height: 16px;
  border-radius: 99px;
  background: rgba(255,255,255,0.18);
  overflow: hidden;
  box-shadow: inset 0 0 0 1px rgba(255,255,255,0.08);
}

.progress-inner {
  width: 72%%;
  height: 100%%;
  border-radius: 99px;
  background: linear-gradient(90deg, #22c55e, #06b6d4, #fff);
}

.corner {
  position: absolute;
  width: 180px;
  height: 180px;
  border: 5px solid rgba(255,255,255,0.32);
}

.corner.left {
  left: 42px;
  top: 132px;
  border-right: 0;
  border-bottom: 0;
  border-radius: 42px 0 0 0;
}

.corner.right {
  right: 42px;
  top: 132px;
  border-left: 0;
  border-bottom: 0;
  border-radius: 0 42px 0 0;
}
</style>
</head>
<body>
<div class="page">
  <div class="bg"></div>
  <div class="vignette"></div>

  <div class="corner left"></div>
  <div class="corner right"></div>

  <div class="card">
    <img class="product" src="%s"/>
    <div class="shine"></div>
  </div>

  <div class="top-row">
    <div class="badge">🔥 HOT</div>
    <div class="tag">TikTok Pick</div>
  </div>

  <div class="title-wrap">
    <div class="title">%s</div>
  </div>

  <div class="caption">%s</div>

  <div class="cta"><span class="arrow">👇</span> Xem ở giỏ hàng</div>

  <div class="progress">
    <div class="progress-inner"></div>
  </div>
</div>
</body>
</html>
""".formatted(
                imageUrl,
                imageUrl,
                safeProductName,
                highlightCaption(safeCaption)
        );

        Files.writeString(
                Path.of(htmlPath),
                html,
                StandardCharsets.UTF_8
        );

        return htmlPath;
    }

    private String highlightCaption(String caption) {
        if (caption == null || caption.isBlank()) {
            return "";
        }

        String[] words = caption.split(" ");

        if (words.length < 4) {
            return caption;
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            if (i == 0 || i == 1) {
                sb.append("<strong>")
                        .append(words[i])
                        .append("</strong>");
            } else {
                sb.append(words[i]);
            }

            if (i < words.length - 1) {
                sb.append(" ");
            }
        }

        return sb.toString();
    }

    private String escape(String s) {
        if (s == null) {
            return "";
        }

        return s
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}