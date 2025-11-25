package com.example.api;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HelloController {

    @GetMapping("/hello")
    public Map<String, String> sayHello() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Hello, World!");
        response.put("status", "success");
        return response;
    }

    @PostMapping("/greet")
    public Map<String, String> greetUser(@RequestBody Map<String, String> request) {
        String name = request.getOrDefault("name", "Guest");
        Map<String, String> response = new HashMap<>();
        response.put("greeting", "Welcome, " + name + "!");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return response;
    }
}