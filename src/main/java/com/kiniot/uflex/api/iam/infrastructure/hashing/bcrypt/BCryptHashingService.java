package com.kiniot.uflex.api.iam.infrastructure.hashing.bcrypt;

import com.kiniot.uflex.api.iam.application.internal.outboundservices.hashing.HashingService;
import org.springframework.security.crypto.password.PasswordEncoder;

public interface BCryptHashingService extends HashingService, PasswordEncoder {
}