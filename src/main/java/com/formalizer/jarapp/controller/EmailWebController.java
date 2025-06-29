package com.formalizer.jarapp.controller;

import com.formalizer.jarapp.service.EmailService;
import com.formalizer.jarapp.service.TransformationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller // Changed from @RestController
public class EmailWebController {

    private final TransformationService transformationService;
    private final EmailService emailService;

    @Value("${email.default.recipient}")
    private String defaultRecipientEmail;

    // To store the last content for re-populating the form, especially on error
    private String lastSubmittedContent = "";

    @Autowired
    public EmailWebController(TransformationService transformationService, EmailService emailService) {
        this.transformationService = transformationService;
        this.emailService = emailService;
    }

    @GetMapping("/") // Map to root for easy access
    public String showEmailForm(Model model) {
        // Add lastSubmittedContent to model so Thymeleaf can use it to repopulate textarea
        if (!model.containsAttribute("lastContent")) { // Avoid overwriting flash attributes
             model.addAttribute("lastContent", lastSubmittedContent);
        }
        return "email_form"; // Name of the Thymeleaf template
    }

    @PostMapping("/send-email")
    public String transformAndSendEmailFromForm(@RequestParam("emailContent") String emailContent,
                                                RedirectAttributes redirectAttributes) {

        if (emailContent == null || emailContent.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Email content cannot be empty.");
            redirectAttributes.addFlashAttribute("lastContent", emailContent); // Send back the (empty) content
            lastSubmittedContent = emailContent; // Update for next GET request if user refreshes
            return "redirect:/";
        }

        try {
            String transformedContent = transformationService.makeFormal(emailContent);
            String subject = "Transformed Email Message (Web UI)";

            // Log the action
            System.out.println("WebUI: Original content: " + emailContent);
            System.out.println("WebUI: Attempting to send transformed email to: " + defaultRecipientEmail);
            System.out.println("WebUI: Subject: " + subject);
            System.out.println("WebUI: Transformed content: " + transformedContent);

            emailService.sendEmail(defaultRecipientEmail, subject, transformedContent);

            redirectAttributes.addFlashAttribute("successMessage", "Email transformed and sent successfully to " + defaultRecipientEmail + "!");
            lastSubmittedContent = ""; // Clear last content on success for a fresh form
            redirectAttributes.addFlashAttribute("lastContent", "");


        } catch (RuntimeException e) { // Catching runtime from EmailService
            System.err.println("WebUI Error: Failed to send email - " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to send email: " + e.getMessage());
            lastSubmittedContent = emailContent; // Keep content in form on error
            redirectAttributes.addFlashAttribute("lastContent", emailContent);
        } catch (Exception e) {
            System.err.println("WebUI Error: Unexpected error during email processing - " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
            lastSubmittedContent = emailContent; // Keep content in form on error
            redirectAttributes.addFlashAttribute("lastContent", emailContent);
        }

        return "redirect:/"; // Redirect back to the form page
    }
}
