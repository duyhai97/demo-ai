package com.example.backenai.config;

import com.example.backenai.entity.RoleEntity;
import com.example.backenai.entity.UserEntity;
import com.example.backenai.repository.RoleRepository;
import com.example.backenai.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {

        System.out.println("========== INIT DATABASE ==========");

        RoleEntity adminRole =
                createRoleIfNotExists("ADMIN");

        RoleEntity userRole =
                createRoleIfNotExists("USER");

        createAdminIfNotExists(adminRole);

        System.out.println("========== INIT DONE ==========");
    }

    private RoleEntity createRoleIfNotExists(String roleName) {

        return roleRepository.findByName(roleName)
                .orElseGet(() -> {

                    RoleEntity role = new RoleEntity();
                    role.setName(roleName);

                    System.out.println("CREATE ROLE = " + roleName);

                    return roleRepository.save(role);
                });
    }

    private void createAdminIfNotExists(RoleEntity adminRole) {

        if (userRepository.findByUsername("admin").isPresent()) {

            System.out.println("ADMIN EXISTS");

            return;
        }

        UserEntity admin = new UserEntity();

        admin.setUsername("admin");

        admin.setPassword(
                passwordEncoder.encode("123456")
        );

        admin.setFullName("System Administrator");

        admin.setEmail("admin@demo-ai.com");

        admin.setEnabled(true);

        admin.setRoles(
                new HashSet<>(
                        List.of(adminRole)
                )
        );

        userRepository.save(admin);

        System.out.println("CREATE DEFAULT ADMIN SUCCESS");
        System.out.println("USERNAME = admin");
        System.out.println("PASSWORD = 123456");
    }
}