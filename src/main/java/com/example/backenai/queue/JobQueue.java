package com.example.backenai.queue;

import com.example.backenai.model.VideoJob;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class JobQueue {

    private final BlockingQueue<VideoJob> queue =
            new LinkedBlockingQueue<>();

    public void push(VideoJob job) {
        queue.offer(job);
    }

    public VideoJob pop() throws InterruptedException {
        return queue.take();
    }
}