package com.kiniot.uflex.api.iam.interfaces.rest.transform;

import com.kiniot.uflex.api.iam.domain.model.commands.SignInCommand;
import com.kiniot.uflex.api.iam.domain.model.valueobjects.Email;
import com.kiniot.uflex.api.iam.domain.model.valueobjects.Password;
import com.kiniot.uflex.api.iam.interfaces.rest.resources.SignInResource;

public class SignInCommandFromResourceAssembler {
    public static SignInCommand toCommandFromResource(SignInResource signInResource) {
        return new SignInCommand(new Email(signInResource.email()), new Password(signInResource.password()));
    }
}
