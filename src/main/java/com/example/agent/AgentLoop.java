package com.example.agent;

import com.example.command.Action;
import com.example.command.AgentCommand;
import com.example.llm.LlmClient;
import com.example.llm.LlmCommandParser;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AgentLoop {

  private static final int MAX_ITERATIONS = 5;

  private final LlmClient llmClient;
  private final LlmCommandParser commandParser;
  private final Agent agent;

  public AgentLoop(
      LlmClient llmClient,
      LlmCommandParser commandParser,
      Agent agent
  ) {
    this.llmClient = llmClient;
    this.commandParser = commandParser;
    this.agent = agent;
  }

  public void run(String userRequest) {

    String previousResult = null;

    for (int iteration = 1; iteration <= MAX_ITERATIONS; iteration++) {

      try {

        log.info("--- [Ітерація {}/{}] ---", iteration, MAX_ITERATIONS);

        // 1. Ask LLM
        String llmResponse = llmClient.ask(userRequest, previousResult);
        log.info("<- Відповідь LLM (JSON): {}", llmResponse.replaceAll("\\s+", " "));

        // 2. Parse LLM response
        AgentDecision decision = commandParser.parse(llmResponse);

        // 3. Check whether task is finished by LLM
        if (decision.finished()) {
          log.info("✔ Задача успішно завершена (LLM finished).");
          return;
        }

        AgentCommand command = decision.command();

        if (command == null || command.action() == Action.UNKNOWN) {
          log.warn("⚠ Невідома або непоSupported дія (UNKNOWN). Зупиняємо цикл.");
          return;
        }

        // Авто-коригування для CREATE_DIRECTORY
        if (command.action() == Action.CREATE_DIRECTORY) {
          String dirName = command.source();

          // Якщо LLM переплутала поля і записала назву папки у destination, а файл у source
          if (command.destination() != null && !command.destination().isBlank()) {
            // Якщо source схожий на файл (має розширення), а destination ні — міняємо їх місцями
            if (command.source() != null && command.source().contains(".")) {
              dirName = command.destination();
            }
          }
          command = new AgentCommand(Action.CREATE_DIRECTORY, dirName, null);
        }

        // Авто-коригування для READ та LIST
        if (command.action() == Action.READ || command.action() == Action.LIST) {
          if (command.destination() != null) {
            command = new AgentCommand(command.action(), command.source(), null);
          }
        }

        // 4. Execute command
        AgentResult result = agent.execute(command);

        if (result.success()) {
          log.info("✔ Результат файлової системи: {}", result.message());
        } else {
          log.warn("⚠ Помилка виконання: {}", result.message());
          // Якщо сталася помилка виконання, припиняємо цикл
          return;
        }

        if (result.data() != null) {
          log.info("📄 Данні:\n{}", result.data());
        }

        // 5. Якщо дія була поодинокою (не багатокроковим запитом), завершуємо
        if (!isMultiStepIntent(userRequest, command)) {
          log.info("✔ Задача успішно завершена.");
          return;
        }

        // 6. Формуємо результат для наступної ітерації (для багатокрокових запитів)
        previousResult = """
            Previous action: %s
            Previous action parameters: source=%s, destination=%s
            Execution result: success=%s, message=%s
            Result data: %s
            """.formatted(
            command.action(),
            command.source(),
            command.destination(),
            result.success(),
            result.message(),
            result.data()
        );

      } catch (Exception e) {
        log.error("✖ Збій у циклі агента", e);
        return;
      }
    }

    log.warn("⚠ Досягнуто ліміт ітерацій ({})", MAX_ITERATIONS);
  }

  private boolean isMultiStepIntent(String userRequest, AgentCommand lastCommand) {
    String lower = userRequest.toLowerCase();
    // Якщо запит містить сполучники "та", "і", "а потім" і перша дія була CREATE_DIRECTORY — це multi-step
    boolean hasAnd = lower.contains(" та ") || lower.contains(" і ") || lower.contains(" а потім ");
    return hasAnd && lastCommand.action() == Action.CREATE_DIRECTORY;
  }
}