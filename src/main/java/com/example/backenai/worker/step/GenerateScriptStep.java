package com.example.backenai.worker.step;

import com.example.backenai.model.VideoJob;
import com.example.backenai.service.JobService;
import com.example.backenai.service.OpenRouterService;
import com.example.backenai.service.ScriptCleanerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GenerateScriptStep implements VideoStep {

    private final OpenRouterService openRouterService;
    private final ScriptCleanerService cleaner;
    private final JobService jobService;

    @Override
    public void execute(VideoJob job) throws Exception {

        String raw =
                openRouterService.generateVoiceScript(
                        job.getProductName()
                );

        String script =
                cleaner.clean(raw);

        job.setScript(script);

        jobService.save(job);
    }
}