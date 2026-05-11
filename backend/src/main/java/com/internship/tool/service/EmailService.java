package com.internship.tool.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public void sendCreationNotification(String to, String title) {
        Context context = new Context();
        context.setVariable("title", title);
        String process = templateEngine.process("create-notification", context);
        sendEmail(to, "New Compliance Record Assigned", process);
    }

    public void sendOverdueAlert(String to, String title) {
        Context context = new Context();
        context.setVariable("title", title);
        String process = templateEngine.process("overdue-alert", context);
        sendEmail(to, "Compliance Record Overdue Alert", process);
    }

    private void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setText(body, true);
            helper.setTo(to);
            helper.setSubject(subject);
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            // Log error
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }
}
