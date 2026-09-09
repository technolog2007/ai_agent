package com.example;

import java.io.IOException;
import java.nio.file.Files;
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

    try {

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

        case MOVE_MATCHING:
          moveMatching(
              command.source(),
              command.destination()
          );
          break;

        case CREATE_DIRECTORY:
          createDirectory(command.source());
          break;
      }

    } catch (IllegalArgumentException e) {

      log.warn(
          "Command rejected: {}",
          e.getMessage()
      );
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

      if (!fileManager.fileExists(fileName)) {

        log.warn(
            "File does not exist: {}",
            fileName
        );

        return;
      }

      String content =
          fileManager.readFile(fileName);

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

      if (!fileManager.fileExists(source)) {

        log.warn(
            "Source file does not exist: {}",
            source
        );

        return;
      }

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
  private void moveMatching(
      String pattern,
      String destination
  ) {

    try {

      List<Path> files =
          fileManager.findFiles(pattern);

      if (files.isEmpty()) {

        log.info(
            "No files found for pattern: {}",
            pattern
        );

        return;
      }

      if (!fileManager.directoryExists(destination)) {

        fileManager.createDirectory(destination);

        log.info(
            "Directory created: {}",
            destination
        );
      }

      for (Path file : files) {

        String fileName =
            file.getFileName().toString();

        String destinationPath =
            destination + "/" + fileName;

        fileManager.moveFile(
            fileName,
            destinationPath
        );

        log.info(
            "File moved: {} -> {}",
            fileName,
            destinationPath
        );
      }

    } catch (IOException e) {

      log.error(
          "Cannot move files matching: {}",
          pattern,
          e
      );
    }
  }
}