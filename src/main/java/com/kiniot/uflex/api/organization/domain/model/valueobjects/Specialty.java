package com.kiniot.uflex.api.organization.domain.model.valueobjects;

import java.util.Arrays;

public enum Specialty {
    TRAUMATOLOGICAL,
    NEUROLOGICAL,
    SPORTS,
    GENERAL;

    public static String[] valuesAsStrings() {
        return Arrays.stream(values())
                .map(Enum::name)
                .toArray(String[]::new);
    }
}
