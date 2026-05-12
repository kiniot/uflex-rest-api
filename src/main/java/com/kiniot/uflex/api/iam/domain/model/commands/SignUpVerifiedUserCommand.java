package com.kiniot.uflex.api.iam.domain.model.commands;

import com.kiniot.uflex.api.iam.domain.model.entities.Role;
import com.kiniot.uflex.api.iam.domain.model.valueobjects.Email;

import java.util.List;

public record SignUpVerifiedUserCommand(
        Email emailAddress,
        String password,
        List<Role> roles
) {
}