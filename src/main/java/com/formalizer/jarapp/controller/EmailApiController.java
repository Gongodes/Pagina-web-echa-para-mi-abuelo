package com.formalizer.jarapp.controller;

import com.formalizer.jarapp.service.EmailService;
import com.formalizer.jarapp.service.TransformationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/email")
public class EmailApiController {

    private final TransformationService transformationService;
    private final EmailService emailService;

    @Value("${email.default.recipient}")
    private String defaultRecipientEmail;

    @Autowired
    public EmailApiController(TransformationService transformationService, EmailService emailService) {
        this.transformationService = transformationService;
        this.emailService = emailService;
    }

    // Define a simple request structure for the API
    public static class EmailRequest {
        private String content;
        // Could add 'to', 'subject' fields here if they should be API-configurable

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    @PostMapping("/transform-and-send")
    public ResponseEntity<?> transformAndSendEmail(@RequestBody EmailRequest request) {
        if (request == null || request.getContent() == null || request.getContent().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email content cannot be empty."));
        }

        try {
            String originalContent = request.getContent();
            String transformedContent = transformationService.makeFormal(originalContent);
            String subject = "Transformed Email Message (API)"; // Or make subject configurable

            // Log the action
            System.out.println("API: Original content: " + originalContent);
            System.out.println("API: Attempting to send transformed email to: " + defaultRecipientEmail);
            System.out.println("API: Subject: " + subject);
            System.out.println("API: Transformed content: " + transformedContent);

            emailService.sendEmail(defaultRecipientEmail, subject, transformedContent);

            return ResponseEntity.ok(Map.of(
                "message", "Email transformed and sent successfully to " + defaultRecipientEmail,
                "originalContent", originalContent,
                "transformedContent", transformedContent,
                "recipient", defaultRecipientEmail
            ));
        } catch (RuntimeException e) { // Catching runtime from EmailService for now
            System.err.println("API Error: " + e.getMessage());
             // Log the full stack trace for detailed debugging on the server
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(Map.of("error", "Failed to send email: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("API Error: Unexpected error during email processing - " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(Map.of("error", "An unexpected error occurred: " + e.getMessage()));
        }
    }
}
