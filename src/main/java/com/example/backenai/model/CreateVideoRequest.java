package com.example.backenai.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateVideoRequest {

    private String productName;

    private String affiliateLink;

    private List<String> imagePaths;

}