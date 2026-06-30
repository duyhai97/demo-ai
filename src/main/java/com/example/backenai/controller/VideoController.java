package com.example.backenai.controller;

import com.example.backenai.constant.JobStatus;
import com.example.backenai.model.CreateVideoRequest;
import com.example.backenai.model.VideoJob;
import com.example.backenai.model.VideoQuotaResponse;
import com.example.backenai.queue.JobQueue;
import com.example.backenai.service.JobService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/videos")
@CrossOrigin(origins = "*")
public class VideoController {

    private final JobService jobService;
    private final JobQueue queue;

    public VideoController(JobService jobService, JobQueue queue) {
        this.jobService = jobService;
        this.queue = queue;
    }

    @PostMapping
    public VideoJob createVideo(
            @RequestBody CreateVideoRequest request,
            Authentication authentication
    ) {

        jobService.validateDailyVideoLimit(authentication);
        VideoJob job = new VideoJob();

        job.setJobId(UUID.randomUUID().toString());
        job.setProductName(request.getProductName());
        job.setAffiliateLink(request.getAffiliateLink());
        job.setImagePaths(request.getImagePaths());
        job.setCreatedBy(authentication == null ? "unknown" : authentication.getName());

        job.setStatus(JobStatus.PENDING);
        job.setProgress(0);
        job.setCurrentStep("Đang chờ xử lý");
        jobService.save(job);

        queue.push(job);

        return job;
    }

    @GetMapping("/{jobId}")
    public VideoJob get(@PathVariable String jobId) {
        return jobService.get(jobId);
    }

    @GetMapping
    public Page<VideoJob> getVideos(Pageable pageable, Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(a ->
                        a.getAuthority().equals("ADMIN")
                                || a.getAuthority().equals("ROLE_ADMIN")
                );
        if (isAdmin) {
            return jobService.findAll(pageable);
        } else {
            return jobService.findByCreatedBy(authentication.getName(), pageable);
        }
    }

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public Map<String, Object> uploadImages(
            @RequestParam("files") List<MultipartFile> files
    ) throws Exception {

        String uploadDir = System.getProperty("user.dir") + "/storage/images";

        File dir = new File(uploadDir);

        if (!dir.exists()) {
            dir.mkdirs();
        }

        List<String> imagePaths = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            String originalName = file.getOriginalFilename();
            String ext = ".jpg";

            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }

            String fileName = UUID.randomUUID() + ext;
            File target = new File(dir, fileName);

            file.transferTo(target);

            imagePaths.add(target.getAbsolutePath());
        }

        return Map.of(
                "count", imagePaths.size(),
                "imagePaths", imagePaths
        );
    }

    @GetMapping("/quota")
    public VideoQuotaResponse quota(Authentication authentication) {
        return jobService.getQuota(authentication);
    }
}