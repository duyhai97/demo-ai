package com.example.backenai.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/uploads")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UploadController {

    private static final String IMAGE_DIR = "storage/images";

    @PostMapping("/images")
    public Map<String, Object> uploadImages(
            @RequestParam("images") List<MultipartFile> images
    ) throws Exception {

        File dir = new File(IMAGE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        List<String> imagePaths = new ArrayList<>();

        for (MultipartFile image : images) {
            if (image.isEmpty()) {
                continue;
            }

            String originalName = image.getOriginalFilename();
            String ext = ".jpg";

            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }

            String fileName = UUID.randomUUID() + ext;
            File outputFile = new File(dir, fileName);

            Files.copy(image.getInputStream(), outputFile.toPath());

            imagePaths.add(outputFile.getPath());
        }

        return Map.of(
                "count", imagePaths.size(),
                "images", imagePaths
        );
    }
}