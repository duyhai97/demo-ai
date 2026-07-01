package com.example.backenai.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoScene {
    private Integer imageIndex;
    private Integer duration;
    private String voice;
    private String subtitle;
    private String effect;
}