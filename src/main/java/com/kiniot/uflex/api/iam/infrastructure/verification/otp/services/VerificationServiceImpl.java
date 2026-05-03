package com.kiniot.uflex.api.iam.infrastructure.verification.otp.services;

import com.kiniot.uflex.api.iam.infrastructure.verification.otp.OtpSecureVerificationService;
import com.kiniot.uflex.api.iam.infrastructure.verification.otp.configuration.VerificationConfiguration;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class VerificationServiceImpl implements OtpSecureVerificationService {

    private final SecureRandom secureRandom = new SecureRandom();
    private final VerificationConfiguration configuration;

    public VerificationServiceImpl(VerificationConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public String generateCode() {
        int max = (int) Math.pow(10, configuration.getCodeLength()) - 1;
        int code = secureRandom.nextInt(max + 1);
        return String.format("%0" + configuration.getCodeLength() + "d", code);
    }

    @Override
    public String generateCode(int length) {
        int max = (int) Math.pow(10, length) - 1;
        int code = secureRandom.nextInt(max + 1);
        return String.format("%0" + length + "d", code);
    }

    @Override
    public String generateRandomPassword() {
        return generateRandomPassword(configuration.getPasswordLength());
    }

    @Override
    public String generateRandomPassword(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+";
        StringBuilder password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = secureRandom.nextInt(characters.length());
            password.append(characters.charAt(index));
        }
        return password.toString();
    }

    @Override
    public Integer generateExpirationMinutes() {
        return configuration.getExpirationMinutes();
    }

    @Override
    public boolean verifyCode(String code, String expectedCode, LocalDateTime expirationTime) {
        if (code == null || expectedCode == null || expirationTime == null) return false;
        if (LocalDateTime.now().isAfter(expirationTime)) return false;
        return code.equals(expectedCode);
    }
}