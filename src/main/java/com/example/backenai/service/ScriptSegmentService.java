package com.example.backenai.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ScriptSegmentService {

    /**
     * Tách script thành từng câu.
     */
    public List<String> split(String script) {

        List<String> result = new ArrayList<>();

        if (script == null || script.isBlank()) {
            return result;
        }

        script = script
                .replace("\r", " ")
                .replace("\n", " ");

        script = script.replaceAll("\\s+", " ").trim();

        String[] arr = script.split("(?<=[.!?])\\s+");

        for (String s : arr) {

            s = s.trim();

            if (!s.isBlank()) {
                result.add(s);
            }
        }

        return result;
    }

    /**
     * Wrap text để caption đẹp hơn.
     */
    public String wrap(String text) {

        return wrap(text, 24);
    }

    /**
     * Wrap theo số ký tự tối đa mỗi dòng.
     */
    public String wrap(
            String text,
            int maxChars
    ) {

        if (text == null) {
            return "";
        }

        String[] words = text.split("\\s+");

        StringBuilder out = new StringBuilder();

        int current = 0;

        for (String word : words) {

            if (current + word.length() > maxChars) {

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

    /**
     * Tách + wrap luôn.
     */
    public List<String> splitAndWrap(String script) {

        List<String> captions = new ArrayList<>();

        for (String s : split(script)) {

            captions.add(
                    wrap(s)
            );
        }

        return captions;
    }

}