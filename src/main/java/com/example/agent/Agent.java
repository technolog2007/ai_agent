package com.example.agent;

import com.example.command.AgentCommand;
import com.example.command.Action;
import com.example.command.CommandValidator;
import com.example.filesystem.FileManager;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

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

  public AgentResult execute(AgentCommand command) {

    try {

      // 1. Перевіряємо команду
      validator.validate(command);

      // 2. Виконуємо команду
      return switch (command.action()) {

        case LIST ->
            listFiles();

        case READ ->
            readFile(command);

        case MOVE ->
            moveFile(command);

        case MOVE_MATCHING ->
            moveMatching(command);

        case CREATE_DIRECTORY ->
            createDirectory(command);

        case UNKNOWN ->
            new AgentResult(
                false,
                ResultType.VALIDATION_ERROR,
                "Unknown or unsupported command",
                null
            );
      };

    } catch (IllegalArgumentException e) {

      // Команда сформована неправильно
      return new AgentResult(
          false,
          ResultType.VALIDATION_ERROR,
          e.getMessage(),
          null
      );

    } catch (IOException e) {

      // Команда правильна, але операцію виконати не вдалося
      return new AgentResult(
          false,
          ResultType.EXECUTION_ERROR,
          e.getMessage(),
          null
      );
    }
  }

  private AgentResult listFiles()
      throws IOException {

    List<Path> files =
        fileManager.listFiles();

    String data =
        files.stream()
            .map(path ->
                path.getFileName().toString()
            )
            .toList()
            .toString();

    return new AgentResult(
        true,
        ResultType.SUCCESS,
        "Files listed successfully",
        data
    );
  }

  private AgentResult readFile(
      AgentCommand command
  ) throws IOException {

    String content =
        fileManager.readFile(
            command.source()
        );

    return new AgentResult(
        true,
        ResultType.SUCCESS,
        "File read successfully",
        content
    );
  }

  private AgentResult moveFile(
      AgentCommand command
  ) throws IOException {

    String source =
        command.source();

    String destination =
        command.destination();

    // Якщо директорії призначення ще немає —
    // створюємо її.
    if (!fileManager.directoryExists(destination)) {
      fileManager.createDirectory(destination);
    }

    String fileName =
        Path.of(source)
            .getFileName()
            .toString();

    String destinationFile =
        Path.of(destination, fileName)
            .toString();

    fileManager.moveFile(
        source,
        destinationFile
    );

    return new AgentResult(
        true,
        ResultType.SUCCESS,
        "File moved successfully",
        null
    );
  }

  private AgentResult moveMatching(
      AgentCommand command
  ) throws IOException {

    String pattern =
        command.source();

    String destination =
        command.destination();

    List<Path> matchingFiles =
        fileManager.findFiles(pattern);

    if (matchingFiles.isEmpty()) {

      return new AgentResult(
          false,
          ResultType.EXECUTION_ERROR,
          "No files matching pattern: " + pattern,
          null
      );
    }

    // Створюємо директорію призначення,
    // якщо її ще немає.
    if (!fileManager.directoryExists(destination)) {
      fileManager.createDirectory(destination);
    }

    for (Path file : matchingFiles) {

      String fileName =
          file.getFileName()
              .toString();

      String destinationFile =
          Path.of(
              destination,
              fileName
          ).toString();

      fileManager.moveFile(
          fileName,
          destinationFile
      );
    }

    return new AgentResult(
        true,
        ResultType.SUCCESS,
        "Matching files moved successfully",
        "Moved files: " + matchingFiles.size()
    );
  }

  private AgentResult createDirectory(
      AgentCommand command
  ) throws IOException {

    fileManager.createDirectory(
        command.source()
    );

    return new AgentResult(
        true,
        ResultType.SUCCESS,
        "Directory created successfully",
        null
    );
  }
}