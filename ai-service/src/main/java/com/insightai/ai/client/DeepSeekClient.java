package com.insightai.ai.client;

import com.insightai.ai.config.DeepSeekProperties;
import com.insightai.ai.dto.ApiErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class DeepSeekClient {

    private final WebClient webClient;
    private final DeepSeekProperties properties;

    public DeepSeekClient(WebClient deepSeekWebClient, DeepSeekProperties properties) {
        this.webClient = deepSeekWebClient;
        this.properties = properties;
    }

    @Retryable(
            retryFor = { RuntimeException.class },
            maxAttemptsExpression = "#{@deepSeekProperties.maxRetries}",
            backoff = @Backoff(delayExpression = "#{@deepSeekProperties.retryDelay}")
    )
    public String generateCompletion(String prompt, String systemPrompt) {
        log.info("Calling DeepSeek API with model: {}", properties.getModel());

        Map<String, Object> requestBody = Map.of(
                "model", properties.getModel(),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.7,
                "max_tokens", 2000
        );

        try {
            DeepSeekResponse response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, clientResponse ->
                            handleError(clientResponse, "Client error"))
                    .onStatus(HttpStatusCode::is5xxServerError, clientResponse ->
                            handleError(clientResponse, "Server error"))
                    .bodyToMono(DeepSeekResponse.class)
                    .block();

            if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
                return response.getChoices().get(0).getMessage().getContent();
            }
            throw new RuntimeException("Empty response from DeepSeek API");
        } catch (Exception e) {
            log.error("DeepSeek API call failed: {}", e.getMessage());
            throw new RuntimeException("Failed to generate completion", e);
        }
    }

    private Mono<? extends Throwable> handleError(ClientResponse response, String context) {
        return response.bodyToMono(String.class)
                .flatMap(body -> {
                    log.error("{}: {} - {}", context, response.statusCode().value(), body);
                    return Mono.error(new RuntimeException(context + ": " + body));
                });
    }

    public String generateJsonCompletion(String prompt, String systemPrompt) {
        log.info("Calling DeepSeek API for JSON response");

        Map<String, Object> requestBody = Map.of(
                "model", properties.getModel(),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt + "\n\nIMPORTANT: Respond ONLY with valid JSON."),
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.3,
                "max_tokens", 3000,
                "response_format", Map.of("type", "json_object")
        );

        try {
            DeepSeekResponse response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(status -> status.value() >= 400, clientResponse ->
                            clientResponse.bodyToMono(String.class)
                                    .flatMap(body -> {
                                        log.error("DeepSeek API error: {} - {}", clientResponse.statusCode().value(), body);
                                        return Mono.error(new RuntimeException("API error: " + body));
                                    }))
                    .bodyToMono(DeepSeekResponse.class)
                    .block();

            if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
                return response.getChoices().get(0).getMessage().getContent();
            }
            throw new RuntimeException("Empty response from DeepSeek API");
        } catch (Exception e) {
            log.error("DeepSeek JSON API call failed: {}", e.getMessage());
            throw new RuntimeException("Failed to generate JSON completion", e);
        }
    }

    @lombok.Data
    public static class DeepSeekResponse {
        private String id;
        private String object;
        private int created;
        private String model;
        private List<Choice> choices;
        private Usage usage;

        @lombok.Data
        public static class Choice {
            private int index;
            private Message message;
            private String finishReason;
        }

        @lombok.Data
        public static class Message {
            private String role;
            private String content;
        }

        @lombok.Data
        public static class Usage {
            private int promptTokens;
            private int completionTokens;
            private int totalTokens;
        }
    }
}