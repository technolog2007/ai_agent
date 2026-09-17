package com.example;

import com.example.agent.Agent;
import com.example.agent.AgentLoop;
import com.example.command.CommandValidator;
import com.example.filesystem.FileManager;
import com.example.llm.LlmClient;
import com.example.llm.LlmCommandParser;
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

    AgentLoop agentLoop =
        new AgentLoop(
            llmClient,
            commandParser,
            agent
        );

    Scanner scanner =
        new Scanner(System.in);

    while (true) {

      System.out.print("> ");

      String input =
          scanner.nextLine();

      if (input.equalsIgnoreCase("exit")) {
        break;
      }

      agentLoop.run(input);
    }

    scanner.close();

    log.info("Agent stopped.");
  }
}