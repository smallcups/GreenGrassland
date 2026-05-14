package com.greengrassland;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * GreenGrassland 应用主类
 */
@SpringBootApplication
@EnableScheduling
public class GreenGrasslandApplication {

    public static void main(String[] args) {
        SpringApplication.run(GreenGrasslandApplication.class, args);
    }
}
