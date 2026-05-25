package com.ai.learning.ai.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {


    @GetMapping("/hello")
    public String hello(){
        return "AI Backend Learning Poooo!!!";
    }

    @GetMapping("/status")
    public String status(){
        return "DAY 1 DONE!!!";
    }
}
