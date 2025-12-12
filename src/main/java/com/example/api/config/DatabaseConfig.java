package com.example.api.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseConfig {

    @Bean
    public CommandLineRunner initDatabase() {
        return args -> {
            System.out.println("=".repeat(70));
            System.out.println("POS-API Database Ready!");
            System.out.println("=".repeat(70));
            System.out.println("H2 Console: http://localhost:8080/h2-console");
            System.out.println("JDBC URL: jdbc:h2:file:./data/discountdb");
            System.out.println("Username: sa");
            System.out.println("Password: (leave blank)");
            System.out.println("=".repeat(70));
            System.out.println("Note: discount_rules table created automatically by JPA");
            System.out.println("=".repeat(70));
        };
    }
}