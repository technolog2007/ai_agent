package com.example;

import com.fasterxml.jackson.databind.ObjectMapper;

public class LlmCommandParser {

  private final ObjectMapper objectMapper;

  public LlmCommandParser() {
    this.objectMapper =
        new ObjectMapper();
  }

  public AgentCommand parse(String json)
      throws Exception {

    LlmCommandResponse response =
        objectMapper.readValue(
            json,
            LlmCommandResponse.class
        );

    if (response.action() == null) {
      throw new IllegalArgumentException(
          "LLM response does not contain action"
      );
    }

    if (response.action() == null) {

      throw new IllegalArgumentException(
          "LLM could not understand the request"
      );
    }

    Action action;

    try {

      action =
          Action.valueOf(
              response.action()
                  .toUpperCase()
          );

    } catch (IllegalArgumentException e) {

      throw new IllegalArgumentException(
          "LLM returned unsupported action: "
              + response.action(),
          e
      );
    }

    return new AgentCommand(
        action,
        response.source(),
        response.destination()
    );
  }
}