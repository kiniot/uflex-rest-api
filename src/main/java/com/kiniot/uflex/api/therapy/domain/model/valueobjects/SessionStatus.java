package com.kiniot.uflex.api.therapy.domain.model.valueobjects;

import java.util.Objects;
import java.util.Set;

public enum SessionStatus {
    Pending, Ready, InProgress, Completed, Cancelled;

    /** Statuses in which a therapy session is still considered active (not terminal). */
    public static final Set<SessionStatus> ACTIVE_STATUSES = Set.of(Pending, Ready, InProgress);

    public boolean isActive() {
        return ACTIVE_STATUSES.contains(this);
    }

    public static SessionStatus of(String value) {
        Objects.requireNonNull(value, "sessionStatus must not be null");
        return valueOf(value);
    }

    public static SessionStatus fromNullable(String value) {
        return (value == null || value.isBlank()) ? null : of(value);
    }

    public static String toStringOrNull(SessionStatus status) {
        return status == null ? null : status.name();
    }
}
