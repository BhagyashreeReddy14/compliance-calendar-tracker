package com.example.tool.service;

import com.example.tool.entity.Compliance;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @InjectMocks
    private EmailService emailService;

    @Mock
    private MimeMessage mimeMessage;

    private Compliance compliance;

    @BeforeEach
    void setUp() {
        compliance = new Compliance();
        compliance.setTitle("GDPR Audit");
        compliance.setDescription("Annual review");
        compliance.setStatus("PENDING");
        compliance.setDueDate(LocalDate.now().plusDays(5));

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    @DisplayName("should send a generic email")
    void sendEmail_success() {
        emailService.sendEmail("user@example.com", "Test Subject", "Test Content");

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("should send compliance created email")
    void sendComplianceCreatedEmail_success() {
        when(templateEngine.process(eq("emails/compliance-created"), any(Context.class)))
                .thenReturn("<html>Content</html>");

        emailService.sendComplianceCreatedEmail("user@example.com", compliance);

        verify(templateEngine).process(eq("emails/compliance-created"), any(Context.class));
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("should send overdue alert email")
    void sendOverdueEmail_success() {
        when(templateEngine.process(eq("emails/compliance-overdue"), any(Context.class)))
                .thenReturn("<html>Overdue Content</html>");

        emailService.sendOverdueEmail("user@example.com", compliance);

        verify(templateEngine).process(eq("emails/compliance-overdue"), any(Context.class));
        verify(mailSender).send(any(MimeMessage.class));
    }
}
