package com.blitzar.account_holder_id_document.web.exception_handler;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.annotation.Status;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import io.micronaut.http.server.exceptions.response.ErrorContext;
import io.micronaut.http.server.exceptions.response.ErrorResponseProcessor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Produces
@Singleton
@Requires(classes = { IllegalArgumentException.class })
public class IllegalArgumentExceptionHandler implements ExceptionHandler<IllegalArgumentException, HttpResponse<?>> {

    private static final Logger logger = LoggerFactory.getLogger(IllegalArgumentExceptionHandler.class);

    private final ErrorResponseProcessor<?> errorResponseProcessor;

    @Inject
    public IllegalArgumentExceptionHandler(ErrorResponseProcessor<?> errorResponseProcessor) {
        this.errorResponseProcessor = errorResponseProcessor;
    }

    @Override
    @Status(value = HttpStatus.BAD_REQUEST)
    public HttpResponse<?> handle(HttpRequest request, IllegalArgumentException exception) {
        logger.error(exception.getMessage());

        return errorResponseProcessor.processResponse(ErrorContext.builder(request)
                .cause(exception)
                .errorMessage(exception.getMessage())
                .build(), HttpResponse.badRequest());
    }
}