package team.soma.teto.health.ai.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import team.soma.teto.health.analysis.job.domain.AiFailureCode;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

abstract class HttpAiClientSupport {

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    HttpAiClientSupport(AiProperties aiProperties, ObjectMapper objectMapper) {
        if (aiProperties.getBaseUrl() == null) {
            throw new IllegalStateException("AI_BASE_URL is required when app.ai.mode=real");
        }
        this.aiProperties = aiProperties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(aiProperties.getConnectTimeout())
                .build();
    }

    protected <T> T postJson(String path, String correlationId, Object request, Class<T> responseType) {
        byte[] body = writeJson(request);
        HttpRequest httpRequest = baseRequest(path, correlationId)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();
        return sendWithRetry(httpRequest, responseType, true);
    }

    protected <T> T postMultipart(String path, String correlationId, String boundary, HttpRequest.BodyPublisher body, Class<T> responseType) {
        HttpRequest httpRequest = baseRequest(path, correlationId)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(body)
                .build();
        return sendWithRetry(httpRequest, responseType, false);
    }

    protected byte[] writeJson(Object request) {
        try {
            return objectMapper.writeValueAsBytes(request);
        } catch (JacksonException exception) {
            throw new AiClientException(AiFailureCode.INVALID_AI_RESPONSE, "failed to serialize AI request");
        }
    }

    private HttpRequest.Builder baseRequest(String path, String correlationId) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(resolve(path))
                .timeout(aiProperties.getReadTimeout())
                .header(CORRELATION_ID_HEADER, correlationId);
        if (aiProperties.getApiKey() != null && !aiProperties.getApiKey().isBlank()) {
            builder.header(API_KEY_HEADER, aiProperties.getApiKey());
        }
        return builder;
    }

    private URI resolve(String path) {
        String normalizedPath = path.startsWith("/") ? path.substring(1) : path;
        String baseUrl = aiProperties.getBaseUrl().toString();
        String normalizedBaseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        return URI.create(normalizedBaseUrl + normalizedPath);
    }

    private <T> T sendWithRetry(HttpRequest request, Class<T> responseType, boolean retryable) {
        int attempts = retryable ? Math.max(1, aiProperties.getRetryCount() + 1) : 1;
        AiClientException lastException = null;
        for (int attempt = 0; attempt < attempts; attempt++) {
            try {
                return send(request, responseType);
            } catch (AiClientException exception) {
                if (!retryable || exception.failureCode() != AiFailureCode.AI_SERVER_ERROR) {
                    throw exception;
                }
                lastException = exception;
            }
        }
        throw lastException != null ? lastException : new AiClientException(AiFailureCode.UNKNOWN, "AI request failed");
    }

    private <T> T send(HttpRequest request, Class<T> responseType) {
        try {
            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() >= 400 && response.statusCode() < 500) {
                throw new AiClientException(AiFailureCode.INVALID_AI_RESPONSE, "AI request was rejected");
            }
            if (response.statusCode() >= 500) {
                throw new AiClientException(AiFailureCode.AI_SERVER_ERROR, "AI server error");
            }
            return readBody(response.body(), responseType);
        } catch (java.net.http.HttpTimeoutException exception) {
            throw new AiClientException(AiFailureCode.AI_TIMEOUT, "AI request timed out");
        } catch (IOException exception) {
            throw new AiClientException(AiFailureCode.AI_SERVER_ERROR, "AI request failed");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AiClientException(AiFailureCode.AI_TIMEOUT, "AI request was interrupted");
        }
    }

    private <T> T readBody(InputStream body, Class<T> responseType) {
        try (InputStream inputStream = body) {
            if (inputStream == null) {
                throw new AiClientException(AiFailureCode.INVALID_AI_RESPONSE, "AI response was empty");
            }
            return objectMapper.readValue(inputStream, responseType);
        } catch (AiClientException exception) {
            throw exception;
        } catch (IOException | JacksonException exception) {
            throw new AiClientException(AiFailureCode.INVALID_AI_RESPONSE, "AI response could not be parsed");
        }
    }
}
