package com.example.backenai.controller;

import com.example.backenai.model.LoginRequest;
import com.example.backenai.model.LoginResponse;
import com.example.backenai.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final JwtService jwtService;

    @PostMapping("/login")
    public LoginResponse login(
            @RequestBody LoginRequest request
    ) {
        String username = request.getUsername();
        String password = request.getPassword();

        if (!"admin".equals(username) || !"123456".equals(password)) {
            throw new RuntimeException("Sai tài khoản hoặc mật khẩu");
        }

        String token =
                jwtService.generateToken(username);

        return new LoginResponse(
                token,
                username
        );
    }
}