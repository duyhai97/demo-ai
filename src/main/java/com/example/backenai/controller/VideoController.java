package com.example.backenai.controller;

import com.example.backenai.constant.JobStatus;
import com.example.backenai.model.CreateVideoRequest;
import com.example.backenai.model.VideoJob;
import com.example.backenai.queue.JobQueue;
import com.example.backenai.service.JobService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/videos")
public class VideoController {

    private final JobService jobService;
    private final JobQueue queue;

    public VideoController(
            JobService jobService,
            JobQueue queue
    ) {
        this.jobService = jobService;
        this.queue = queue;
    }

    @PostMapping
    public VideoJob createVideo(@RequestBody CreateVideoRequest request) {

        VideoJob job = new VideoJob();

        job.setJobId(UUID.randomUUID().toString());
        job.setProductName(request.getProductName());
        job.setAffiliateLink(request.getAffiliateLink());
        job.setImagePaths(request.getImagePaths());

        job.setStatus(JobStatus.PENDING);
        job.setProgress(0);
        job.setCurrentStep("Đang chờ xử lý");

        jobService.save(job);

        System.out.println("PUSH JOB TO QUEUE = " + job.getJobId());

        queue.push(job);

        return job;
    }

    @GetMapping("/{jobId}")
    public VideoJob get(
            @PathVariable String jobId
    ) {
        return jobService.get(jobId);
    }

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public Map<String, String> upload(
            @RequestParam("file")
            MultipartFile file
    ) throws Exception {

        String uploadDir =
                System.getProperty("user.dir")
                        + "/storage/images";

        File dir = new File(uploadDir);

        if (!dir.exists()) {
            dir.mkdirs();
        }

        String fileName =
                UUID.randomUUID()
                        + ".jpg";

        File target =
                new File(dir, fileName);

        file.transferTo(target);

        System.out.println(
                "UPLOAD SUCCESS = "
                        + target.getAbsolutePath()
        );

        return Map.of(
                "imagePath",
                target.getAbsolutePath()
        );
    }
}