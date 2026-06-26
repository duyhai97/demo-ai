package com.example.backenai.worker.step;

import com.example.backenai.model.VideoJob;

public interface VideoStep {

    void execute(VideoJob job) throws Exception;

}