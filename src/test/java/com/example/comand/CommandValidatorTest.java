package com.example.comand;

import com.example.command.Action;
import com.example.command.AgentCommand;
import com.example.command.CommandValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CommandValidatorTest {

  private final CommandValidator validator =
      new CommandValidator();

  @Test
  void shouldAcceptListCommand() {

    AgentCommand command =
        new AgentCommand(
            Action.LIST,
            null,
            null
        );

    assertDoesNotThrow(
        () -> validator.validate(command)
    );
  }

  @Test
  void shouldAcceptReadCommand() {

    AgentCommand command =
        new AgentCommand(
            Action.READ,
            "test.txt",
            null
        );

    assertDoesNotThrow(
        () -> validator.validate(command)
    );
  }

  @Test
  void shouldAcceptMoveCommand() {

    AgentCommand command =
        new AgentCommand(
            Action.MOVE,
            "test.txt",
            "Documents"
        );

    assertDoesNotThrow(
        () -> validator.validate(command)
    );
  }

  @Test
  void shouldAcceptCreateDirectoryCommand() {

    AgentCommand command =
        new AgentCommand(
            Action.CREATE_DIRECTORY,
            "Documents",
            null
        );

    assertDoesNotThrow(
        () -> validator.validate(command)
    );
  }

  @Test
  void shouldRejectNullCommand() {

    assertThrows(
        IllegalArgumentException.class,
        () -> validator.validate(null)
    );
  }

  @Test
  void shouldRejectReadWithoutSource() {

    AgentCommand command =
        new AgentCommand(
            Action.READ,
            null,
            null
        );

    assertThrows(
        IllegalArgumentException.class,
        () -> validator.validate(command)
    );
  }

  @Test
  void shouldRejectMoveWithoutDestination() {

    AgentCommand command =
        new AgentCommand(
            Action.MOVE,
            "test.txt",
            null
        );

    assertThrows(
        IllegalArgumentException.class,
        () -> validator.validate(command)
    );
  }

  @Test
  void shouldRejectListWithSource() {

    AgentCommand command =
        new AgentCommand(
            Action.LIST,
            "test.txt",
            null
        );

    assertThrows(
        IllegalArgumentException.class,
        () -> validator.validate(command)
    );
  }

  @Test
  void shouldAcceptMoveMatchingCommand() {

    AgentCommand command =
        new AgentCommand(
            Action.MOVE_MATCHING,
            "*.jpg",
            "Images"
        );

    assertDoesNotThrow(
        () -> validator.validate(command)
    );
  }

  @Test
  void shouldRejectMoveMatchingWithoutPattern() {

    AgentCommand command =
        new AgentCommand(
            Action.MOVE_MATCHING,
            null,
            "Images"
        );

    assertThrows(
        IllegalArgumentException.class,
        () -> validator.validate(command)
    );
  }

  @Test
  void shouldRejectMoveMatchingWithoutDestination() {

    AgentCommand command =
        new AgentCommand(
            Action.MOVE_MATCHING,
            "*.jpg",
            null
        );

    assertThrows(
        IllegalArgumentException.class,
        () -> validator.validate(command)
    );
  }
  @Test
  void shouldRejectUnknownCommand() {

    AgentCommand command =
        new AgentCommand(
            Action.UNKNOWN,
            null,
            null
        );

    assertThrows(
        IllegalArgumentException.class,
        () -> validator.validate(command)
    );
  }
}