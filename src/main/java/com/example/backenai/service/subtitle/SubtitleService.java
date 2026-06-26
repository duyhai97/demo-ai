package com.example.backenai.service.subtitle;

import com.example.backenai.model.CaptionSegment;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class SubtitleService {

    public String create(
            List<CaptionSegment> segments
    ) throws Exception {

        Files.createDirectories(
                Path.of("storage/subtitle")
        );

        String output =
                "storage/subtitle/"
                        + UUID.randomUUID()
                        + ".srt";

        StringBuilder sb =
                new StringBuilder();

        int index = 1;

        for (CaptionSegment segment : segments) {

            sb.append(index++)
                    .append("\n");

            sb.append(format(segment.getStart()))
                    .append(" --> ")
                    .append(format(segment.getEnd()))
                    .append("\n");

            sb.append(segment.getText())
                    .append("\n\n");
        }

        Files.writeString(
                Path.of(output),
                sb.toString()
        );

        return output;
    }

    private String format(double seconds) {

        int h = (int) seconds / 3600;
        int m = ((int) seconds % 3600) / 60;
        int s = (int) seconds % 60;
        int ms = (int) ((seconds - (int) seconds) * 1000);

        return String.format(
                Locale.US,
                "%02d:%02d:%02d,%03d",
                h,
                m,
                s,
                ms
        );
    }

}