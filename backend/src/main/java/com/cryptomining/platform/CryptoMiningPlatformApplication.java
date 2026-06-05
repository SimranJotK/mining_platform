package com.cryptomining.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CryptoMiningPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(CryptoMiningPlatformApplication.class, args);
    }
}
