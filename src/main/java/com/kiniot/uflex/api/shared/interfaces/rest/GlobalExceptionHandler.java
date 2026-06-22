package com.kiniot.uflex.api.shared.interfaces.rest;

import com.kiniot.uflex.api.device.domain.exceptions.DeviceAlreadyRegisteredException;
import com.kiniot.uflex.api.device.domain.exceptions.DeviceAssignmentNotAllowedException;
import com.kiniot.uflex.api.device.domain.exceptions.DeviceClinicMismatchException;
import com.kiniot.uflex.api.device.domain.exceptions.DeviceNotFoundException;
import com.kiniot.uflex.api.device.domain.exceptions.DeviceNotInStockException;
import com.kiniot.uflex.api.media.domain.exceptions.MediaAssetNotFoundException;
import com.kiniot.uflex.api.media.domain.exceptions.MediaFileTooLargeException;
import com.kiniot.uflex.api.media.domain.exceptions.MediaStorageException;
import com.kiniot.uflex.api.media.domain.exceptions.MediaUploadNotConfirmableException;
import com.kiniot.uflex.api.media.domain.exceptions.UnsupportedMediaContentTypeException;
import com.kiniot.uflex.api.iam.domain.exceptions.EdgeServiceAccountAlreadyExistsException;
import com.kiniot.uflex.api.iam.domain.exceptions.EmailAlreadyInUseException;
import com.kiniot.uflex.api.iam.domain.exceptions.InvalidCredentialsException;
import com.kiniot.uflex.api.iam.domain.exceptions.RoleNotFoundException;
import com.kiniot.uflex.api.iam.domain.exceptions.UserWithEmailNotFound;
import com.kiniot.uflex.api.iam.domain.exceptions.UserWithIdNotFoundException;
import com.kiniot.uflex.api.iam.domain.exceptions.TenantAssignmentException;
import com.kiniot.uflex.api.iam.domain.exceptions.UserTenantAlreadyAssignedException;
import com.kiniot.uflex.api.iam.domain.exceptions.UserTenantNotAssignedException;
import com.kiniot.uflex.api.organization.domain.exceptions.ClinicAlreadyRegisteredException;
import com.kiniot.uflex.api.organization.domain.exceptions.ClinicNotFoundException;
import com.kiniot.uflex.api.organization.domain.exceptions.CrossClinicAssignmentException;
import com.kiniot.uflex.api.organization.domain.exceptions.PatientAccessDeniedException;
import com.kiniot.uflex.api.organization.domain.exceptions.PatientAlreadyRegisteredException;
import com.kiniot.uflex.api.organization.domain.exceptions.PatientNotFoundException;
import com.kiniot.uflex.api.organization.domain.exceptions.PatientOperationNotAllowedException;
import com.kiniot.uflex.api.organization.domain.exceptions.PhysiotherapistAlreadyRegisteredException;
import com.kiniot.uflex.api.organization.domain.exceptions.PhysiotherapistAlreadySuspendedException;
import com.kiniot.uflex.api.organization.domain.exceptions.PhysiotherapistClinicMismatchException;
import com.kiniot.uflex.api.organization.domain.exceptions.PhysiotherapistLicenseInvalidException;
import com.kiniot.uflex.api.organization.domain.exceptions.PhysiotherapistNotFoundException;
import com.kiniot.uflex.api.organization.domain.exceptions.PhysiotherapistPhotoAssetInvalidException;
import com.kiniot.uflex.api.organization.domain.exceptions.PhysiotherapistNotSuspendedException;
import com.kiniot.uflex.api.organization.domain.exceptions.PhysiotherapistOperationNotAllowedException;
import com.kiniot.uflex.api.organization.domain.exceptions.ProfileNotFoundException;
import com.kiniot.uflex.api.organization.domain.exceptions.SuspendedPhysiotherapistAssignmentException;
import com.kiniot.uflex.api.organization.domain.exceptions.UserNotFoundException;
import com.kiniot.uflex.api.planning.domain.exceptions.DuplicateExerciseSeriesOrderException;
import com.kiniot.uflex.api.planning.domain.exceptions.DuplicateRoutineOrderException;
import com.kiniot.uflex.api.planning.domain.exceptions.DuplicateRoutineScheduleException;
import com.kiniot.uflex.api.planning.domain.exceptions.ExerciseClinicMismatchException;
import com.kiniot.uflex.api.planning.domain.exceptions.ExerciseVideoAssetInvalidException;
import com.kiniot.uflex.api.planning.domain.exceptions.ExerciseWithIdNotFoundException;
import com.kiniot.uflex.api.planning.domain.exceptions.CurrentUserPatientProfileNotFoundException;
import com.kiniot.uflex.api.planning.domain.exceptions.InvalidTreatmentPlanStatusTransitionException;
import com.kiniot.uflex.api.planning.domain.exceptions.OverlappingTreatmentPlanPeriodException;
import com.kiniot.uflex.api.planning.domain.exceptions.PatientAlreadyHasActiveTreatmentPlanException;
import com.kiniot.uflex.api.planning.domain.exceptions.PatientClinicMismatchException;
import com.kiniot.uflex.api.planning.domain.exceptions.PatientWithIdNotFoundException;
import com.kiniot.uflex.api.planning.domain.exceptions.RoutineWithOrderNotFoundException;
import com.kiniot.uflex.api.planning.domain.exceptions.TreatmentPlanWithIdNotFoundException;
import com.kiniot.uflex.api.shared.domain.exceptions.AuthenticatedUserClinicNotFoundException;
import com.kiniot.uflex.api.therapy.domain.exceptions.HardwareNotReadyException;
import com.kiniot.uflex.api.therapy.domain.exceptions.IoTSensorsNotPlacedException;
import com.kiniot.uflex.api.therapy.domain.exceptions.PatientAlreadyInActiveSessionException;
import com.kiniot.uflex.api.therapy.domain.exceptions.RoutineNotCompletedException;
import com.kiniot.uflex.api.therapy.domain.exceptions.SerieNotFoundException;
import com.kiniot.uflex.api.therapy.domain.exceptions.SerieNotStartedException;
import com.kiniot.uflex.api.therapy.domain.exceptions.TherapySessionAlreadyFinalizedException;
import com.kiniot.uflex.api.therapy.domain.exceptions.TherapySessionNotFoundException;
import com.kiniot.uflex.api.therapy.domain.exceptions.TherapySessionNotInProgressException;
import com.kiniot.uflex.api.therapy.domain.exceptions.TherapySessionStillInProgressException;
import com.kiniot.uflex.api.shared.domain.exceptions.AuthenticatedTenantNotFoundException;
import com.kiniot.uflex.api.shared.domain.exceptions.AuthenticatedUserIdNotFoundException;
import com.kiniot.uflex.api.shared.interfaces.rest.resources.ErrorResource;
import com.kiniot.uflex.api.subscription.domain.exceptions.CurrentSubscriptionAlreadyExistsException;
import com.kiniot.uflex.api.subscription.domain.exceptions.InvalidSubscriptionAmountFormatException;
import com.kiniot.uflex.api.subscription.domain.exceptions.StripeCheckoutSessionCreationException;
import com.kiniot.uflex.api.subscription.domain.exceptions.SubscriptionNotFoundException;
import com.kiniot.uflex.api.subscription.domain.exceptions.SubscriptionOperationNotAllowedException;
import com.kiniot.uflex.api.subscription.domain.exceptions.SubscriptionPriceMismatchException;
import com.kiniot.uflex.api.subscription.domain.exceptions.TierNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Locale;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final ErrorResponseFactory errorResponseFactory;

    public GlobalExceptionHandler(ErrorResponseFactory errorResponseFactory) {
        this.errorResponseFactory = errorResponseFactory;
    }

    @ExceptionHandler({
            PatientAlreadyRegisteredException.class,
            ClinicAlreadyRegisteredException.class,
            PhysiotherapistAlreadyRegisteredException.class,
            PhysiotherapistAlreadySuspendedException.class,
            PhysiotherapistLicenseInvalidException.class,
            PhysiotherapistNotSuspendedException.class,
            SuspendedPhysiotherapistAssignmentException.class,
            CrossClinicAssignmentException.class,
            TenantAssignmentException.class,
            EmailAlreadyInUseException.class,
            EdgeServiceAccountAlreadyExistsException.class,
            UserTenantAlreadyAssignedException.class,
            UserTenantNotAssignedException.class,
            ExerciseClinicMismatchException.class,
            DeviceAlreadyRegisteredException.class,
            DeviceClinicMismatchException.class,
            DeviceAssignmentNotAllowedException.class,
            DeviceNotInStockException.class,
            DuplicateRoutineOrderException.class,
            DuplicateRoutineScheduleException.class,
            DuplicateExerciseSeriesOrderException.class,
            InvalidTreatmentPlanStatusTransitionException.class,
            PatientClinicMismatchException.class,
            com.kiniot.uflex.api.organization.domain.exceptions.PatientClinicMismatchException.class,
            PatientAccessDeniedException.class,
            PatientOperationNotAllowedException.class,
            PhysiotherapistClinicMismatchException.class,
            PhysiotherapistOperationNotAllowedException.class,
            PatientAlreadyHasActiveTreatmentPlanException.class,
            OverlappingTreatmentPlanPeriodException.class,
            CurrentSubscriptionAlreadyExistsException.class,
            SubscriptionOperationNotAllowedException.class,
            PatientAlreadyInActiveSessionException.class,
            TherapySessionAlreadyFinalizedException.class,
            TherapySessionNotInProgressException.class,
            TherapySessionStillInProgressException.class,
            HardwareNotReadyException.class,
            IoTSensorsNotPlacedException.class,
            RoutineNotCompletedException.class,
            SerieNotStartedException.class,
            MediaUploadNotConfirmableException.class
    })
    public ResponseEntity<ErrorResource> handleConflictExceptions(RuntimeException exception, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.CONFLICT, exception.getMessage(), request, exception);
    }

    @ExceptionHandler({
            ClinicNotFoundException.class,
            UserNotFoundException.class,
            ProfileNotFoundException.class,
            UserWithIdNotFoundException.class,
            UserWithEmailNotFound.class,
            RoleNotFoundException.class,
            TreatmentPlanWithIdNotFoundException.class,
            ExerciseWithIdNotFoundException.class,
            RoutineWithOrderNotFoundException.class,
            PatientWithIdNotFoundException.class,
            PatientNotFoundException.class,
            PhysiotherapistNotFoundException.class,
            CurrentUserPatientProfileNotFoundException.class,
            AuthenticatedUserClinicNotFoundException.class,
            TherapySessionNotFoundException.class,
            SerieNotFoundException.class,
            AuthenticatedUserClinicNotFoundException.class,
            AuthenticatedUserIdNotFoundException.class,
            AuthenticatedTenantNotFoundException.class,
            DeviceNotFoundException.class,
            MediaAssetNotFoundException.class,
            TierNotFoundException.class,
            SubscriptionNotFoundException.class
    })
    public ResponseEntity<ErrorResource> handleNotFoundExceptions(RuntimeException exception, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request, exception);
    }

    @ExceptionHandler(InvalidSubscriptionAmountFormatException.class)
    public ResponseEntity<ErrorResource> handleInvalidSubscriptionAmountFormatException(
            InvalidSubscriptionAmountFormatException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), request, exception);
    }

    @ExceptionHandler(StripeCheckoutSessionCreationException.class)
    public ResponseEntity<ErrorResource> handleStripeCheckoutSessionCreationException(
            StripeCheckoutSessionCreationException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(HttpStatus.BAD_GATEWAY, exception.getMessage(), request, exception);
    }

    @ExceptionHandler(MediaFileTooLargeException.class)
    public ResponseEntity<ErrorResource> handleMediaFileTooLargeException(
            MediaFileTooLargeException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(HttpStatus.PAYLOAD_TOO_LARGE, exception.getMessage(), request, exception);
    }

    @ExceptionHandler(MediaStorageException.class)
    public ResponseEntity<ErrorResource> handleMediaStorageException(
            MediaStorageException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(HttpStatus.BAD_GATEWAY, exception.getMessage(), request, exception);
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            InvalidCredentialsException.class,
            SubscriptionPriceMismatchException.class,
            UnsupportedMediaContentTypeException.class,
            PhysiotherapistPhotoAssetInvalidException.class,
            ExerciseVideoAssetInvalidException.class
    })
    public ResponseEntity<ErrorResource> handleBadRequestExceptions(RuntimeException exception, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), request, exception);
    }

    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            TypeMismatchException.class
    })
    public ResponseEntity<ErrorResource> handleMethodArgumentTypeMismatchException(
            Exception exception,
            HttpServletRequest request
    ) {
        String parameterName = "parameter";
        String expectedType = "valid value";
        String rejectedValue = "null";

        if (exception instanceof MethodArgumentTypeMismatchException methodArgumentTypeMismatchException) {
            parameterName = methodArgumentTypeMismatchException.getName();
            expectedType = methodArgumentTypeMismatchException.getRequiredType() != null
                    ? humanReadableTypeName(methodArgumentTypeMismatchException.getRequiredType())
                    : "valid value";
            rejectedValue = methodArgumentTypeMismatchException.getValue() != null
                    ? methodArgumentTypeMismatchException.getValue().toString()
                    : "null";
        } else if (exception instanceof TypeMismatchException typeMismatchException) {
            expectedType = typeMismatchException.getRequiredType() != null
                    ? humanReadableTypeName(typeMismatchException.getRequiredType())
                    : "valid value";
            rejectedValue = typeMismatchException.getValue() != null
                    ? typeMismatchException.getValue().toString()
                    : "null";
        }

        String message = "Parameter '%s' must be a valid %s. Received: \"%s\""
                .formatted(parameterName, expectedType, rejectedValue);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, message, request, exception);
    }

    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            ServletRequestBindingException.class
    })
    public ResponseEntity<ErrorResource> handleMissingServletRequestParameterException(
            Exception exception,
            HttpServletRequest request
    ) {
        String message;
        if (exception instanceof MissingServletRequestParameterException missingServletRequestParameterException) {
            message = "Required request parameter '%s' is missing"
                    .formatted(missingServletRequestParameterException.getParameterName());
        } else {
            message = exception.getMessage();
        }
        return buildErrorResponse(HttpStatus.BAD_REQUEST, message, request, exception);
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            HttpMessageConversionException.class
    })
    public ResponseEntity<ErrorResource> handleHttpMessageNotReadableException(
            Exception exception,
            HttpServletRequest request
    ) {
        String message = "Request body is malformed or contains invalid values";
        return buildErrorResponse(HttpStatus.BAD_REQUEST, message, request, exception);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResource> handleIllegalStateException(IllegalStateException exception, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.CONFLICT, exception.getMessage(), request, exception);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResource> handleAccessDeniedException(AccessDeniedException exception, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, "You do not have permission to access this resource", request, exception);
    }

    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<ErrorResource> handleErrorResponseException(ErrorResponseException exception, HttpServletRequest request) {
        var status = HttpStatus.valueOf(exception.getStatusCode().value());
        var detail = exception.getBody().getDetail();
        if (detail == null || detail.isBlank()) {
            detail = exception.getMessage();
        }
        return buildErrorResponse(status, detail, request, exception);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResource> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> "%s: %s".formatted(error.getField(), error.getDefaultMessage()))
                .reduce((first, second) -> first + "; " + second)
                .orElse("Request body validation failed");
        return buildErrorResponse(HttpStatus.BAD_REQUEST, message, request, exception);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResource> handleNoResourceFoundException(
            NoResourceFoundException exception,
            HttpServletRequest request
    ) {
        String message = "No endpoint found for %s %s".formatted(request.getMethod(), request.getRequestURI());
        return buildErrorResponse(HttpStatus.NOT_FOUND, message, request, exception);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResource> handleUnhandledException(Exception exception, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request, exception);
    }

    private ResponseEntity<ErrorResource> buildErrorResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            Throwable throwable
    ) {
        logException(status, message, request, throwable);
        return errorResponseFactory.buildResponse(status, message, request, throwable);
    }

    private void logException(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            Throwable throwable
    ) {
        var method = request.getMethod();
        var path = request.getRequestURI();
        if (status.is5xxServerError()) {
            log.error("{} {} -> {} {} | {}", method, path, status.value(), status.getReasonPhrase(), message, throwable);
            return;
        }
        log.warn("{} {} -> {} {} | {}", method, path, status.value(), status.getReasonPhrase(), message);
    }

    private String humanReadableTypeName(Class<?> type) {
        if (Integer.class.equals(type) || int.class.equals(type)) {
            return "integer";
        }
        if (Long.class.equals(type) || long.class.equals(type)) {
            return "long";
        }
        if (java.util.UUID.class.equals(type)) {
            return "UUID";
        }
        if (Boolean.class.equals(type) || boolean.class.equals(type)) {
            return "boolean";
        }
        return type.getSimpleName().toLowerCase(Locale.ROOT);
    }
}
