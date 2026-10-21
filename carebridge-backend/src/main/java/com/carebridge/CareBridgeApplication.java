package com.carebridge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = {
        "com.carebridge",
        "com.slotmind.ai"
})
@EnableAsync
public class CareBridgeApplication {
    public static void main(String[] args) {
        SpringApplication.run(CareBridgeApplication.class, args);
    }
}
