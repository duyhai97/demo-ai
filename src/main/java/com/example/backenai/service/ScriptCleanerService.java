package com.example.backenai.service;

import org.springframework.stereotype.Service;

@Service
public class ScriptCleanerService {

    public String clean(String text) {

        if (text == null) {
            return "";
        }

        text = text.replace("\r", "");

        text = text.replace("*", "");
        text = text.replace("#", "");
        text = text.replace("•", "");
        text = text.replace("👉", "");
        text = text.replace("🔥", "");
        text = text.replace("✅", "");
        text = text.replace("✔", "");
        text = text.replace("💥", "");
        text = text.replace("❤️", "");

        text = text.replaceAll("\\[.*?\\]", "");

        text = text.replaceAll("\\(.*?\\)", "");

        text = text.replaceAll("[\\t]+", " ");

        text = text.replaceAll(" +", " ");

        text = text.replaceAll("\\n{2,}", "\n");

        text = text.trim();

        return text;
    }

    public String toSpeech(String script) {

        script = clean(script);

        script = script.replace("\n", " ");

        script = script.replaceAll(" +", " ");

        return script.trim();
    }

}