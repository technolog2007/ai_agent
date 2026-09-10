package com.example;

import java.nio.file.Path;
import java.util.Scanner;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class App {

  public static void main(String[] args) {

    log.info("Local File Agent");
    log.info("Agent started.");

    Path allowedFolder =
        Path.of("C:\\Temp\\AgentTest");

    FileManager fileManager =
        new FileManager(allowedFolder);

    CommandValidator validator =
        new CommandValidator();

    Agent agent =
        new Agent(
            fileManager,
            validator
        );

    LlmClient llmClient =
        new LlmClient();

    LlmCommandParser commandParser =
        new LlmCommandParser();

    Scanner scanner =
        new Scanner(System.in);

    while (true) {

      System.out.print("> ");

      String input =
          scanner.nextLine();

      if (input.equalsIgnoreCase("exit")) {
        break;
      }

      try {

        String llmResponse =
            llmClient.ask(input);

        log.info(
            "LLM response: {}",
            llmResponse
        );

        AgentCommand command =
            commandParser.parse(
                llmResponse
            );

        log.info(
            "Command: {}",
            command
        );

        agent.execute(command);

      } catch (Exception e) {

        log.error(
            "Cannot process command",
            e
        );
      }
    }

    scanner.close();

    log.info("Agent stopped.");
  }
}