package com.formalizer.jarapp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private JavaMailSender mockMailSender;

    @InjectMocks
    private EmailService emailService;

    private final String testFromEmail = "sender@example.com";
    private final String testToEmail = "recipient@example.com";
    private final String testSubject = "Test Subject";
    private final String testBody = "Test email body";

    @BeforeEach
    void setUp() {
        // Manually set the @Value field for testing, as Spring context is not fully loaded
        ReflectionTestUtils.setField(emailService, "fromEmailAddress", testFromEmail);
    }

    @Test
    void sendEmail_success() {
        // Arrange
        doNothing().when(mockMailSender).send(any(SimpleMailMessage.class));
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        assertDoesNotThrow(() -> emailService.sendEmail(testToEmail, testSubject, testBody));

        // Assert
        verify(mockMailSender, times(1)).send(messageCaptor.capture());
        SimpleMailMessage capturedMessage = messageCaptor.getValue();

        assertEquals(testFromEmail, capturedMessage.getFrom());
        assertNotNull(capturedMessage.getTo());
        assertEquals(1, capturedMessage.getTo().length);
        assertEquals(testToEmail, capturedMessage.getTo()[0]);
        assertEquals(testSubject, capturedMessage.getSubject());
        assertEquals(testBody, capturedMessage.getText());
    }

    @Test
    void sendEmail_mailSenderThrowsException_shouldThrowRuntimeException() {
        // Arrange
        doThrow(new RuntimeException("Simulated Mail Send Exception")).when(mockMailSender).send(any(SimpleMailMessage.class));

        // Act & Assert
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            emailService.sendEmail(testToEmail, testSubject, testBody);
        });
        assertTrue(thrown.getMessage().contains("Error sending email"));
        assertTrue(thrown.getCause().getMessage().contains("Simulated Mail Send Exception"));

        verify(mockMailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}
