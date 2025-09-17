package com.example.docusignapp.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ConsentController {

    @Value("${docusign.clientId}")
    private String clientId;

    @GetMapping("/consent")
    public ResponseEntity<Map<String, String>> getConsentUrl() {
        String consentUrl = "https://account-d.docusign.com/oauth/auth?response_type=code&scope=signature%20impersonation&client_id=" 
                + clientId + "&redirect_uri=https://www.docusign.com";
        return ResponseEntity.ok(Map.of("consentUrl", consentUrl));
    }
}