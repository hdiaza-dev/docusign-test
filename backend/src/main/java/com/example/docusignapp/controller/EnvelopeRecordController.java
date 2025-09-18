package com.example.docusignapp.controller;

import com.example.docusignapp.entity.EnvelopeRecord;
import com.example.docusignapp.repository.EnvelopeRecordRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/envelopes")
public class EnvelopeRecordController {

    private final EnvelopeRecordRepository repository;

    public EnvelopeRecordController(EnvelopeRecordRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/records")
    public ResponseEntity<List<EnvelopeRecord>> getAllRecords() {
        return ResponseEntity.ok(repository.findAll());
    }

    @GetMapping("/records/{envelopeId}")
    public ResponseEntity<EnvelopeRecord> getRecord(@PathVariable String envelopeId) {
        return repository.findByEnvelopeId(envelopeId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}