package com.kiniot.uflex.api.iam.domain.model.commands;

import com.kiniot.uflex.api.iam.domain.model.valueobjects.Email;
import com.kiniot.uflex.api.iam.domain.model.valueobjects.Password;

public record SignInCommand(
        Email email,
        Password password
) {
}