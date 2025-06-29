package com.example.formalemail.controller;

import com.example.formalemail.service.EmailService;
import com.example.formalemail.service.TransformationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmailController.class)
// Provide a dummy value for the required property during testing
@TestPropertySource(properties = "email.recipient=test@example.com")
public class EmailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransformationService transformationService;

    @MockBean
    private EmailService emailService;

    @Test
    void transformEmail_validInput_shouldReturnSuccessMessage() throws Exception {
        String originalContent = "hey, check this out asap!";
        String transformedContent = "Hello, please check this out as soon as possible.";
        String recipient = "test@example.com"; // Matches @TestPropertySource

        when(transformationService.makeFormal(originalContent)).thenReturn(transformedContent);
        // No need to mock emailService.sendEmail if we are just verifying it's called,
        // but we can use doNothing() if we want to be explicit or if it had a return type.
        doNothing().when(emailService).sendEmail(eq(recipient), anyString(), eq(transformedContent));

        mockMvc.perform(post("/api/email/transform")
                .contentType(MediaType.TEXT_PLAIN) // Assuming raw text body
                .content(originalContent))
                .andExpect(status().isOk())
                .andExpect(content().string("Email transformed. Attempted to send to " + recipient + ". Transformed content: '" + transformedContent + "'"));

        verify(transformationService, times(1)).makeFormal(originalContent);
        verify(emailService, times(1)).sendEmail(recipient, "Transformed Email Message", transformedContent);
    }

    @Test
    void transformEmail_emptyInput_shouldReturnErrorMessage() throws Exception {
        mockMvc.perform(post("/api/email/transform")
                .contentType(MediaType.TEXT_PLAIN)
                .content(""))
                .andExpect(status().isOk()) // Controller currently returns 200 OK with message
                .andExpect(content().string("Email content cannot be empty."));

        verify(transformationService, never()).makeFormal(anyString());
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void transformEmail_nullInput_shouldTreatAsEmpty() throws Exception {
        // Spring MVC might convert a truly null body to an empty string
        // or reject based on @RequestBody(required=true) if that was set.
        // Here, we send an empty string to simulate what the controller receives for "no content".
        mockMvc.perform(post("/api/email/transform")
                .contentType(MediaType.TEXT_PLAIN)
                .content("")) // Testing behavior with empty string, as controller logic handles it
                .andExpect(status().isOk())
                .andExpect(content().string("Email content cannot be empty."));

        verify(transformationService, never()).makeFormal(anyString());
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }
}
