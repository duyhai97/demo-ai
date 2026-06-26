package com.example.backenai.worker.step;

import com.example.backenai.model.VideoJob;
import com.example.backenai.service.JobService;
import com.example.backenai.service.VideoRenderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@RequiredArgsConstructor
public class RenderVideoStep implements VideoStep {

    private final VideoRenderService videoRenderService;
    private final JobService jobService;

    @Override
    public void execute(VideoJob job) throws Exception {

        if (job.getFramePaths() == null || job.getFramePaths().isEmpty()) {
            throw new RuntimeException("Frame paths is empty before render");
        }

        if (job.getVoicePath() == null || job.getVoicePath().isBlank()) {
            throw new RuntimeException("Voice path is empty before render");
        }

        String videoPath =
                videoRenderService.render(
                        job.getFramePaths(),
                        job.getVoicePath()
                );

        job.setVideoPath(videoPath);

        String fileName =
                new File(videoPath).getName();

        job.setVideoUrl("/videos/" + fileName);

        jobService.save(job);

        System.out.println("VIDEO = " + videoPath);
    }
}