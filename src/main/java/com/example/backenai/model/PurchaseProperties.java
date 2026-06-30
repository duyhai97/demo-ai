package com.example.backenai.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "app.purchase")
public class PurchaseProperties {

    private Video10 video10 = new Video10();
    private Bank bank = new Bank();

    @Getter
    @Setter
    public static class Video10 {
        private Integer extraVideos = 10;
        private Long amount = 29000L;
    }

    @Getter
    @Setter
    public static class Bank {
        private String bin;
        private String accountNo;
        private String accountName;
    }
}