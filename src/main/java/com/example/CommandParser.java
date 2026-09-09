package com.example;

public class CommandParser {

  public AgentCommand parse(String input) {

    if (input == null || input.isBlank()) {
      throw new IllegalArgumentException(
          "Command cannot be empty"
      );
    }

    String normalized = input
        .trim()
        .toLowerCase();

    if (normalized.equals("list")
        || normalized.equals("покажи файли")
        || normalized.equals("показати файли")
        || normalized.equals("перелік файлів")) {

      return new AgentCommand(
          Action.LIST,
          null,
          null
      );
    }

    if (normalized.startsWith("read ")) {

      String fileName =
          input.trim().substring(5).trim();

      if (fileName.isBlank()) {
        throw new IllegalArgumentException(
            "READ command requires file"
        );
      }

      return new AgentCommand(
          Action.READ,
          fileName,
          null
      );
    }

    if (normalized.startsWith("прочитай ")) {

      String fileName =
          input.trim().substring(9).trim();

      if (fileName.isBlank()) {
        throw new IllegalArgumentException(
            "READ command requires file"
        );
      }

      return new AgentCommand(
          Action.READ,
          fileName,
          null
      );
    }

    if (normalized.startsWith("mkdir ")) {

      String directory =
          input.trim().substring(6).trim();

      if (directory.isBlank()) {
        throw new IllegalArgumentException(
            "CREATE_DIRECTORY requires directory"
        );
      }

      return new AgentCommand(
          Action.CREATE_DIRECTORY,
          directory,
          null
      );
    }

    if (normalized.startsWith("створи папку ")) {

      String directory =
          input.trim().substring(13).trim();

      if (directory.isBlank()) {
        throw new IllegalArgumentException(
            "CREATE_DIRECTORY requires directory"
        );
      }

      return new AgentCommand(
          Action.CREATE_DIRECTORY,
          directory,
          null
      );
    }

    if (normalized.startsWith("move ")) {

      String[] parts =
          input.trim().split("\\s+");

      if (parts.length != 3) {
        throw new IllegalArgumentException(
            "Usage: move <source> <destination>"
        );
      }

      return new AgentCommand(
          Action.MOVE,
          parts[1],
          parts[2]
      );
    }

    throw new IllegalArgumentException(
        "Unknown command: " + input
    );
  }
}