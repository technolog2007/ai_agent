package com.example.command;

public class CommandValidator {

  public void validate(AgentCommand command) {

    if (command == null) {
      throw new IllegalArgumentException(
          "Command cannot be null"
      );
    }

    if (command.action() == null) {
      throw new IllegalArgumentException(
          "Action cannot be null"
      );
    }

    switch (command.action()) {

      case LIST:
        validateList(command);
        break;

      case READ:
        validateRead(command);
        break;

      case MOVE:
        validateMove(command);
        break;

      case MOVE_MATCHING:
        validateMoveMatching(command);
        break;

      case CREATE_DIRECTORY:
        validateCreateDirectory(command);
        break;

      case UNKNOWN:
        throw new IllegalArgumentException(
            "Unknown or unsupported command"
        );
    }
  }

  private void validateList(AgentCommand command) {

    if (command.source() != null ||
        command.destination() != null) {

      throw new IllegalArgumentException(
          "LIST command must not have source or destination"
      );
    }
  }

  private void validateRead(AgentCommand command) {

    if (isBlank(command.source())) {
      throw new IllegalArgumentException(
          "READ command requires source"
      );
    }

    if (command.destination() != null) {
      throw new IllegalArgumentException(
          "READ command must not have destination"
      );
    }
  }

  private void validateMove(AgentCommand command) {

    if (isBlank(command.source())) {
      throw new IllegalArgumentException(
          "MOVE command requires source"
      );
    }

    if (isBlank(command.destination())) {
      throw new IllegalArgumentException(
          "MOVE command requires destination"
      );
    }
  }

  private void validateMoveMatching(
      AgentCommand command
  ) {

    if (isBlank(command.source())) {
      throw new IllegalArgumentException(
          "MOVE_MATCHING command requires pattern"
      );
    }

    if (isBlank(command.destination())) {
      throw new IllegalArgumentException(
          "MOVE_MATCHING command requires destination"
      );
    }
  }

  private void validateCreateDirectory(
      AgentCommand command
  ) {

    if (isBlank(command.source())) {
      throw new IllegalArgumentException(
          "CREATE_DIRECTORY command requires directory"
      );
    }

    if (command.destination() != null) {
      throw new IllegalArgumentException(
          "CREATE_DIRECTORY command must not have destination"
      );
    }
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}