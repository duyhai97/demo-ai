package com.example.backenai.worker.step;

import com.example.backenai.model.VideoJob;
import com.example.backenai.service.JobService;
import com.example.backenai.service.TtsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GenerateVoiceStep implements VideoStep {

    private final TtsService ttsService;
    private final JobService jobService;

    @Override
    public void execute(VideoJob job) throws Exception {

        if (job.getScript() == null || job.getScript().isBlank()) {
            throw new RuntimeException("Script is empty before TTS");
        }

        String voicePath =
                ttsService.generate(job.getScript());

        double voiceDuration =
                ttsService.getAudioDurationSeconds(voicePath);

        if (voiceDuration <= 0) {
            throw new RuntimeException("Invalid voice duration: " + voiceDuration);
        }

        job.setVoicePath(voicePath);
        jobService.save(job);

        System.out.println("VOICE = " + voicePath);
        System.out.println("VOICE DURATION = " + voiceDuration);
    }
}