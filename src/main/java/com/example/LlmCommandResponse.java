package com.example;

public record LlmCommandResponse(
    String action,
    String source,
    String destination
) {
}