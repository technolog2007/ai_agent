package com.example;

import java.nio.file.Path;
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

    // 1. Показати файли
    agent.execute(
        new AgentCommand(
            Action.LIST,
            null,
            null
        )
    );

    // 2. Прочитати файл
    agent.execute(
        new AgentCommand(
            Action.READ,
            "test.txt",
            null
        )
    );

    // 3. Створити папку
    agent.execute(
        new AgentCommand(
            Action.CREATE_DIRECTORY,
            "Documents",
            null
        )
    );

    // 4. Перемістити файл
    agent.execute(
        new AgentCommand(
            Action.MOVE,
            "test.txt",
            "Documents/test.txt"
        )
    );
  }
}