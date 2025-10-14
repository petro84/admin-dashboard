package com.petro.admin_dashboard.exception;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.petro.admin_dashboard.model.HttpResponse;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.EmptyStackException;
import java.util.List;
import java.util.stream.Collectors;

import static java.time.LocalDateTime.now;
import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class HandleException extends ResponseEntityExceptionHandler implements ErrorController {
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        return new ResponseEntity<>(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .reason(ex.getMessage())
                        .developerMessage(ex.getMessage())
                        .status(resolve(statusCode.value()))
                        .statusCode(statusCode.value())
                        .build(), statusCode);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        String fieldMessage = fieldErrors.stream().map(FieldError::getDefaultMessage).collect(Collectors.joining(", "));
        return new ResponseEntity<>(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .reason(fieldMessage)
                        .developerMessage(ex.getMessage())
                        .status(resolve(statusCode.value()))
                        .statusCode(statusCode.value())
                        .build(), statusCode);
    }

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public ResponseEntity<HttpResponse> SqlIntegrityConstraintViolationException(
            SQLIntegrityConstraintViolationException exception) {
        return new ResponseEntity<>(HttpResponse.builder()
                .timeStamp(now().toString())
                .reason(exception.getMessage().contains("Duplicate entry") ? "Information already exists"
                        : exception.getMessage())
                .developerMessage(exception.getMessage())
                .status(BAD_REQUEST)
                .statusCode(BAD_REQUEST.value())
                .build(), BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<HttpResponse> BadCredentialsException(BadCredentialsException exception) {
        return new ResponseEntity<>(HttpResponse.builder()
                .timeStamp(now().toString())
                .reason(exception.getMessage() + ", Incorrect email or password")
                .developerMessage(exception.getMessage())
                .status(BAD_REQUEST)
                .statusCode(BAD_REQUEST.value())
                .build(), BAD_REQUEST);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<HttpResponse> ApiException(ApiException exception) {
        return new ResponseEntity<>(HttpResponse.builder()
                .timeStamp(now().toString())
                .reason(exception.getMessage())
                .developerMessage(exception.getMessage())
                .status(BAD_REQUEST)
                .statusCode(BAD_REQUEST.value())
                .build(), BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<HttpResponse> AccessDeniedException(AccessDeniedException exception) {
        return new ResponseEntity<>(HttpResponse.builder()
                .timeStamp(now().toString())
                .reason(exception.getMessage())
                .developerMessage(exception.getMessage())
                .status(FORBIDDEN)
                .statusCode(FORBIDDEN.value())
                .build(), FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<HttpResponse> Exception(Exception exception) {
        return new ResponseEntity<>(HttpResponse.builder()
                .timeStamp(now().toString())
                .reason(exception.getMessage() != null
                        ? (exception.getMessage().contains("expected 1, actual 0") ? "Record not found"
                        : exception.getMessage())
                        : "Some error occurred.")
                .developerMessage(exception.getMessage())
                .status(INTERNAL_SERVER_ERROR)
                .statusCode(INTERNAL_SERVER_ERROR.value())
                .build(), INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(JWTDecodeException.class)
    public ResponseEntity<HttpResponse> JWTDecodeException(JWTDecodeException exception) {
        return new ResponseEntity<>(HttpResponse.builder()
                .timeStamp(now().toString())
                .reason("Could not decode token")
                .developerMessage(exception.getMessage())
                .status(INTERNAL_SERVER_ERROR)
                .statusCode(INTERNAL_SERVER_ERROR.value())
                .build(), INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(EmptyStackException.class)
    public ResponseEntity<HttpResponse> EmptyStackException(EmptyStackException exception) {
        return new ResponseEntity<>(HttpResponse.builder()
                .timeStamp(now().toString())
                .reason(exception.getMessage().contains("expected 1, actual 0") ? "Record not found"
                        : exception.getMessage())
                .developerMessage(exception.getMessage())
                .status(BAD_REQUEST)
                .statusCode(BAD_REQUEST.value())
                .build(), BAD_REQUEST);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<HttpResponse> DisabledException(DisabledException exception) {;
        return new ResponseEntity<>(HttpResponse.builder()
                .timeStamp(now().toString())
                .reason("User account is currently disabled")
                .developerMessage(exception.getMessage())
                .status(BAD_REQUEST)
                .statusCode(BAD_REQUEST.value())
                .build(), BAD_REQUEST);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<HttpResponse> LockedException(LockedException exception) {
        return new ResponseEntity<>(HttpResponse.builder()
                .timeStamp(now().toString())
                .reason("User account is currently locked")
                .developerMessage(exception.getMessage())
                .status(BAD_REQUEST)
                .statusCode(BAD_REQUEST.value())
                .build(), BAD_REQUEST);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<HttpResponse> DataAccessException(DataAccessException exception) {
        return new ResponseEntity<>(HttpResponse.builder()
                .timeStamp(now().toString())
                .reason(processErrorMessage(exception.getMessage()))
                .developerMessage(processErrorMessage(exception.getMessage()))
                .status(BAD_REQUEST)
                .statusCode(BAD_REQUEST.value())
                .build(), BAD_REQUEST);
    }

    private String processErrorMessage(String message) {
        if (message != null) {
            if (message.contains("Duplicate entry") && message.contains("AccountVerifications")) {
                return "You already verified your account.";
            }
            if (message.contains("Duplicate entry") && message.contains("ResetPasswordVerifications")) {
                return "We already sent you an email to reset your password.";
            }
            if (message.contains("Duplicate entry")) {
                return "Duplicate entry. Please try again.";
            }
        }

        return "Some error occurred";
    }
}
