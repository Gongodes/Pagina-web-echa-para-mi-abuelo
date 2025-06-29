package com.formalizer.jarapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.formalizer.jarapp.service.EmailService;
import com.formalizer.jarapp.service.TransformationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmailApiController.class)
// Provide dummy value for email.default.recipient for testing context
@TestPropertySource(properties = "email.default.recipient=testapi@example.com")
public class EmailApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransformationService mockTransformationService;

    @MockBean
    private EmailService mockEmailService;

    @Autowired
    private EmailApiController emailApiController; // To inject the actual value

    private ObjectMapper objectMapper = new ObjectMapper();
    private String defaultTestRecipient = "testapi@example.com"; // Should match @TestPropertySource

     @BeforeEach
    void setUp() {
        // Ensure the @Value field in the controller is set with the test property
        // This is often handled by Spring, but can be explicit for clarity or complex cases
        ReflectionTestUtils.setField(emailApiController, "defaultRecipientEmail", defaultTestRecipient);
    }

    @Test
    void transformAndSendEmail_validRequest_shouldReturnSuccess() throws Exception {
        String originalContent = "hey, can u check this out? thx";
        String transformedContent = "Hello, could you please check this out? Thank you.";

        EmailApiController.EmailRequest request = new EmailApiController.EmailRequest();
        request.setContent(originalContent);

        when(mockTransformationService.makeFormal(originalContent)).thenReturn(transformedContent);
        doNothing().when(mockEmailService).sendEmail(eq(defaultTestRecipient), anyString(), eq(transformedContent));

        mockMvc.perform(post("/api/email/transform-and-send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Email transformed and sent successfully to " + defaultTestRecipient))
                .andExpect(jsonPath("$.transformedContent").value(transformedContent))
                .andExpect(jsonPath("$.recipient").value(defaultTestRecipient));

        verify(mockTransformationService, times(1)).makeFormal(originalContent);
        verify(mockEmailService, times(1)).sendEmail(eq(defaultTestRecipient), anyString(), eq(transformedContent));
    }

    @Test
    void transformAndSendEmail_emptyContent_shouldReturnBadRequest() throws Exception {
        EmailApiController.EmailRequest request = new EmailApiController.EmailRequest();
        request.setContent(""); // Empty content

        mockMvc.perform(post("/api/email/transform-and-send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Email content cannot be empty."));

        verify(mockTransformationService, never()).makeFormal(anyString());
        verify(mockEmailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void transformAndSendEmail_nullContent_shouldReturnBadRequest() throws Exception {
        EmailApiController.EmailRequest request = new EmailApiController.EmailRequest();
        request.setContent(null); // Null content

        mockMvc.perform(post("/api/email/transform-and-send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Email content cannot be empty."));

        verify(mockTransformationService, never()).makeFormal(anyString());
        verify(mockEmailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void transformAndSendEmail_emailServiceThrowsError_shouldReturnInternalServerError() throws Exception {
        String originalContent = "some valid content";
        EmailApiController.EmailRequest request = new EmailApiController.EmailRequest();
        request.setContent(originalContent);

        when(mockTransformationService.makeFormal(originalContent)).thenReturn("Some transformed content.");
        doThrow(new RuntimeException("Failed to connect to mail server"))
            .when(mockEmailService).sendEmail(anyString(), anyString(), anyString());

        mockMvc.perform(post("/api/email/transform-and-send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Failed to send email: Failed to connect to mail server"));

        verify(mockTransformationService, times(1)).makeFormal(originalContent);
        verify(mockEmailService, times(1)).sendEmail(anyString(), anyString(), anyString());
    }
}
