package com.javarush.jira.common.error;

import lombok.AllArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BindException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
@AllArgsConstructor
public class RestExceptionHandler extends BasicExceptionHandler {

    @ExceptionHandler(BindException.class)
    public ProblemDetail bindException(BindException ex, WebRequest request) {
        return processException(ex, request,
                (e, type, msg, r) -> {
                    ProblemDetail pd = createProblemDetail(ex, type.status, msg);
                    pd.setProperty("invalid_params", errorMessageHandler.getErrorMap(ex.getBindingResult()));
                    return pd;
                });
    }

    //   https://howtodoinjava.com/spring-mvc/spring-problemdetail-errorresponse/#5-adding-problemdetail-to-custom-exceptions
    @ExceptionHandler(Exception.class)
    public ProblemDetail exception(Exception ex, WebRequest request) {
        return processException(ex, request,
                (e, type, msg, r) -> createProblemDetail(e, type.status, msg));
    }

    protected ProblemDetail createProblemDetail(Exception ex, HttpStatusCode status, String defaultDetail) {
        ErrorResponse.Builder builder = ErrorResponse.builder(ex, status, defaultDetail);
        return builder.build().updateAndGetBody(errorMessageHandler.getMessageSource(), LocaleContextHolder.getLocale());
    }
}
