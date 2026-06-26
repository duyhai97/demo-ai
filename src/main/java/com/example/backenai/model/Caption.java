package com.example.backenai.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Caption {

    private String text;

    private double start;

    private double end;

    public Caption() {
    }

    public Caption(
            String text,
            double start,
            double end
    ) {
        this.text = text;
        this.start = start;
        this.end = end;
    }
}