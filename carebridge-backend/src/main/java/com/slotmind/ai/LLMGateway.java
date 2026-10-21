package com.slotmind.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Provider-neutral LLM gateway for CareBridge.
 * <p>
 * Supported providers:
 * - lmstudio: LM Studio OpenAI-compatible API
 * - bionic:   OpenAI-compatible local API
 * - ollama:   OpenAI-compatible Ollama API
 * - groq:     Groq OpenAI-compatible API
 * - gemini:   Google Gemini generateContent API
 * <p>
 * Default local model:
 * llama-3.2-3b-instruct
 */
@Component
public class LLMGateway {

    private static final Logger log =
            LoggerFactory.getLogger(LLMGateway.class);

    private static final String SYSTEM_PROMPT =
            "You are a careful healthcare appointment scheduling assistant. "
                    + "Follow scheduling constraints exactly. "
                    + "Return valid JSON only when JSON is requested. "
                    + "Never recommend a slot that is not available.";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String provider;
    private final String baseUrl;
    private final String apiKey;
    private final String model;

    public LLMGateway(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            @Value("${carebridge.ai.provider:lmstudio}")
            String provider,
            @Value("${carebridge.ai.base-url:}")
            String configuredBaseUrl,
            @Value("${carebridge.ai.api-key:}")
            String apiKey,
            @Value("${carebridge.ai.model:}")
            String configuredModel) {

        this.restClient = restClientBuilder.build();
        this.objectMapper = objectMapper;

        this.provider = provider == null
                ? "lmstudio"
                : provider.trim().toLowerCase();

        this.baseUrl = stripTrailingSlash(
                configuredBaseUrl == null || configuredBaseUrl.isBlank()
                        ? defaultBaseUrl(this.provider)
                        : configuredBaseUrl);

        this.apiKey = apiKey == null ? "" : apiKey.trim();

        this.model = configuredModel == null
                || configuredModel.isBlank()
                ? defaultModel(this.provider)
                : configuredModel.trim();

        log.info(
                "LLM Gateway initialized: provider={}, model={}, baseUrl={}",
                this.provider, this.model, this.baseUrl);
    }

    private static String defaultBaseUrl(String provider) {
        return switch (provider) {
            case "gemini" -> "https://generativelanguage.googleapis.com/v1beta";

            case "groq" -> "https://api.groq.com/openai/v1";

            case "ollama" -> "http://localhost:11434/v1";

            case "lmstudio" -> "http://localhost:1235/v1";

            case "bionic" -> "http://localhost:1234/v1";

            default -> "http://localhost:1235/v1";
        };
    }

    private static String defaultModel(String provider) {
        return switch (provider) {
            case "gemini" -> "gemini-2.5-flash";
            case "groq" -> "llama-3.3-70b-versatile";
            case "ollama" -> "qwen3-vl:8b";
            case "bionic" -> "Qwen3-VL-8B-Instruct-Q4_K_M-GGUF";
            case "lmstudio" -> "llama-3.2-3b-instruct";
            default -> "llama-3.2-3b-instruct";
        };
    }

    private static String stripTrailingSlash(String value) {
        String result = value.trim();

        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }

        return result;
    }

    /**
     * Sends a prompt to the configured LLM and returns its text response.
     */
    public String callLLM(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException(
                    "LLM prompt must not be blank");
        }

        long start = System.currentTimeMillis();

        log.info(
                "LLM request started: provider={}, model={}",
                provider, model);

        try {
            String answer = switch (provider) {
                case "gemini" -> callGemini(prompt);

                case "lmstudio", "bionic", "ollama", "groq" -> callOpenAiCompatible(prompt);

                default -> throw new IllegalStateException(
                        "Unsupported carebridge.ai.provider '"
                                + provider
                                + "'. Supported providers: "
                                + "lmstudio, bionic, ollama, groq, gemini");
            };

            log.info(
                    "LLM response received: provider={}, model={}, "
                            + "durationMs={}, responseLength={}",
                    provider,
                    model,
                    System.currentTimeMillis() - start,
                    answer.length());

            return answer;

        } catch (Exception exception) {
            log.error(
                    "LLM request failed: provider={}, model={}, "
                            + "durationMs={}, error={}",
                    provider,
                    model,
                    System.currentTimeMillis() - start,
                    exception.getMessage());

            throw exception instanceof IllegalStateException
                    ? (IllegalStateException) exception
                    : new IllegalStateException(
                    "LLM gateway call failed for provider '"
                            + provider + "' at " + baseUrl,
                    exception);
        }
    }

    /**
     * Calls LM Studio, Bionic, Ollama, or Groq through the
     * OpenAI-compatible Chat Completions API.
     */
    private String callOpenAiCompatible(String prompt) {

        Map<String, Object> request = new LinkedHashMap<>();

        request.put("model", model);
        request.put("temperature", 0.1);
        request.put("max_tokens", 500);
        request.put("stream", false);

        request.put(
                "messages",
                List.of(
                        Map.of(
                                "role", "system",
                                "content", SYSTEM_PROMPT),
                        Map.of(
                                "role", "user",
                                "content", prompt)));

        try {
            RestClient.RequestBodySpec requestSpec = restClient
                    .post()
                    .uri(baseUrl + "/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON);

            if (!apiKey.isBlank()) {
                requestSpec.header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + apiKey);
            }

            JsonNode response = requestSpec
                    .body(request)
                    .retrieve()
                    .body(JsonNode.class);

            JsonNode contentNode = response == null
                    ? null
                    : response.path("choices")
                    .path(0)
                    .path("message")
                    .path("content");

            if (contentNode == null
                    || !contentNode.isTextual()
                    || contentNode.asText().isBlank()) {
                throw new IllegalStateException(
                        "LLM response did not contain "
                                + "choices[0].message.content");
            }

            return contentNode.asText();

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "LLM gateway call failed for provider '"
                            + provider + "' at " + baseUrl
                            + ". Check endpoint, model ID, and API key.",
                    exception);
        }
    }

    /**
     * Calls the Google Gemini generateContent REST API.
     */
    private String callGemini(String prompt) {

        if (apiKey.isBlank()) {
            throw new IllegalStateException(
                    "Set CAREBRIDGE_AI_API_KEY when using Gemini");
        }

        Map<String, Object> request = Map.of(
                "contents",
                List.of(
                        Map.of(
                                "role", "user",
                                "parts",
                                List.of(Map.of("text", prompt)))),

                "systemInstruction",
                Map.of(
                        "parts",
                        List.of(Map.of("text", SYSTEM_PROMPT))),

                "generationConfig",
                Map.of(
                        "temperature", 0.1,
                        "maxOutputTokens", 500));

        String encodedKey = URLEncoder.encode(
                apiKey, StandardCharsets.UTF_8);

        String url = baseUrl
                + "/models/" + model
                + ":generateContent?key=" + encodedKey;

        try {
            JsonNode response = restClient
                    .post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(JsonNode.class);

            JsonNode parts = response == null
                    ? null
                    : response.path("candidates")
                    .path(0)
                    .path("content")
                    .path("parts");

            if (parts == null || !parts.isArray()) {
                throw new IllegalStateException(
                        "Gemini response did not contain "
                                + "candidates[0].content.parts");
            }

            StringBuilder answer = new StringBuilder();

            for (JsonNode part : parts) {
                if (part.path("text").isTextual()) {
                    answer.append(part.path("text").asText());
                }
            }

            if (answer.isEmpty()) {
                throw new IllegalStateException(
                        "Gemini returned no text content");
            }

            return answer.toString();

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Gemini gateway call failed. "
                            + "Check model ID, endpoint, and API key.",
                    exception);
        }
    }
}
