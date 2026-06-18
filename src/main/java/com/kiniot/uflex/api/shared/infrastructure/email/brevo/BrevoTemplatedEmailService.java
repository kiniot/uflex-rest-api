package com.kiniot.uflex.api.shared.infrastructure.email.brevo;

import java.util.Map;

public interface BrevoTemplatedEmailService {
    void sendEmail(String to, String subject, String templateName, Map<String, Object> variables);
}
