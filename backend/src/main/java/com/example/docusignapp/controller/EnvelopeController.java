package com.example.docusignapp.controller;

import com.docusign.esign.client.ApiException;
import com.example.docusignapp.service.DocuSignService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.context.annotation.Profile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Profile("docusign")
public class EnvelopeController {

    private final DocuSignService service;

    public EnvelopeController(DocuSignService service) {
        this.service = service;
    }

    @PostMapping("/envelopes")
    public ResponseEntity<?> createEnvelopeAndRecipientView(@RequestBody CreateEnvelopeRequest req) {
        try {
            String envelopeId = service.createEnvelopeWithQuestionnaire(req.signerName(), req.signerEmail(), req.answers());
            String signingUrl = service.createRecipientViewUrl(envelopeId, req.signerName(), req.signerEmail(), req.returnUrl());
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
