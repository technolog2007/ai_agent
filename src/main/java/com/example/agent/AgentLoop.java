package com.example.agent;

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

    for (int iteration = 1;
        iteration <= MAX_ITERATIONS;
        iteration++) {

      try {

        log.info(
            "Agent iteration: {}",
            iteration
        );

        // 1. Ask LLM
        String llmResponse =
            llmClient.ask(
                userRequest,
                previousResult
            );

        log.info(
            "LLM response: {}",
            llmResponse
        );

        // 2. Parse LLM response
        AgentDecision decision =
            commandParser.parse(
                llmResponse
            );

        log.info(
            "Decision: {}",
            decision
        );

        // 3. Check whether task is finished
        if (decision.finished()) {

          log.info(
              "Task finished."
          );

          return;
        }

        // 4. Command must exist if task is not finished
        AgentCommand command =
            decision.command();

        if (command == null) {

          log.error(
              "LLM returned finished=false but command is null"
          );

          return;
        }

        log.info(
            "Command: {}",
            command
        );

        // 5. Execute command
        AgentResult result =
            agent.execute(command);

        if (result.success() && isTerminalCommand(command)) {

          log.info("Task completed successfully.");

          return;
        }

        log.info(
            "Agent result: {}",
            result.message()
        );

        if (result.data() != null) {

          log.info(
              "Agent data:\n{}",
              result.data()
          );
        }

        // 6. Prepare execution result
        //    for the next LLM iteration
        previousResult =
            """
            Previous action:
            %s
  
            Previous action parameters:
            source = %s
            destination = %s
  
            Execution result:
            success = %s
            message = %s
  
            Result data:
            %s
            """.formatted(
                command.action(),
                command.source(),
                command.destination(),
                result.success(),
                result.message(),
                result.data()
            );

      } catch (Exception e) {

        log.error(
            "Agent loop failed",
            e
        );

        return;
      }
    }

    log.warn(
        "Maximum number of agent iterations reached: {}",
        MAX_ITERATIONS
    );
  }

  private boolean isTerminalCommand(AgentCommand command) {

    return switch (command.action()) {
      case LIST,
          READ,
          MOVE,
          MOVE_MATCHING,
          CREATE_DIRECTORY -> true;

      case UNKNOWN -> false;
    };
  }
}