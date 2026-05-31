package com.kiniot.uflex.api.planning.domain.exceptions;

public class PatientClinicMismatchException extends RuntimeException {
    public PatientClinicMismatchException(String patientId, String clinicId) {
        super("Patient with id " + patientId + " does not belong to clinic " + clinicId);
    }
}
