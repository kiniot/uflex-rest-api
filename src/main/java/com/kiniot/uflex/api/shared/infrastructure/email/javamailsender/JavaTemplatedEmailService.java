package com.kiniot.uflex.api.shared.infrastructure.email.javamailsender;

import java.util.Map;

public interface JavaTemplatedEmailService {
    void sendEmail(String to, String subject, String templateName, Map<String, Object> variables);
}