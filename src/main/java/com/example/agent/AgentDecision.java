package com.example.agent;

import com.example.command.AgentCommand;

public record AgentDecision(
    AgentCommand command,
    boolean finished
) {
}