package dev.akemi.backend.exception;

import dev.akemi.backend.entity.RestBean;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ValidationController {

    @ExceptionHandler(ValidationException.class)
    public RestBean<Void> validateException(ValidationException validationException){
        log.warn("Resolve [{} : {}]", validationException.getClass().getName(),validationException.getMessage());
        return RestBean.failure(400,"请求参数有误");
    }
}
