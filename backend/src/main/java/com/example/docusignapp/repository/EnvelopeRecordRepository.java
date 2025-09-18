package com.example.docusignapp.repository;

import com.example.docusignapp.entity.EnvelopeRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EnvelopeRecordRepository extends JpaRepository<EnvelopeRecord, Long> {
    Optional<EnvelopeRecord> findByEnvelopeId(String envelopeId);
}