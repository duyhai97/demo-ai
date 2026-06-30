package com.example.backenai.controller;

import com.example.backenai.model.CreateUserRequest;
import com.example.backenai.model.UserResponse;
import com.example.backenai.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public List<UserResponse> getUsers() {
        return userService.getAllUsers();
    }

    @PostMapping
    public UserResponse createUser(
            @RequestBody CreateUserRequest request
    ) {
        return userService.createUser(request);
    }
}