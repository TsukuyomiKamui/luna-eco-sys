package com.kamui.luna;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class LunaResSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(LunaResSystemApplication.class, args);
    }

}
