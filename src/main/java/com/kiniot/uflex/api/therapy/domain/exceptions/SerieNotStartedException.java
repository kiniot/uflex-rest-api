package com.kiniot.uflex.api.therapy.domain.exceptions;

import com.kiniot.uflex.api.therapy.domain.model.valueobjects.SerieStatus;

public class SerieNotStartedException extends RuntimeException {

    private SerieNotStartedException(String message) {
        super(message);
    }

    public static SerieNotStartedException forSerie(String serieId, SerieStatus currentStatus) {
        return new SerieNotStartedException(
                "Serie %s cannot accept repetitions in status %s; it must be Started".formatted(serieId, currentStatus));
    }
}
