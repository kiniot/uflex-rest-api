package com.kiniot.uflex.api.subscription.domain.model.queries;

import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;

public record GetSubscriptionByClinicQuery(ClinicId clinicId) {
}
