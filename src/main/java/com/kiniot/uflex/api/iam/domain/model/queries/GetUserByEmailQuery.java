package com.kiniot.uflex.api.iam.domain.model.queries;

import com.kiniot.uflex.api.iam.domain.model.valueobjects.Email;

public record GetUserByEmailQuery(
        Email email
) {
}