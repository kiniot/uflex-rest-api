package com.kiniot.uflex.api.shared.interfaces.rest;

import com.kiniot.uflex.api.iam.domain.exceptions.UserWithEmailNotFound;
import com.kiniot.uflex.api.iam.domain.exceptions.UserWithIdNotFoundException;
import com.kiniot.uflex.api.iam.domain.exceptions.TenantAssignmentException;
import com.kiniot.uflex.api.organization.domain.exceptions.ClinicAlreadyRegisteredException;
import com.kiniot.uflex.api.organization.domain.exceptions.ClinicNotFoundException;
import com.kiniot.uflex.api.organization.domain.exceptions.CrossClinicAssignmentException;
import com.kiniot.uflex.api.organization.domain.exceptions.PatientAlreadyRegisteredException;
import com.kiniot.uflex.api.organization.domain.exceptions.PhysiotherapistAlreadyRegisteredException;
import com.kiniot.uflex.api.organization.domain.exceptions.PhysiotherapistLicenseInvalidException;
import com.kiniot.uflex.api.organization.domain.exceptions.ProfileNotFoundException;
import com.kiniot.uflex.api.organization.domain.exceptions.UserNotFoundException;
import com.kiniot.uflex.api.planning.domain.exceptions.DuplicateExerciseSeriesOrderException;
import com.kiniot.uflex.api.planning.domain.exceptions.DuplicateRoutineOrderException;
import com.kiniot.uflex.api.planning.domain.exceptions.DuplicateRoutineScheduleException;
import com.kiniot.uflex.api.planning.domain.exceptions.ExerciseClinicMismatchException;
import com.kiniot.uflex.api.planning.domain.exceptions.ExerciseWithIdNotFoundException;
import com.kiniot.uflex.api.planning.domain.exceptions.InvalidTreatmentPlanStatusTransitionException;
import com.kiniot.uflex.api.planning.domain.exceptions.OverlappingTreatmentPlanPeriodException;
import com.kiniot.uflex.api.planning.domain.exceptions.PatientAlreadyHasActiveTreatmentPlanException;
import com.kiniot.uflex.api.planning.domain.exceptions.PatientClinicMismatchException;
import com.kiniot.uflex.api.planning.domain.exceptions.PatientWithIdNotFoundException;
import com.kiniot.uflex.api.planning.domain.exceptions.RoutineWithOrderNotFoundException;
import com.kiniot.uflex.api.planning.domain.exceptions.TreatmentPlanWithIdNotFoundException;
import com.kiniot.uflex.api.shared.domain.exceptions.AuthenticatedUserClinicNotFoundException;
import com.kiniot.uflex.api.shared.interfaces.rest.resources.ErrorResource;
import com.kiniot.uflex.api.subscription.domain.exceptions.InvalidSubscriptionAmountFormatException;
import com.kiniot.uflex.api.subscription.domain.exceptions.StripeCheckoutSessionCreationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            PatientAlreadyRegisteredException.class,
            ClinicAlreadyRegisteredException.class,
            PhysiotherapistAlreadyRegisteredException.class,
            PhysiotherapistLicenseInvalidException.class,
            CrossClinicAssignmentException.class,
            TenantAssignmentException.class,
            ExerciseClinicMismatchException.class,
            DuplicateRoutineOrderException.class,
            DuplicateRoutineScheduleException.class,
            DuplicateExerciseSeriesOrderException.class,
            InvalidTreatmentPlanStatusTransitionException.class,
            PatientClinicMismatchException.class,
            PatientAlreadyHasActiveTreatmentPlanException.class,
            OverlappingTreatmentPlanPeriodException.class
    })
    public ResponseEntity<ErrorResource> handleConflictExceptions(RuntimeException exception, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.CONFLICT, exception.getMessage(), request);
    }

    @ExceptionHandler({
            ClinicNotFoundException.class,
            UserNotFoundException.class,
            ProfileNotFoundException.class,
            UserWithIdNotFoundException.class,
            UserWithEmailNotFound.class,
            TreatmentPlanWithIdNotFoundException.class,
            ExerciseWithIdNotFoundException.class,
            RoutineWithOrderNotFoundException.class,
            PatientWithIdNotFoundException.class,
            AuthenticatedUserClinicNotFoundException.class
    })
    public ResponseEntity<ErrorResource> handleNotFoundExceptions(RuntimeException exception, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResource> handleIllegalArgumentException(IllegalArgumentException exception, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
    }

    @ExceptionHandler(InvalidSubscriptionAmountFormatException.class)
    public ResponseEntity<ErrorResource> handleInvalidSubscriptionAmountFormatException(
            InvalidSubscriptionAmountFormatException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
    }

    @ExceptionHandler(StripeCheckoutSessionCreationException.class)
    public ResponseEntity<ErrorResource> handleStripeCheckoutSessionCreationException(
            StripeCheckoutSessionCreationException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(HttpStatus.BAD_GATEWAY, exception.getMessage(), request);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResource> handleIllegalStateException(IllegalStateException exception, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.CONFLICT, exception.getMessage(), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResource> handleAccessDeniedException(AccessDeniedException exception, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, "You do not have permission to access this resource", request);
    }

    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<ErrorResource> handleErrorResponseException(ErrorResponseException exception, HttpServletRequest request) {
        var status = HttpStatus.valueOf(exception.getStatusCode().value());
        var detail = exception.getBody().getDetail();
        if (detail == null || detail.isBlank()) {
            detail = exception.getMessage();
        }
        return buildErrorResponse(status, detail, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResource> handleUnhandledException(Exception exception, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request);
    }

    private ResponseEntity<ErrorResource> buildErrorResponse(HttpStatus status, String detail, HttpServletRequest request) {
        var errorResource = new ErrorResource(
                detail,
                status.value(),
                status.getReasonPhrase(),
                OffsetDateTime.now().toString(),
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(errorResource);
    }
}
