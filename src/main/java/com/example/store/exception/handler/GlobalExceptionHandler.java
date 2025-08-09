package com.example.store.exception.handler;

import com.example.store.payload.response.ErrorResponse;
import com.example.store.exception.CustomerNotFoundException;
import com.example.store.exception.OrderNotFoundException;
import com.example.store.i18n.MessageKeys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.stream.Collectors;


@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderNotFound(
            OrderNotFoundException ex, HttpServletRequest request) {
        log.error("Order not found", ex);

        ErrorResponse errorResponse = ErrorResponse.of(
                ex.getMessage(),
                request.getRequestURI(),
                404
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCustomerNotFound(
            CustomerNotFoundException ex, HttpServletRequest request) {
        log.error("Customer not found", ex);

        ErrorResponse errorResponse = ErrorResponse.of(
                ex.getMessage(),
                request.getRequestURI(),
                404
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFound(
            ProductNotFoundException ex, HttpServletRequest request) {
        log.error("Product not found", ex);

        ErrorResponse errorResponse = ErrorResponse.of(
                ex.getMessage(),
                request.getRequestURI(),
                404
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        log.error("Invalid argument", ex);

        ErrorResponse errorResponse = ErrorResponse.of(
                ex.getMessage(),
                request.getRequestURI(),
                400
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        log.error("Data integrity violation", ex);

        String message = messageSource.getMessage(
                MessageKeys.ERROR.DATA_INTEGRITY_DUPLICATE,
                null,
                LocaleContextHolder.getLocale());

        ErrorResponse errorResponse = ErrorResponse.of(
                message,
                request.getRequestURI(),
                400
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, HttpServletRequest request) {
        log.error("Unexpected error", ex);

        String message = messageSource.getMessage(
                MessageKeys.ERROR.UNEXPECTED,
                null,
                LocaleContextHolder.getLocale());

        ErrorResponse errorResponse = ErrorResponse.of(
                message,
                request.getRequestURI(),
                500
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @Override
    @SuppressWarnings("NullableProblems")
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            @NotNull MethodArgumentNotValidException ex,
            @NotNull HttpHeaders headers,
            @NotNull HttpStatusCode status,
            @NotNull WebRequest request) {

        log.error("Validation failed", ex);
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        String path = request.getDescription(false).replace("uri=", "");

        String message = messageSource.getMessage(
                MessageKeys.ERROR.VALIDATION_FAILED,
                new Object[]{details},
                LocaleContextHolder.getLocale());

        ErrorResponse errorResponse = ErrorResponse.of(
                message,
                path,
                400
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }
}