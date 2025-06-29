package com.example.formalemail.controller;

import com.example.formalemail.service.EmailService;
import com.example.formalemail.service.TransformationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/ui/email")
public class UIController {

    private final TransformationService transformationService;
    private final EmailService emailService;

    @Value("${email.recipient}")
    private String recipientEmail;

    // Store the last successfully sent content to re-populate the form
    private String lastSentContent = "";

    @Autowired
    public UIController(TransformationService transformationService, EmailService emailService) {
        this.transformationService = transformationService;
        this.emailService = emailService;
    }

    @GetMapping("/form")
    public String showEmailForm(Model model) {
        model.addAttribute("lastContent", lastSentContent); // To repopulate if needed
        return "email_form";
    }

    @PostMapping("/send")
    public String transformAndSendEmail(@RequestParam String emailContent, RedirectAttributes redirectAttributes, Model model) {
        if (emailContent == null || emailContent.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Email content cannot be empty.");
            redirectAttributes.addFlashAttribute("lastContent", emailContent);
            return "redirect:/ui/email/form";
        }

        try {
            String transformedContent = transformationService.makeFormal(emailContent);
            String subject = "Transformed Email Message (via UI)";

            System.out.println("Original content (from UI): " + emailContent);
            System.out.println("Attempting to send transformed email (from UI) to: " + recipientEmail);
            System.out.println("Subject: " + subject);
            System.out.println("Transformed content (from UI): " + transformedContent);

            emailService.sendEmail(recipientEmail, subject, transformedContent);

            redirectAttributes.addFlashAttribute("successMessage", "Email transformed and sent successfully to " + recipientEmail + "!");
            lastSentContent = emailContent; // Store original content on success
            redirectAttributes.addFlashAttribute("lastContent", ""); // Clear form for next input
        } catch (Exception e) {
            System.err.println("Error during UI email processing: " + e.getMessage());
            e.printStackTrace(); // For more detailed logging in case of issues
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred: " + e.getMessage());
            redirectAttributes.addFlashAttribute("lastContent", emailContent); // Keep content in form on error
        }

        return "redirect:/ui/email/form";
    }
}
