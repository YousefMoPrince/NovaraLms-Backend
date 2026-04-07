package com.amarjo.novaralms.auth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin")
public class AdminController {
    @GetMapping("/v1/dashboard")
    public String dashboard() {
        return "dashboard, Welcome Admin";
    }
}
