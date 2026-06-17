package com.kiniot.uflex.api.iam.domain.exceptions;

public class UserTenantAlreadyAssignedException extends RuntimeException {
    public UserTenantAlreadyAssignedException() {
        super("User is already associated with a tenant");
    }
}
