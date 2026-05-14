package com.kiniot.uflex.api.iam.infrastructure.verification.otp.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "otp.verification")
public class VerificationConfiguration {
    private Integer expirationMinutes = 15;
    private Integer codeLength = 6;
    private Integer passwordLength = 12;
}