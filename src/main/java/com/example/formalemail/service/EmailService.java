package com.example.formalemail.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            // It's good practice to set the 'from' address if not configured globally
            // message.setFrom("your-email@example.com"); // Or configure spring.mail.from in properties
            mailSender.send(message);
            System.out.println("Email sent successfully to " + to);
        } catch (Exception e) {
            System.err.println("Error sending email to " + to + ": " + e.getMessage());
            // Depending on the application, you might want to throw a custom exception here
            // or implement retry logic.
        }
    }
}
