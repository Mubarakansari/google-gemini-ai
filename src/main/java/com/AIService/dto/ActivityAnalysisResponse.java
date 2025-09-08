package com.AIService.dto;

import java.util.List;

public record ActivityAnalysisResponse(
        Analysis analysis,
        List<Improvement> improvements,
        List<Suggestion> suggestions,
        List<String> safety
) {
}
