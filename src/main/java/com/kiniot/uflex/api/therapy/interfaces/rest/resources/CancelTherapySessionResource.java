package com.kiniot.uflex.api.therapy.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelTherapySessionResource(
        @NotBlank @Size(max = 500) String reason
) {}
