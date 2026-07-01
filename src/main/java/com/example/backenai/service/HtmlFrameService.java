package com.example.backenai.service;

import com.example.backenai.model.CaptionSegment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HtmlFrameService {

    private final HtmlTemplateService htmlTemplateService;
    private final ChromeScreenshotService chromeScreenshotService;

    public List<String> generateFrames(
            List<String> imagePaths,
            String productName,
            List<CaptionSegment> timeline,
            double audioDuration
    ) throws Exception {

        if (imagePaths == null || imagePaths.isEmpty()) {
            throw new RuntimeException("No images");
        }

        if (timeline == null || timeline.isEmpty()) {
            throw new RuntimeException("No timeline");
        }

        if (audioDuration <= 0) {
            throw new RuntimeException("Invalid audio duration: " + audioDuration);
        }

        List<String> frames = new ArrayList<>();

        int frameCount = timeline.size();

        for (int i = 0; i < frameCount; i++) {
            CaptionSegment caption = timeline.get(i);

            int imageIndex = i % imagePaths.size();

            String imagePath = imagePaths.get(imageIndex);

            String effect = pickEffect(i);

            String htmlPath =
                    htmlTemplateService.createHtml(
                            imagePath,
                            productName,
                            caption.getText(),
                            effect
                    );

            String framePath =
                    chromeScreenshotService.screenshot(htmlPath);

            frames.add(framePath);
        }

        System.out.println("GENERATED FRAME COUNT = " + frames.size());

        return frames;
    }

    private String pickEffect(int index) {
        return switch (index % 4) {
            case 0 -> "zoom_in";
            case 1 -> "pan_left";
            case 2 -> "zoom_out";
            default -> "pan_right";
        };
    }
}