package com.kiniot.uflex.api.iam.infrastructure.email.javamailsender.services;

import com.kiniot.uflex.api.iam.infrastructure.email.javamailsender.UserNotificationEmailService;
import com.kiniot.uflex.api.shared.infrastructure.email.brevo.BrevoTemplatedEmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class NotificationEmailServiceImpl implements UserNotificationEmailService {

    private final BrevoTemplatedEmailService templatedEmailService;
    private static final Logger log = LoggerFactory.getLogger(NotificationEmailServiceImpl.class);

    public NotificationEmailServiceImpl(BrevoTemplatedEmailService templatedEmailService) {
        this.templatedEmailService = templatedEmailService;
    }

    @Override
    public void sendVerificationEmail(String to, String code, int expirationMinutes) {
        sendTemplatedEmail(to, "Confirma tu cuenta de uFlex", "es/email/verification-email",
                Map.of("code", code, "expirationMinutes", expirationMinutes));
    }

    @Override
    public void sendPasswordResetEmail(String to, String code, int expirationMinutes) {
        sendTemplatedEmail(to, "Recupera tu contraseña", "es/email/reset-password-email",
                Map.of("code", code, "expirationMinutes", expirationMinutes));
    }

    @Override
    public void sendTemporaryPasswordEmail(String to, String temporaryPassword) {
        sendTemplatedEmail(to, "Bienvenido a uFlex", "es/email/temporary-password-email",
                Map.of("temporaryPassword", temporaryPassword));
    }

    private void sendTemplatedEmail(String to, String subject, String template, Map<String, Object> variables) {
        log.info("Sending email '{}' to {} using template {}", subject, to, template);
        templatedEmailService.sendEmail(to, subject, template, variables);
    }
}
