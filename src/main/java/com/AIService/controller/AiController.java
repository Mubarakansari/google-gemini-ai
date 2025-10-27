package com.AIService.controller;

import com.AIService.dto.ActivityAnalysisResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("ai")
@Slf4j
public class AiController {
    private final WebClient webClient;
    private static final String geminiApiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";
    private static final String geminiApiKey = "AIzaSyCDHN9_UrK0tYZJ6VsMPDgrFnsKLa08Or8";

    public AiController(WebClient webClient) {
        this.webClient = webClient;
    }

    @GetMapping("/check-log")
    public String checkLog() {
        log.info("🔄 Info Log call...");
        log.warn("⚠️Warning Log call...");
        log.error("❌ Error Log call...");
        log.debug("🔍  Debug Log call...");
        return "All Log create successfully";
    }

    @PostMapping("prompt/message")
    public ActivityAnalysisResponse getAiResponse(@RequestBody String message) throws JsonProcessingException {
        String prompt = createPrompt(message);
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", prompt)
                                )
                        )
                )
        );
        String responseText = webClient.post().uri(geminiApiUrl).header("Content-Type", "application/json")
                .header("x-goog-api-key", geminiApiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(responseText);
        JsonNode textNode = rootNode.path("candidates").get(0).path("content").path("parts").get(0).path("text");
        String jsonContent = textNode.asText().replaceAll("```json\\n", "").replaceAll("\\n", "").trim();
        JsonNode jsonResponse = objectMapper.readTree(jsonContent);
        log.info("jsonResponse {} >>", jsonResponse);
        ActivityAnalysisResponse response = objectMapper.readValue(jsonResponse.toString(), ActivityAnalysisResponse.class);
        log.warn("Analysis {} >>", response.analysis());
        log.error("improvements {} >>", response.improvements());
        log.info("safety>> {} ", response.safety());
        log.debug("suggestions {} >>", response.suggestions());
        log.trace("trace {} >>", response);
        return response;

    }

    public String createPrompt(String prompt) {
        return String.format("""
                        Analyze this message and provide detailed recommendations in the following EXACT JSON format:
                        {
                          "analysis": {
                            "overall": "Overall analysis here",
                          },
                          "improvements": [
                            {
                              "area": "Area name",
                              "recommendation": "Detailed recommendation"
                            }
                          ],
                          "suggestions": [
                            {
                              "description": "Detailed workout description"
                            }
                          ],
                          "safety": [
                            "Safety point 1",
                            "Safety point 2"
                          ]
                        }

                        Analyze this activity:
                        Additional Metrics: %s
                                
                        Provide detailed analysis focusing on performance, improvements.
                        Ensure the response follows the EXACT JSON format shown above.
                        """,
                prompt
        );
    }
}
