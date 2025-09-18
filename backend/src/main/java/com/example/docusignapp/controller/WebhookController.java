package com.example.docusignapp.controller;

import com.example.docusignapp.entity.EnvelopeRecord;
import com.example.docusignapp.repository.EnvelopeRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/webhook")
public class WebhookController {

    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);
    private final EnvelopeRecordRepository repository;

    public WebhookController(EnvelopeRecordRepository repository) {
        this.repository = repository;
    }

    @PostMapping(value = "/docusign", consumes = {"application/xml", "text/xml", "application/json"})
    public ResponseEntity<String> handleDocuSignWebhook(@RequestBody String payload) {
        try {
            logger.info("Webhook received: {}", payload);
            
            // Parse XML to extract envelopeId and status
            String envelopeId = extractFromXml(payload, "EnvelopeID");
            String status = extractFromXml(payload, "Status");
            
            logger.info("Processing envelope {} with status {}", envelopeId, status);
            
            if (envelopeId != null && "Completed".equalsIgnoreCase(status)) {
                Optional<EnvelopeRecord> recordOpt = repository.findByEnvelopeId(envelopeId);
                if (recordOpt.isPresent()) {
                    EnvelopeRecord record = recordOpt.get();
                    record.setStatus(EnvelopeRecord.SigningStatus.SIGNED);
                    record.setSignedDate(LocalDateTime.now());
                    repository.save(record);
                    logger.info("Updated envelope {} to SIGNED status", envelopeId);
                } else {
                    logger.warn("Envelope {} not found in database", envelopeId);
                }
            }
            
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            logger.error("Error processing webhook", e);
            return ResponseEntity.ok("OK"); // Always return OK to DocuSign
        }
    }
    
    private String extractFromXml(String xml, String tagName) {
        try {
            String startTag = "<" + tagName + ">";
            String endTag = "</" + tagName + ">";
            int start = xml.indexOf(startTag);
            int end = xml.indexOf(endTag);
            if (start != -1 && end != -1) {
                return xml.substring(start + startTag.length(), end).trim();
            }
        } catch (Exception e) {
            logger.warn("Failed to extract {} from XML", tagName, e);
        }
        return null;
    }
}