package com.example.formalemail.controller;

import com.example.formalemail.service.EmailService;
import com.example.formalemail.service.TransformationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    private final TransformationService transformationService;
    private final EmailService emailService;

    @Value("${email.recipient}") // Make recipient configurable
    private String recipientEmail;

    @Autowired
    public EmailController(TransformationService transformationService, EmailService emailService) {
        this.transformationService = transformationService;
        this.emailService = emailService;
    }

    @PostMapping("/transform")
    public String transformEmail(@RequestBody String emailContent) {
        if (emailContent == null || emailContent.trim().isEmpty()) {
            return "Email content cannot be empty.";
        }

        String transformedContent = transformationService.makeFormal(emailContent);
        String subject = "Transformed Email Message";

        // Log before sending
        System.out.println("Original content: " + emailContent);
        System.out.println("Attempting to send transformed email to: " + recipientEmail);
        System.out.println("Subject: " + subject);
        System.out.println("Transformed content: " + transformedContent);

        emailService.sendEmail(recipientEmail, subject, transformedContent);

        return "Email transformed. Attempted to send to " + recipientEmail + ". Transformed content: '" + transformedContent + "'";
    }
}
