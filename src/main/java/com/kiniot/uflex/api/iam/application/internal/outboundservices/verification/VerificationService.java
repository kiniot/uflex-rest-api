package com.kiniot.uflex.api.iam.application.internal.outboundservices.verification;

import java.time.LocalDateTime;

public interface VerificationService {

    String generateCode();

    String generateCode(int length);

    String generateRandomPassword();

    String generateRandomPassword(int length);

    Integer generateExpirationMinutes();

    boolean verifyCode(String code, String expectedCode, LocalDateTime expirationTime);
}