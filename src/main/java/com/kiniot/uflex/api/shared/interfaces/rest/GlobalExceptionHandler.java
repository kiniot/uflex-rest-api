package com.kiniot.uflex.api.shared.interfaces.rest;

import com.kiniot.uflex.api.iam.domain.exceptions.UserWithEmailNotFound;
import com.kiniot.uflex.api.iam.domain.exceptions.UserWithIdNotFoundException;
import com.kiniot.uflex.api.organization.domain.exceptions.ClinicAlreadyRegisteredException;
import com.kiniot.uflex.api.organization.domain.exceptions.ClinicNotFoundException;
import com.kiniot.uflex.api.organization.domain.exceptions.CrossClinicAssignmentException;
import com.kiniot.uflex.api.organization.domain.exceptions.PatientAlreadyRegisteredException;
import com.kiniot.uflex.api.organization.domain.exceptions.PhysiotherapistAlreadyRegisteredException;
import com.kiniot.uflex.api.organization.domain.exceptions.ProfileNotFoundException;
import com.kiniot.uflex.api.organization.domain.exceptions.UserNotFoundException;
import com.kiniot.uflex.api.planning.domain.exceptions.ExerciseWithIdNotFoundException;
import com.kiniot.uflex.api.planning.domain.exceptions.RoutineWithOrderNotFoundException;
import com.kiniot.uflex.api.planning.domain.exceptions.TreatmentPlanWithIdNotFoundException;
import com.kiniot.uflex.api.shared.domain.exceptions.AuthenticatedUserClinicNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.OffsetDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            PatientAlreadyRegisteredException.class,
            ClinicAlreadyRegisteredException.class,
            PhysiotherapistAlreadyRegisteredException.class,
            CrossClinicAssignmentException.class
    })
    public ProblemDetail handleConflictExceptions(RuntimeException exception, HttpServletRequest request) {
        return buildProblemDetail(HttpStatus.CONFLICT, exception.getMessage(), request);
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
            AuthenticatedUserClinicNotFoundException.class
    })
    public ProblemDetail handleNotFoundExceptions(RuntimeException exception, HttpServletRequest request) {
        return buildProblemDetail(HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgumentException(IllegalArgumentException exception, HttpServletRequest request) {
        return buildProblemDetail(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalStateException(IllegalStateException exception, HttpServletRequest request) {
        return buildProblemDetail(HttpStatus.CONFLICT, exception.getMessage(), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDeniedException(AccessDeniedException exception, HttpServletRequest request) {
        return buildProblemDetail(HttpStatus.FORBIDDEN, "You do not have permission to access this resource", request);
    }

    @ExceptionHandler(ErrorResponseException.class)
    public ProblemDetail handleErrorResponseException(ErrorResponseException exception, HttpServletRequest request) {
        var status = HttpStatus.valueOf(exception.getStatusCode().value());
        var detail = exception.getBody().getDetail();
        if (detail == null || detail.isBlank()) {
            detail = exception.getMessage();
        }
        return buildProblemDetail(status, detail, request);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnhandledException(Exception exception, HttpServletRequest request) {
        return buildProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request);
    }

    private ProblemDetail buildProblemDetail(HttpStatus status, String detail, HttpServletRequest request) {
        var problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(status.getReasonPhrase());
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", OffsetDateTime.now().toString());
        problemDetail.setProperty("path", request.getRequestURI());
        return problemDetail;
    }
}
