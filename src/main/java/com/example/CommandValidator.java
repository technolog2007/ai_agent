package com.example;

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

      case CREATE_DIRECTORY:
        validateCreateDirectory(command);
        break;
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

  private void validateCreateDirectory(AgentCommand command) {

    if (isBlank(command.source())) {
      throw new IllegalArgumentException(
          "CREATE_DIRECTORY command requires source"
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