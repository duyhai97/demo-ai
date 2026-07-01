package com.example.backenai.service;

import com.example.backenai.model.VideoPlan;
import com.example.backenai.model.VideoScene;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ScriptSegmentService {

    public List<String> split(String script) {
        List<String> result = new ArrayList<>();

        if (script == null || script.isBlank()) {
            return result;
        }

        script = script
                .replace("\r", " ")
                .replace("\n", " ")
                .replaceAll("\\s+", " ")
                .trim();

        String[] arr = script.split("(?<=[.!?])\\s+");

        for (String s : arr) {
            s = s.trim();

            if (!s.isBlank()) {
                result.add(s);
            }
        }

        return result;
    }

    public List<String> splitFromVideoPlan(VideoPlan plan) {
        List<String> result = new ArrayList<>();

        if (plan == null || plan.getScenes() == null || plan.getScenes().isEmpty()) {
            return result;
        }

        for (VideoScene scene : plan.getScenes()) {
            String subtitle = scene.getSubtitle();

            if (subtitle == null || subtitle.isBlank()) {
                subtitle = scene.getVoice();
            }

            subtitle = wrap(subtitle);

            if (!subtitle.isBlank()) {
                result.add(subtitle);
            }
        }

        return result;
    }

    public String wrap(String text) {
        return wrap(text, 22);
    }

    public String wrap(String text, int maxChars) {
        if (text == null) {
            return "";
        }

        text = text
                .replace("\r", " ")
                .replace("\n", " ")
                .replaceAll("\\s+", " ")
                .trim();

        if (text.isBlank()) {
            return "";
        }

        String[] words = text.split("\\s+");

        StringBuilder out = new StringBuilder();

        int current = 0;

        for (String word : words) {
            if (current > 0 && current + 1 + word.length() > maxChars) {
                out.append("\n");
                current = 0;
            }

            if (current != 0) {
                out.append(" ");
                current++;
            }

            out.append(word);
            current += word.length();
        }

        return out.toString();
    }

    public List<String> splitAndWrap(String script) {
        List<String> captions = new ArrayList<>();

        for (String s : split(script)) {
            captions.add(wrap(s));
        }

        return captions;
    }

    public List<String> splitAndWrap(VideoPlan plan) {
        return splitFromVideoPlan(plan);
    }
}