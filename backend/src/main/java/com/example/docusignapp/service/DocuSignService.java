package com.example.docusignapp.service;

import com.docusign.esign.api.EnvelopesApi;
import com.docusign.esign.client.ApiClient;
import com.docusign.esign.client.ApiException;
import com.docusign.esign.model.*;
import com.example.docusignapp.config.DocuSignConfig;
import com.example.docusignapp.entity.EnvelopeRecord;
import com.example.docusignapp.repository.EnvelopeRecordRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "docusign.enabled", havingValue = "true")
public class DocuSignService {

    private final ApiClient apiClient;
    private final DocuSignConfig config;
    private final EnvelopeRecordRepository repository;
    
    @Value("${docusign.webhookUrl}")
    private String webhookUrl;

    public DocuSignService(ApiClient apiClient, DocuSignConfig config, EnvelopeRecordRepository repository) {
        this.apiClient = apiClient;
        this.config = config;
        this.repository = repository;
    }

    public String createEnvelopeWithPdf(String signerName, String signerEmail, MultipartFile pdfFile) throws ApiException {
        try {
            String envelopeId = doCreateEnvelopeWithPdf(signerName, signerEmail, pdfFile, apiClient);
            saveEnvelopeRecord(envelopeId, signerName, signerEmail, pdfFile.getOriginalFilename());
            return envelopeId;
        } catch (ApiException e) {
            if (e.getCode() == 401 || e.getMessage().contains("token")) {
                try {
                    ApiClient freshClient = config.createFreshApiClient();
                    String envelopeId = doCreateEnvelopeWithPdf(signerName, signerEmail, pdfFile, freshClient);
                    saveEnvelopeRecord(envelopeId, signerName, signerEmail, pdfFile.getOriginalFilename());
                    return envelopeId;
                } catch (Exception ex) {
                    throw new ApiException("Failed to refresh token: " + ex.getMessage());
                }
            }
            throw e;
        }
    }

    private String doCreateEnvelopeWithPdf(String signerName, String signerEmail, MultipartFile pdfFile, ApiClient client) throws ApiException {
        try {
            byte[] pdfBytes = pdfFile.getBytes();
            String base64Doc = Base64.getEncoder().encodeToString(pdfBytes);

            Document doc = new Document();
            doc.setDocumentBase64(base64Doc);
            doc.setName(pdfFile.getOriginalFilename());
            doc.setFileExtension("pdf");
            doc.setDocumentId("1");

            // SignHere tab positioned at the end of the document
            SignHere signHere = new SignHere();
            signHere.setDocumentId("1");
            signHere.setPageNumber("1");
            signHere.setXPosition("100");
            signHere.setYPosition("700");

            Tabs tabs = new Tabs();
            tabs.setSignHereTabs(List.of(signHere));

            Signer signer = new Signer();
            signer.setEmail(signerEmail);
            signer.setName(signerName);
            signer.setRecipientId("1");
            signer.setRoutingOrder("1");
            signer.setClientUserId("1000");
            signer.setTabs(tabs);

            Recipients recipients = new Recipients();
            recipients.setSigners(List.of(signer));

            EnvelopeDefinition envelope = new EnvelopeDefinition();
            envelope.setEmailSubject("Firma de documento PDF");
            envelope.setDocuments(List.of(doc));
            envelope.setRecipients(recipients);
            envelope.setStatus("sent");
            
            // Webhook solo para HTTPS (dev/prod)
            if (getWebhookUrl().startsWith("https://")) {
                EventNotification eventNotification = new EventNotification();
                eventNotification.setUrl(getWebhookUrl());
                eventNotification.setLoggingEnabled("true");
                eventNotification.setRequireAcknowledgment("true");
                eventNotification.setUseSoapInterface("false");
                
                EnvelopeEvent envelopeEvent = new EnvelopeEvent();
                envelopeEvent.setEnvelopeEventStatusCode("completed");
                eventNotification.setEnvelopeEvents(List.of(envelopeEvent));
                
                envelope.setEventNotification(eventNotification);
            }

            EnvelopesApi envelopesApi = new EnvelopesApi(client);
            EnvelopeSummary summary = envelopesApi.createEnvelope(config.getAccountId(), envelope);
            return summary.getEnvelopeId();
        } catch (IOException e) {
            throw new ApiException("Error reading PDF file: " + e.getMessage());
        }
    }

    public String createRecipientViewUrl(String envelopeId, String signerName, String signerEmail, String returnUrl) throws ApiException {
        EnvelopesApi envelopesApi = new EnvelopesApi(apiClient);
        RecipientViewRequest viewRequest = new RecipientViewRequest();
        viewRequest.setReturnUrl(returnUrl);
        viewRequest.setAuthenticationMethod("none");
        viewRequest.setEmail(signerEmail);
        viewRequest.setUserName(signerName);
        viewRequest.setClientUserId("1000"); // must match signer.clientUserId if set

        // Important: embedded recipients must set clientUserId on the recipient
        // Update the recipient to set clientUserId
        TemplateRole role = null; // not used here

        ViewUrl viewUrl = envelopesApi.createRecipientView(config.getAccountId(), envelopeId, viewRequest);
        return viewUrl.getUrl();
    }

    private String buildHtmlQuestionnaire(String signerName, Map<String, Object> answers) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body>");
        sb.append("<h1>Cuestionario</h1>");
        sb.append("<p>Nombre del firmante: ").append(escape(signerName)).append("</p>");
        sb.append("<table border='1' cellpadding='6' cellspacing='0'>");
        for (Map.Entry<String, Object> e : answers.entrySet()) {
            sb.append("<tr><td><b>").append(escape(e.getKey())).append("</b></td><td>")
              .append(escape(String.valueOf(e.getValue()))).append("</td></tr>");
        }
        sb.append("</table>");
        sb.append("<p>Firme aquí: <span>/firma/</span></p>");
        sb.append("</body></html>");
        return sb.toString();
    }

    private String escape(String s) {
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }

    private void saveEnvelopeRecord(String envelopeId, String signerName, String signerEmail, String fileName) {
        EnvelopeRecord record = new EnvelopeRecord();
        record.setEnvelopeId(envelopeId);
        record.setSignerName(signerName);
        record.setSignerEmail(signerEmail);
        record.setFileName(fileName);
        repository.save(record);
    }

    private String getWebhookUrl() {
        return webhookUrl;
    }
}
