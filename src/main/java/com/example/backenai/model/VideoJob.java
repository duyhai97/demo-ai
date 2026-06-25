package com.example.backenai.model;

import com.example.backenai.constant.JobStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VideoJob {

    private String imagePath;
    private String script;
    private String voicePath;
    private String subtitlePath;
    private String videoPath;
    private String videoUrl;

    private String jobId;
    private String productName;
    private String affiliateLink;
    private JobStatus status;
}