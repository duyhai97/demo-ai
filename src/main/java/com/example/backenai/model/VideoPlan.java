package com.example.backenai.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoPlan {
    private String title;
    private String hook;
    private Integer duration;
    private List<VideoScene> scenes;
    private String cta;
}