package com.ttvz.kodeling.common.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.net.URI;
import java.util.Comparator;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String ERROR_BASE_URI = "https://kodeling/errors/";

    public record FieldViolation(String field, String message) {
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        List<FieldViolation> errors = ex.getBindingResult().getFieldErrors().stream()
            .map(this::toViolation)
            .sorted(Comparator.comparing(FieldViolation::field).thenComparing(FieldViolation::message))
            .toList();
        return validationProblem(errors);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ProblemDetail handleMethodValidation(HandlerMethodValidationException ex) {
        List<FieldViolation> errors = ex.getParameterValidationResults().stream()
            .flatMap(result -> {
                String paramName = result.getMethodParameter().getParameterName();
                String field = paramName != null ? paramName : "parameter";
                return result.getResolvableErrors().stream()
                    .map(err -> new FieldViolation(
                        field,
                        err.getDefaultMessage() != null ? err.getDefaultMessage() : "invalid value"));
            })
            .sorted(Comparator.comparing(FieldViolation::field).thenComparing(FieldViolation::message))
            .toList();
        return validationProblem(errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {
        List<FieldViolation> errors = ex.getConstraintViolations().stream()
            .map(this::toViolation)
            .sorted(Comparator.comparing(FieldViolation::field).thenComparing(FieldViolation::message))
            .toList();
        return validationProblem(errors);
    }

    @ExceptionHandler({
        BadRequestException.class,
        HttpMessageNotReadableException.class,
        MethodArgumentTypeMismatchException.class,
        MissingServletRequestParameterException.class
    })
    public ProblemDetail handleBadRequest(Exception ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, resolveBadRequestDetail(ex));
        problem.setType(URI.create(ERROR_BASE_URI + "bad-request"));
        problem.setTitle("Bad request");
        return problem;
    }

    @ExceptionHandler({ResourceNotFoundException.class, NoResourceFoundException.class})
    public ProblemDetail handleNotFound(Exception ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setType(URI.create(ERROR_BASE_URI + "not-found"));
        problem.setTitle("Resource not found");
        return problem;
    }

    @ExceptionHandler(ConflictException.class)
    public ProblemDetail handleConflict(ConflictException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setType(URI.create(ERROR_BASE_URI + "conflict"));
        problem.setTitle("Conflict");
        return problem;
    }

    private ProblemDetail validationProblem(List<FieldViolation> errors) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "One or more fields are invalid");
        problem.setType(URI.create(ERROR_BASE_URI + "validation"));
        problem.setTitle("Validation failed");
        problem.setProperty("errors", errors);
        return problem;
    }

    private FieldViolation toViolation(FieldError fieldError) {
        String message = fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "invalid value";
        return new FieldViolation(fieldError.getField(), message);
    }

    private FieldViolation toViolation(ConstraintViolation<?> violation) {
        String path = violation.getPropertyPath().toString();
        int lastDot = path.lastIndexOf('.');
        String field = lastDot >= 0 ? path.substring(lastDot + 1) : path;
        return new FieldViolation(field, violation.getMessage());
    }

    private String resolveBadRequestDetail(Exception ex) {
        if (ex instanceof HttpMessageNotReadableException) {
            return "Malformed request body";
        }
        return ex.getMessage() != null ? ex.getMessage() : "Invalid request";
    }
}
