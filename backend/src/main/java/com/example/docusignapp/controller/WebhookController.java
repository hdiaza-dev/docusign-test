package com.example.docusignapp.controller;

import com.example.docusignapp.entity.EnvelopeRecord;
import com.example.docusignapp.repository.EnvelopeRecordRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/webhook")
public class WebhookController {

    private final EnvelopeRecordRepository repository;

    public WebhookController(EnvelopeRecordRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/docusign")
    public ResponseEntity<String> handleDocuSignWebhook(@RequestBody Map<String, Object> payload) {
        try {
            String envelopeId = (String) payload.get("envelopeId");
            String status = (String) payload.get("status");
            
            if (envelopeId != null && "completed".equals(status)) {
                Optional<EnvelopeRecord> recordOpt = repository.findByEnvelopeId(envelopeId);
                if (recordOpt.isPresent()) {
                    EnvelopeRecord record = recordOpt.get();
                    record.setStatus(EnvelopeRecord.SigningStatus.SIGNED);
                    record.setSignedDate(LocalDateTime.now());
                    repository.save(record);
                }
            }
            
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            return ResponseEntity.ok("OK"); // Always return OK to DocuSign
        }
    }
}