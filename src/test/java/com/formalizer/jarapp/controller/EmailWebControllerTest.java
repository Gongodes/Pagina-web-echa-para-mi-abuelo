package com.formalizer.jarapp.controller;

import com.formalizer.jarapp.service.EmailService;
import com.formalizer.jarapp.service.TransformationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmailWebController.class)
@TestPropertySource(properties = "email.default.recipient=testweb@example.com")
public class EmailWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransformationService mockTransformationService;

    @MockBean
    private EmailService mockEmailService;

    @Autowired
    private EmailWebController emailWebController;

    private String defaultTestRecipient = "testweb@example.com";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailWebController, "defaultRecipientEmail", defaultTestRecipient);
        // Reset lastSubmittedContent before each test for predictable form state
        ReflectionTestUtils.setField(emailWebController, "lastSubmittedContent", "");
    }

    @Test
    void showEmailForm_shouldDisplayForm() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("email_form"))
                .andExpect(model().attributeExists("lastContent"))
                .andExpect(model().attribute("lastContent", "")); // Initially empty
    }

    @Test
    void transformAndSendEmailFromForm_validContent_shouldRedirectWithSuccess() throws Exception {
        String originalContent = "hey, this is for the web form";
        String transformedContent = "Hello, this is for the web form.";

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("emailContent", originalContent);

        when(mockTransformationService.makeFormal(originalContent)).thenReturn(transformedContent);
        doNothing().when(mockEmailService).sendEmail(eq(defaultTestRecipient), anyString(), eq(transformedContent));

        mockMvc.perform(post("/send-email").params(params))
                .andExpect(status().is3xxRedirection()) // Expect redirect
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("successMessage"))
                .andExpect(flash().attribute("successMessage", "Email transformed and sent successfully to " + defaultTestRecipient + "!"))
                .andExpect(flash().attribute("lastContent", "")); // lastContent cleared on success

        verify(mockTransformationService, times(1)).makeFormal(originalContent);
        verify(mockEmailService, times(1)).sendEmail(eq(defaultTestRecipient), anyString(), eq(transformedContent));
    }

    @Test
    void transformAndSendEmailFromForm_emptyContent_shouldRedirectWithError() throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("emailContent", "");

        mockMvc.perform(post("/send-email").params(params))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("errorMessage"))
                .andExpect(flash().attribute("errorMessage", "Email content cannot be empty."))
                .andExpect(flash().attribute("lastContent", "")); // Content was empty

        verify(mockTransformationService, never()).makeFormal(anyString());
        verify(mockEmailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void transformAndSendEmailFromForm_emailServiceThrowsError_shouldRedirectWithErrorAndKeepContent() throws Exception {
        String originalContent = "content that causes mail error";
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("emailContent", originalContent);

        when(mockTransformationService.makeFormal(originalContent)).thenReturn("Transformed content.");
        doThrow(new RuntimeException("Mail server unavailable"))
            .when(mockEmailService).sendEmail(anyString(), anyString(), anyString());

        mockMvc.perform(post("/send-email").params(params))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("errorMessage"))
                .andExpect(flash().attribute("errorMessage", "Failed to send email: Mail server unavailable"))
                .andExpect(flash().attribute("lastContent", originalContent)); // Content retained on error

        verify(mockTransformationService, times(1)).makeFormal(originalContent);
        verify(mockEmailService, times(1)).sendEmail(anyString(), anyString(), anyString());
    }
}
