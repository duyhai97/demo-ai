package com.example.backenai;

import com.example.backenai.model.PurchaseProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(PurchaseProperties.class)
public class BackendAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendAiApplication.class, args);
    }

}
