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

  public AgentResult execute(
      AgentCommand command
  ) {

    try {

      validator.validate(command);

      switch (command.action()) {

        case LIST:
          return listFiles();

        case READ:
          return readFile(
              command.source()
          );

        case MOVE:
          return moveFile(
              command.source(),
              command.destination()
          );

        case MOVE_MATCHING:
          return moveMatching(
              command.source(),
              command.destination()
          );

        case CREATE_DIRECTORY:
          return createDirectory(
              command.source()
          );

        case UNKNOWN:
          return new AgentResult(
              false,
              "Unknown command"
          );
      }

    } catch (IllegalArgumentException e) {

      log.warn(
          "Command rejected: {}",
          e.getMessage()
      );

      return new AgentResult(
          false,
          e.getMessage()
      );
    }

    return new AgentResult(
        false,
        "Command execution failed"
    );
  }

  private AgentResult listFiles() {

    try {

      List<Path> files =
          fileManager.listFiles();

      log.info("Files:");

      for (Path file : files) {

        log.info(
            " - {}",
            file.getFileName()
        );
      }

      return new AgentResult(
          true,
          "Files listed successfully"
      );

    } catch (IOException e) {

      log.error(
          "Cannot list files",
          e
      );

      return new AgentResult(
          false,
          "Cannot list files"
      );
    }
  }

  private AgentResult readFile(
      String fileName
  ) {

    try {

      String content =
          fileManager.readFile(fileName);

      log.info(
          "Content of {}:",
          fileName
      );

      log.info(content);

      return new AgentResult(
          true,
          "File read successfully: "
              + fileName
      );

    } catch (IOException e) {

      log.error(
          "Cannot read file: {}",
          fileName,
          e
      );

      return new AgentResult(
          false,
          "Cannot read file: "
              + fileName
      );
    }
  }

  private AgentResult moveFile(
      String source,
      String destination
  ) {

    try {

      /*
       * destination is a directory.
       * If it does not exist, create it.
       */
      if (!fileManager.directoryExists(
          destination
      )) {

        fileManager.createDirectory(
            destination
        );

        log.info(
            "Directory created: {}",
            destination
        );
      }

      /*
       * Extract the file name from source.
       *
       * Example:
       *
       * source = "test.txt"
       * fileName = "test.txt"
       *
       * source = "folder/test.txt"
       * fileName = "test.txt"
       */
      String fileName =
          Path.of(source)
              .getFileName()
              .toString();

      /*
       * Build destination path.
       *
       * destination = "Documents"
       * fileName = "test.txt"
       *
       * result:
       * Documents/test.txt
       */
      String destinationPath =
          destination + "/" + fileName;

      fileManager.moveFile(
          source,
          destinationPath
      );

      log.info(
          "File moved: {} -> {}",
          source,
          destinationPath
      );

      return new AgentResult(
          true,
          "File moved: "
              + source
              + " -> "
              + destinationPath
      );

    } catch (IOException e) {

      log.error(
          "Cannot move file: {} -> {}",
          source,
          destination,
          e
      );

      return new AgentResult(
          false,
          "Cannot move file: "
              + source
              + " -> "
              + destination
      );
    }
  }

  private AgentResult moveMatching(
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

        return new AgentResult(
            true,
            "No files found for pattern: "
                + pattern
        );
      }

      /*
       * Create destination directory
       * if it does not exist.
       */
      if (!fileManager.directoryExists(
          destination
      )) {

        fileManager.createDirectory(
            destination
        );

        log.info(
            "Directory created: {}",
            destination
        );
      }

      int movedCount = 0;

      for (Path file : files) {

        String fileName =
            file.getFileName()
                .toString();

        String destinationPath =
            destination + "/"
                + fileName;

        fileManager.moveFile(
            fileName,
            destinationPath
        );

        log.info(
            "File moved: {} -> {}",
            fileName,
            destinationPath
        );

        movedCount++;
      }

      return new AgentResult(
          true,
          "Moved "
              + movedCount
              + " file(s) matching "
              + pattern
      );

    } catch (IOException e) {

      log.error(
          "Cannot move files matching: {}",
          pattern,
          e
      );

      return new AgentResult(
          false,
          "Cannot move files matching: "
              + pattern
      );
    }
  }

  private AgentResult createDirectory(
      String directoryName
  ) {

    try {

      fileManager.createDirectory(
          directoryName
      );

      log.info(
          "Directory created: {}",
          directoryName
      );

      return new AgentResult(
          true,
          "Directory created: "
              + directoryName
      );

    } catch (IOException e) {

      log.error(
          "Cannot create directory: {}",
          directoryName,
          e
      );

      return new AgentResult(
          false,
          "Cannot create directory: "
              + directoryName
      );
    }
  }
}