package com.example.atlas.integration.bedrock.exception;

import com.example.atlas.exception.*;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.exception.ApiCallAttemptTimeoutException;
import software.amazon.awssdk.core.exception.ApiCallTimeoutException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.bedrockruntime.model.*;

import java.util.concurrent.CompletionException;
import java.util.function.Supplier;

@Component
public class BedrockExceptionTranslator {

    public <T> T execute(Supplier<T> operation) {
        try {
            return operation.get();
        } catch (BedrockRuntimeException | SdkClientException e) {
            throw translate(e);
        }
    }

    public RuntimeException translate(Throwable throwable) {
        Throwable cause = unwrap(throwable);

        return switch (cause) {
            case AccessDeniedException e ->
                    new ProviderAuthenticationException(e.getMessage(), e);

            case ModelTimeoutException e ->
                    new ProviderTimeoutException(e.getMessage(), e);

            case ApiCallTimeoutException e ->
                    new ProviderTimeoutException(e.getMessage(), e);

            case ApiCallAttemptTimeoutException e ->
                    new ProviderTimeoutException(e.getMessage(), e);

            case ValidationException e ->
                    new ProviderMisconfigurationException(e.getMessage(), e);

            case ResourceNotFoundException e ->
                    new ProviderMisconfigurationException(e.getMessage(), e);

            case ThrottlingException e ->
                    new ProviderUnavailableException(e.getMessage(), e);

            case ServiceUnavailableException e ->
                    new ProviderUnavailableException(e.getMessage(), e);

            case ModelErrorException e ->
                    new ProviderUnavailableException(e.getMessage(), e);

            case SdkClientException e ->
                    new ProviderUnavailableException(e.getMessage(), e);

            case BedrockRuntimeException e ->
                    new InternalException(e.getMessage(), e);

            default ->
                    new InternalException("Unexpected Bedrock failure", cause);
        };
    }

    private Throwable unwrap(Throwable throwable) {
        if (throwable instanceof CompletionException completionException && completionException.getCause() != null) {
            return completionException.getCause();
        }
        return throwable;
    }
}
