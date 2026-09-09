package com.example;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Agent {

  private final FileManager fileManager;
  private final CommandValidator validator;

  public Agent(
      FileManager fileManager,
      CommandValidator validator
  ) {
    this.fileManager = fileManager;
    this.validator = validator;
  }

  public void execute(AgentCommand command) {

    validator.validate(command);

    switch (command.action()) {

      case LIST:
        listFiles();
        break;

      case READ:
        readFile(command.source());
        break;

      case MOVE:
        moveFile(
            command.source(),
            command.destination()
        );
        break;

      case CREATE_DIRECTORY:
        createDirectory(command.source());
        break;
    }
  }

  private void listFiles() {

    try {

      List<Path> files = fileManager.listFiles();

      log.info("Files:");

      for (Path file : files) {
        log.info(" - {}", file.getFileName());
      }

    } catch (IOException e) {
      log.error("Cannot list files", e);
    }
  }

  private void readFile(String fileName) {

    try {

      String content = fileManager.readFile(fileName);

      log.info("Content of {}:", fileName);
      log.info(content);

    } catch (IOException e) {
      log.error(
          "Cannot read file: {}",
          fileName,
          e
      );
    }
  }

  private void moveFile(
      String source,
      String destination
  ) {

    try {

      fileManager.moveFile(
          source,
          destination
      );

      log.info(
          "File moved: {} -> {}",
          source,
          destination
      );

    } catch (IOException e) {

      log.error(
          "Cannot move file: {} -> {}",
          source,
          destination,
          e
      );
    }
  }

  private void createDirectory(String directoryName) {

    try {

      fileManager.createDirectory(directoryName);

      log.info(
          "Directory created: {}",
          directoryName
      );

    } catch (IOException e) {

      log.error(
          "Cannot create directory: {}",
          directoryName,
          e
      );
    }
  }
}