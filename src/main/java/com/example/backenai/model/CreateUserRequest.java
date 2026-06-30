package com.example.backenai.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateUserRequest {

    private String username;

    private String password;

    private String fullName;

    private String email;

    private List<String> roles;
}