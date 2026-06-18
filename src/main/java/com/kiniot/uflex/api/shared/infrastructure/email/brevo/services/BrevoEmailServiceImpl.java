package com.kiniot.uflex.api.shared.infrastructure.email.brevo.services;

import com.kiniot.uflex.api.shared.infrastructure.email.brevo.BrevoTemplatedEmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;
import java.util.Map;

@Service
public class BrevoEmailServiceImpl implements BrevoTemplatedEmailService {

    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    private final RestClient restClient;
    private final TemplateEngine templateEngine;

    @Value("${brevo.api-key}")
    private String apiKey;

    @Value("${brevo.from.address}")
    private String fromAddress;

    @Value("${brevo.from.name}")
    private String fromName;

    public BrevoEmailServiceImpl(TemplateEngine templateEngine) {
        this.restClient = RestClient.create();
        this.templateEngine = templateEngine;
    }

    @Override
    public void sendEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        String htmlContent = renderTemplate(templateName, variables);

        var body = Map.of(
                "sender", Map.of("name", fromName, "email", fromAddress),
                "to", List.of(Map.of("email", to)),
                "subject", subject,
                "htmlContent", htmlContent
        );

        try {
            restClient.post()
                    .uri(BREVO_API_URL)
                    .header("api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            throw new RuntimeException("Error sending email to %s via Brevo: ".formatted(to), e);
        }
    }

    private String renderTemplate(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        return templateEngine.process(templateName, context);
    }
}
