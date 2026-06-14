package com.kiniot.uflex.api.iam.domain.exceptions;

public class UserTenantNotAssignedException extends RuntimeException {
    public UserTenantNotAssignedException() {
        super("User is not associated with the provided tenant");
    }
}
