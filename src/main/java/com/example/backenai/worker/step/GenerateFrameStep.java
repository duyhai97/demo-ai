package com.example.backenai.worker.step;

import com.example.backenai.model.CaptionSegment;
import com.example.backenai.model.VideoJob;
import com.example.backenai.service.HtmlFrameService;
import com.example.backenai.service.JobService;
import com.example.backenai.service.ScriptSegmentService;
import com.example.backenai.service.TtsService;
import com.example.backenai.service.subtitle.SubtitleService;
import com.example.backenai.service.subtitle.SubtitleTimelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GenerateFrameStep implements VideoStep {

    private final ScriptSegmentService scriptSegmentService;
    private final HtmlFrameService htmlFrameService;
    private final JobService jobService;
    private final TtsService ttsService;
    private final SubtitleTimelineService subtitleTimelineService;
    private final SubtitleService subtitleService;

    @Override
    public void execute(VideoJob job) throws Exception {

        if (job.getScript() == null || job.getScript().isBlank()) {
            throw new RuntimeException("Script is empty before frame generation");
        }

        if (job.getVoicePath() == null || job.getVoicePath().isBlank()) {
            throw new RuntimeException("Voice path is empty before frame generation");
        }

        if (job.getImagePaths() == null || job.getImagePaths().isEmpty()) {
            throw new RuntimeException("Image paths is empty before frame generation");
        }

        List<String> captions;

        if (job.getVideoPlan() != null) {
            captions = scriptSegmentService.splitAndWrap(job.getVideoPlan());
        } else {
            captions = scriptSegmentService.splitAndWrap(job.getScript());
        }

        if (captions == null || captions.isEmpty()) {
            throw new RuntimeException("Captions is empty");
        }

        double audioDuration =
                ttsService.getAudioDurationSeconds(job.getVoicePath());

        if (audioDuration <= 0) {
            throw new RuntimeException("Invalid audio duration: " + audioDuration);
        }

        List<CaptionSegment> timeline =
                subtitleTimelineService.build(
                        captions,
                        audioDuration
                );

        String subtitlePath =
                subtitleService.create(timeline);

        job.setSubtitlePath(subtitlePath);
        jobService.save(job);

        System.out.println("CAPTION COUNT = " + captions.size());
        System.out.println("TIMELINE COUNT = " + timeline.size());
        System.out.println("SUBTITLE PATH = " + subtitlePath);

        List<String> frames =
                htmlFrameService.generateFrames(
                        job.getImagePaths(),
                        job.getProductName(),
                        timeline,
                        audioDuration
                );

        if (frames == null || frames.isEmpty()) {
            throw new RuntimeException("Frames is empty");
        }

        job.setFramePaths(frames);
        jobService.save(job);

        System.out.println("FRAME COUNT = " + frames.size());
        System.out.println("FRAMES = " + frames);
    }
}