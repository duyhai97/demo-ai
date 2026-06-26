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

        List<String> frames =
                new ArrayList<>();

        double imageDuration =
                audioDuration / imagePaths.size();

        for (int i = 0; i < imagePaths.size(); i++) {

            double currentTime =
                    i * imageDuration;

            String imagePath =
                    imagePaths.get(i);

            CaptionSegment caption =
                    findCaption(
                            timeline,
                            currentTime
                    );

            String htmlPath =
                    htmlTemplateService.createHtml(
                            imagePath,
                            productName,
                            caption.getText()
                    );

            String framePath =
                    chromeScreenshotService.screenshot(
                            htmlPath
                    );

            frames.add(framePath);
        }

        System.out.println("GENERATED FRAME COUNT = " + frames.size());

        return frames;
    }

    private CaptionSegment findCaption(
            List<CaptionSegment> timeline,
            double currentTime
    ) {
        for (CaptionSegment segment : timeline) {
            if (currentTime >= segment.getStart()
                    && currentTime < segment.getEnd()) {
                return segment;
            }
        }

        return timeline.get(timeline.size() - 1);
    }
}