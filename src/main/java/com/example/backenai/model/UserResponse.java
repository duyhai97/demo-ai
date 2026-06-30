package com.example.backenai.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class UserResponse {

    private Long id;

    private String username;

    private String fullName;

    private String email;

    private Boolean enabled;

    private List<String> roles;

    private Integer dailyVideoLimit;
}