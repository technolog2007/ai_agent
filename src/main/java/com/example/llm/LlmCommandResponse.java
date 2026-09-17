package com.example.llm;

public record LlmCommandResponse(
    String action,
    String source,
    String destination,
    boolean finished
) {
}