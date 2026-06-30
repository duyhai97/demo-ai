package com.example.backenai.service;

import com.example.backenai.entity.RoleEntity;
import com.example.backenai.entity.UserEntity;
import com.example.backenai.model.CreateUserRequest;
import com.example.backenai.model.UserResponse;
import com.example.backenai.repository.RoleRepository;
import com.example.backenai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {

        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new RuntimeException("Username is empty");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new RuntimeException("Password is empty");
        }

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        List<String> roleNames =
                request.getRoles() == null || request.getRoles().isEmpty()
                        ? List.of("USER")
                        : request.getRoles();

        UserEntity user = new UserEntity();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setEnabled(true);
        user.setRoles(new HashSet<>());

        for (String roleName : roleNames) {
            RoleEntity role =
                    roleRepository.findByName(roleName)
                            .orElseGet(() -> createRole(roleName));

            user.getRoles().add(role);
        }

        return toResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private RoleEntity createRole(String roleName) {
        RoleEntity role = new RoleEntity();
        role.setName(roleName);
        return roleRepository.save(role);
    }

    private UserResponse toResponse(UserEntity user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getEnabled(),
                user.getRoles()
                        .stream()
                        .map(RoleEntity::getName)
                        .toList()
        );
    }
}