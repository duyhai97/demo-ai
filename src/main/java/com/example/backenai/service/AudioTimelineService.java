package com.example.backenai.service;

import com.example.backenai.model.AudioClip;
import com.example.backenai.model.Caption;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class AudioTimelineService {

    /**
     * Đọc duration của mp3 bằng ffprobe
     */
    public double getDuration(String audioPath) throws Exception {

        ProcessBuilder pb = new ProcessBuilder(
                "ffprobe",
                "-v",
                "error",
                "-show_entries",
                "format=duration",
                "-of",
                "default=noprint_wrappers=1:nokey=1",
                audioPath
        );

        pb.redirectErrorStream(true);

        Process process = pb.start();

        String duration = "";

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {

            duration = br.readLine();
        }

        process.waitFor();

        if (duration == null || duration.isBlank()) {
            return 0;
        }

        return Double.parseDouble(duration.trim());
    }

    /**
     * Sinh timeline caption
     */
    public List<Caption> buildTimeline(
            List<AudioClip> clips
    ) {

        List<Caption> captions =
                new ArrayList<>();

        double current = 0;

        for (AudioClip clip : clips) {

            Caption caption =
                    new Caption();

            caption.setText(
                    clip.getText()
            );

            caption.setStart(
                    current
            );

            current += clip.getDuration();

            caption.setEnd(
                    current
            );

            captions.add(
                    caption
            );
        }

        return captions;
    }

}