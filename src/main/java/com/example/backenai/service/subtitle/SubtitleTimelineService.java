package com.example.backenai.service.subtitle;

import com.example.backenai.model.CaptionSegment;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SubtitleTimelineService {

    public List<CaptionSegment> build(
            List<String> captions,
            double audioDuration
    ) {

        if (captions == null || captions.isEmpty()) {
            throw new RuntimeException("Caption empty");
        }

        double duration =
                audioDuration / captions.size();

        List<CaptionSegment> result =
                new ArrayList<>();

        double current = 0;

        for (String caption : captions) {

            CaptionSegment segment =
                    new CaptionSegment();

            segment.setText(caption);

            segment.setStart(current);

            current += duration;

            segment.setEnd(current);

            result.add(segment);
        }

        return result;
    }
}