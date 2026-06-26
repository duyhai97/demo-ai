package com.example.backenai.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaptionSegment {

    private String text;

    private double start;

    private double end;

}