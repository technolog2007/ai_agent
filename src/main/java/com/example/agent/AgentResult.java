package com.example.agent;

public record AgentResult(
    boolean success,
    ResultType type,
    String message,
    String data
) {
}