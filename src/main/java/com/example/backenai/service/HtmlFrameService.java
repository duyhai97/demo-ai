package com.example.backenai.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HtmlFrameService {

    private final HtmlTemplateService htmlTemplateService;
    private final ChromeScreenshotService chromeScreenshotService;

    public List<String> generateFrames(List<String> imagePaths, String productName, List<String> captions) throws Exception {

        if (imagePaths == null || imagePaths.isEmpty()) {
            throw new RuntimeException("No images");
        }

        if (captions == null || captions.isEmpty()) {
            throw new RuntimeException("No captions");
        }

        List<String> frames = new ArrayList<>();

        for (int i = 0; i < imagePaths.size(); i++) {

            String imagePath = imagePaths.get(i);

            String caption = captions.get(Math.min(i, captions.size() - 1));

            String htmlPath = htmlTemplateService.createHtml(imagePath, productName, caption);

            String framePath = chromeScreenshotService.screenshot(htmlPath);

            frames.add(framePath);
        }

        System.out.println("GENERATED FRAME COUNT = " + frames.size());

        return frames;
    }
}