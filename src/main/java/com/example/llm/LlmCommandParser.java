package com.example.llm;

import com.example.agent.AgentDecision;
import com.example.command.Action;
import com.example.command.AgentCommand;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LlmCommandParser {

  private final ObjectMapper objectMapper = new ObjectMapper();

  public AgentDecision parse(String jsonResponse) {
    try {
      JsonNode node = objectMapper.readTree(jsonResponse);

      String actionStr = node.has("action") && !node.get("action").isNull()
          ? node.get("action").asText()
          : null;

      String source = node.has("source") && !node.get("source").isNull()
          ? node.get("source").asText()
          : null;

      String destination = node.has("destination") && !node.get("destination").isNull()
          ? node.get("destination").asText()
          : null;

      Action action = parseActionSafely(actionStr);

      // Якщо дія null або UNKNOWN — спираємося на поле finished
      boolean finished = node.has("finished") && node.get("finished").asBoolean();

      // Захист: якщо є реальна дія для виконання, прапорець finished НЕ може бути true
      if (action != null && action != Action.UNKNOWN) {
        finished = false;
      }

      AgentCommand command = new AgentCommand(action, source, destination);
      return new AgentDecision(command, finished);

    } catch (Exception e) {
      log.error("Помилка парсингу JSON від LLM: {}", jsonResponse, e);
      return new AgentDecision(new AgentCommand(Action.UNKNOWN, null, null), false);
    }
  }

  private Action parseActionSafely(String actionStr) {
    if (actionStr == null || actionStr.isBlank()) {
      return null;
    }
    try {
      return Action.valueOf(actionStr.toUpperCase());
    } catch (IllegalArgumentException e) {
      log.warn("⚠ LLM повернула непідтримувану дію: '{}', заміна на UNKNOWN", actionStr);
      return Action.UNKNOWN;
    }
  }
}