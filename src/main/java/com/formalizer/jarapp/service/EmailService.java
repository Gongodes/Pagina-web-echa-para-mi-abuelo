package com.formalizer.jarapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmailAddress; // To set the 'from' field

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmailAddress); // Set the sender from properties
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            System.out.println("Email sent successfully to " + to + " from " + fromEmailAddress);
        } catch (Exception e) {
            System.err.println("Error sending email from " + fromEmailAddress + " to " + to + ": " + e.getMessage());
            // In a real application, you might want to throw a custom exception here,
            // implement retry logic, or notify an admin.
            // For now, we just log the error to console.
            // Re-throwing or a more specific exception might be better for the controller to handle.
            throw new RuntimeException("Error sending email: " + e.getMessage(), e);
        }
    }
}
