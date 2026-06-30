package com.example.backenai.controller;

import com.example.backenai.entity.RoleEntity;
import com.example.backenai.entity.UserEntity;
import com.example.backenai.model.LoginRequest;
import com.example.backenai.model.LoginResponse;
import com.example.backenai.repository.UserRepository;
import com.example.backenai.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public LoginResponse login(
            @RequestBody LoginRequest request
    ) {
        UserEntity user =
                userRepository.findByUsername(request.getUsername())
                        .orElseThrow(() -> new RuntimeException("Sai tài khoản hoặc mật khẩu"));

        if (!Boolean.TRUE.equals(user.getEnabled())) {
            throw new RuntimeException("Tài khoản đã bị khóa");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Sai tài khoản hoặc mật khẩu");
        }

        List<String> roles =
                user.getRoles()
                        .stream()
                        .map(RoleEntity::getName)
                        .toList();

        String token =
                jwtService.generateToken(
                        user.getUsername(),
                        roles
                );

        return new LoginResponse(
                token,
                user.getUsername(),
                roles
        );
    }
}