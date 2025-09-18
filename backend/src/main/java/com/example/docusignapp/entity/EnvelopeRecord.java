package com.example.docusignapp.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "envelope_records")
public class EnvelopeRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String envelopeId;
    
    @Column(nullable = false)
    private String signerName;
    
    @Column(nullable = false)
    private String signerEmail;
    
    @Column(nullable = false)
    private String fileName;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SigningStatus status = SigningStatus.PENDING;
    
    @Column(nullable = false)
    private LocalDateTime sentDate = LocalDateTime.now();
    
    private LocalDateTime signedDate;

    public enum SigningStatus {
        PENDING, SIGNED, DECLINED, VOIDED
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getEnvelopeId() { return envelopeId; }
    public void setEnvelopeId(String envelopeId) { this.envelopeId = envelopeId; }
    
    public String getSignerName() { return signerName; }
    public void setSignerName(String signerName) { this.signerName = signerName; }
    
    public String getSignerEmail() { return signerEmail; }
    public void setSignerEmail(String signerEmail) { this.signerEmail = signerEmail; }
    
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    
    public SigningStatus getStatus() { return status; }
    public void setStatus(SigningStatus status) { this.status = status; }
    
    public LocalDateTime getSentDate() { return sentDate; }
    public void setSentDate(LocalDateTime sentDate) { this.sentDate = sentDate; }
    
    public LocalDateTime getSignedDate() { return signedDate; }
    public void setSignedDate(LocalDateTime signedDate) { this.signedDate = signedDate; }
}