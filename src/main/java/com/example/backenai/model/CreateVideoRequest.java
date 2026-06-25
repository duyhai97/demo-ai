package com.example.backenai.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateVideoRequest {

    private String productName;
    private String affiliateLink;
    private String imageUrl;
    private String imagePath;

}