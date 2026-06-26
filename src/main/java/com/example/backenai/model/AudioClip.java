package com.example.backenai.model;

public class AudioClip {

    private String text;

    private String audioPath;

    private double duration;

    public AudioClip() {
    }

    public AudioClip(
            String text,
            String audioPath,
            double duration
    ) {
        this.text = text;
        this.audioPath = audioPath;
        this.duration = duration;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAudioPath() {
        return audioPath;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "AudioClip{" +
                "text='" + text + '\'' +
                ", audioPath='" + audioPath + '\'' +
                ", duration=" + duration +
                '}';
    }
}