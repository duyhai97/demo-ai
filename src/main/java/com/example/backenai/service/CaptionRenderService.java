package com.example.backenai.service;

import com.example.backenai.model.Caption;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CaptionRenderService {

    public String buildFilter(List<Caption> captions) {

        StringBuilder filter = new StringBuilder();

        filter.append(
                "scale=1080:1920:force_original_aspect_ratio=increase,"
        );

        filter.append(
                "crop=1080:1920,"
        );

        filter.append(
                "zoompan=" +
                        "z='min(zoom+0.0008,1.15)':" +
                        "x='iw/2-(iw/zoom/2)':" +
                        "y='ih/2-(ih/zoom/2)':" +
                        "d=300:" +
                        "s=1080x1920"
        );

        for (Caption caption : captions) {

            filter.append(",");

            filter.append(
                    createDrawText(caption)
            );
        }

        return filter.toString();
    }

    private String createDrawText(Caption caption) {

        String text = escape(caption.getText());

        return String.format(
                "drawtext="
                        + "font='Arial':"
                        + "text='%s':"
                        + "fontsize=72:"
                        + "fontcolor=white:"
                        + "borderw=8:"
                        + "bordercolor=black:"
                        + "line_spacing=18:"
                        + "x=(w-text_w)/2:"
                        + "y=h-420:"
                        + "enable='between(t,%.2f,%.2f)'",
                text,
                caption.getStart(),
                caption.getEnd()
        );
    }

    private String escape(String text) {

        return text
                .replace("\\", "\\\\")
                .replace(":", "\\:")
                .replace("'", "\\'")
                .replace(",", "\\,")
                .replace("%", "\\%");
    }

}