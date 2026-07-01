package com.example.backenai.worker.step;

import com.example.backenai.model.VideoJob;
import com.example.backenai.model.VideoPlan;
import com.example.backenai.model.VideoScene;
import com.example.backenai.service.JobService;
import com.example.backenai.service.OpenRouterService;
import com.example.backenai.service.ScriptCleanerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GenerateScriptStep implements VideoStep {

    private final OpenRouterService openRouterService;
    private final ScriptCleanerService cleaner;
    private final JobService jobService;
    private final ObjectMapper objectMapper;

    @Override
    public void execute(VideoJob job) throws Exception {

        int imageCount = job.getImagePaths() == null ? 1 : Math.max(job.getImagePaths().size(), 1);

        VideoPlan plan = openRouterService.generateVideoPlan(
                job.getProductName(),
                job.getProductName(),
                imageCount
        );

        String script = plan.getScenes()
                .stream()
                .map(VideoScene::getVoice)
                .filter(text -> text != null && !text.isBlank())
                .collect(Collectors.joining(" "));

        script = cleaner.clean(script);

        job.setScript(script);
        job.setVideoPlanJson(objectMapper.writeValueAsString(plan));

        jobService.save(job);
    }
}