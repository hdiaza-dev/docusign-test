package com.example.docusignapp.controller;

import com.docusign.esign.client.ApiException;
import com.example.docusignapp.service.DocuSignService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@ConditionalOnProperty(name = "docusign.enabled", havingValue = "true")
public class EnvelopeController {

    private final DocuSignService service;

    public EnvelopeController(DocuSignService service) {
        this.service = service;
    }

    @PostMapping("/envelopes")
    public ResponseEntity<?> createEnvelopeAndRecipientView(
            @RequestParam String signerName,
            @RequestParam String signerEmail,
            @RequestParam String returnUrl,
            @RequestParam("pdfFile") org.springframework.web.multipart.MultipartFile pdfFile) {
        try {
            String envelopeId = service.createEnvelopeWithPdf(signerName, signerEmail, pdfFile);
            String signingUrl = service.createRecipientViewUrl(envelopeId, signerName, signerEmail, returnUrl);
            Map<String, Object> resp = new HashMap<>();
            resp.put("envelopeId", envelopeId);
            resp.put("signingUrl", signingUrl);
            return ResponseEntity.ok(resp);
        } catch (ApiException ex) {
            Map<String, Object> err = new HashMap<>();
            err.put("message", ex.getMessage());
            err.put("responseBody", ex.getResponseBody());
            return ResponseEntity.status(500).body(err);
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(Map.of("message", ex.getMessage()));
        }
    }

    public static record CreateEnvelopeRequest(String signerName, String signerEmail, String returnUrl,
                                               Map<String,Object> answers) {}
}
