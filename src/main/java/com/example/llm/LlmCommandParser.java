package com.example.llm;

import com.example.agent.AgentDecision;
import com.example.command.Action;
import com.example.command.AgentCommand;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LlmCommandParser {

  private final ObjectMapper objectMapper;

  public LlmCommandParser() {
    this.objectMapper = new ObjectMapper();
  }

  public AgentDecision parse(String json)
      throws Exception {

    LlmCommandResponse response =
        objectMapper.readValue(
            json,
            LlmCommandResponse.class
        );

    /*
     * Якщо LLM повідомила,
     * що задача завершена,
     * команда нам більше не потрібна.
     */
    if (response.finished()) {

      return new AgentDecision(
          null,
          true
      );
    }

    /*
     * Якщо задача НЕ завершена,
     * action обов'язково повинен бути присутній.
     */
    if (isBlank(response.action())) {

      throw new IllegalArgumentException(
          "LLM did not provide an action"
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

    /*
     * LLM може повернути:
     *
     * "null"
     *
     * замість:
     *
     * null
     *
     * Нормалізуємо це до Java null.
     */
    String source =
        normalizeNull(response.source());

    String destination =
        normalizeNull(response.destination());

    AgentCommand command =
        new AgentCommand(
            action,
            source,
            destination
        );

    return new AgentDecision(
        command,
        false
    );
  }

  private String normalizeNull(String value) {

    if (value == null) {
      return null;
    }

    if (value.equalsIgnoreCase("null")) {
      return null;
    }

    return value;
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}