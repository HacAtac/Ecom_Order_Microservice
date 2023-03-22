package com.hacatac.orderservice.exception;

import com.hacatac.orderservice.external.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException ex) {
        return new ResponseEntity<>(new ErrorResponse().builder()
                .errorCode(ex.getErrorCode())
                .errorMessage(ex.getMessage())
                .build(),
                HttpStatus.valueOf(ex.getStatus()));
    }
}
