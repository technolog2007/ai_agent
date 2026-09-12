package com.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LlmCommandParserTest {

  private final LlmCommandParser parser =
      new LlmCommandParser();

  @Test
  void shouldParseListCommand() throws Exception {

    String json = """
        {
          "action": "LIST",
          "source": null,
          "destination": null
        }
        """;

    AgentCommand command =
        parser.parse(json);

    assertEquals(
        Action.LIST,
        command.action()
    );

    assertNull(command.source());
    assertNull(command.destination());
  }

  @Test
  void shouldParseReadCommand() throws Exception {

    String json = """
        {
          "action": "READ",
          "source": "test.txt",
          "destination": null
        }
        """;

    AgentCommand command =
        parser.parse(json);

    assertEquals(
        Action.READ,
        command.action()
    );

    assertEquals(
        "test.txt",
        command.source()
    );

    assertNull(command.destination());
  }

  @Test
  void shouldParseMoveCommand() throws Exception {

    String json = """
        {
          "action": "MOVE",
          "source": "test.txt",
          "destination": "Documents"
        }
        """;

    AgentCommand command =
        parser.parse(json);

    assertEquals(
        Action.MOVE,
        command.action()
    );

    assertEquals(
        "test.txt",
        command.source()
    );

    assertEquals(
        "Documents",
        command.destination()
    );
  }

  @Test
  void shouldParseMoveMatchingCommand()
      throws Exception {

    String json = """
        {
          "action": "MOVE_MATCHING",
          "source": "*.jpg",
          "destination": "Images"
        }
        """;

    AgentCommand command =
        parser.parse(json);

    assertEquals(
        Action.MOVE_MATCHING,
        command.action()
    );

    assertEquals(
        "*.jpg",
        command.source()
    );

    assertEquals(
        "Images",
        command.destination()
    );
  }

  @Test
  void shouldParseCreateDirectoryCommand()
      throws Exception {

    String json = """
        {
          "action": "CREATE_DIRECTORY",
          "source": "Documents",
          "destination": null
        }
        """;

    AgentCommand command =
        parser.parse(json);

    assertEquals(
        Action.CREATE_DIRECTORY,
        command.action()
    );

    assertEquals(
        "Documents",
        command.source()
    );

    assertNull(command.destination());
  }

  @Test
  void shouldParseUnknownCommand()
      throws Exception {

    String json = """
        {
          "action": "UNKNOWN",
          "source": null,
          "destination": null
        }
        """;

    AgentCommand command =
        parser.parse(json);

    assertEquals(
        Action.UNKNOWN,
        command.action()
    );
  }

  @Test
  void shouldRejectUnsupportedAction()
  {
    String json = """
        {
          "action": "DELETE",
          "source": "test.txt",
          "destination": null
        }
        """;

    assertThrows(
        IllegalArgumentException.class,
        () -> parser.parse(json)
    );
  }
}