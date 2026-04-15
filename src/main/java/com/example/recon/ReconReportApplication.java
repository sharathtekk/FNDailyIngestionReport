package com.example.recon;

import com.example.recon.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class ReconReportApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReconReportApplication.class, args);
    }
}

