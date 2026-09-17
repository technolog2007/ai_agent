package com.example.comand;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.agent.AgentDecision;
import com.example.command.Action;
import com.example.command.AgentCommand;
import com.example.llm.LlmCommandParser;
import org.junit.jupiter.api.Test;

class LlmCommandParserTest {

  private final LlmCommandParser parser =
      new LlmCommandParser();

  @Test
  void shouldParseListCommand() throws Exception {

    String json = """
        {
          "action": "LIST",
          "source": null,
          "destination": null,
          "finished": false
        }
        """;

    AgentDecision decision =
        parser.parse(json);

    AgentCommand command =
        decision.command();

    assertEquals(
        Action.LIST,
        command.action()
    );

    assertNull(command.source());
    assertNull(command.destination());

    assertFalse(
        decision.finished()
    );
  }

  @Test
  void shouldParseReadCommand() throws Exception {

    String json = """
        {
          "action": "READ",
          "source": "test.txt",
          "destination": null,
          "finished": false
        }
        """;

    AgentDecision decision =
        parser.parse(json);

    AgentCommand command =
        decision.command();

    assertEquals(
        Action.READ,
        command.action()
    );

    assertEquals(
        "test.txt",
        command.source()
    );

    assertNull(command.destination());

    assertFalse(
        decision.finished()
    );
  }

  @Test
  void shouldParseMoveCommand() throws Exception {

    String json = """
        {
          "action": "MOVE",
          "source": "test.txt",
          "destination": "Documents",
          "finished": false
        }
        """;

    AgentDecision decision =
        parser.parse(json);

    AgentCommand command =
        decision.command();

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

    assertFalse(
        decision.finished()
    );
  }

  @Test
  void shouldParseMoveMatchingCommand()
      throws Exception {

    String json = """
        {
          "action": "MOVE_MATCHING",
          "source": "*.jpg",
          "destination": "Images",
          "finished": false
        }
        """;

    AgentDecision decision =
        parser.parse(json);

    AgentCommand command =
        decision.command();

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

    assertFalse(
        decision.finished()
    );
  }

  @Test
  void shouldParseCreateDirectoryCommand()
      throws Exception {

    String json = """
        {
          "action": "CREATE_DIRECTORY",
          "source": "Documents",
          "destination": null,
          "finished": false
        }
        """;

    AgentDecision decision =
        parser.parse(json);

    AgentCommand command =
        decision.command();

    assertEquals(
        Action.CREATE_DIRECTORY,
        command.action()
    );

    assertEquals(
        "Documents",
        command.source()
    );

    assertNull(command.destination());

    assertFalse(
        decision.finished()
    );
  }

  @Test
  void shouldParseFinishedDecision() throws Exception {

    String json = """
      {
        "action": null,
        "source": null,
        "destination": null,
        "finished": true
      }
      """;

    AgentDecision decision =
        parser.parse(json);

    assertTrue(decision.finished());
    assertNull(decision.command());
  }

  @Test
  void shouldParseUnknownCommand()
      throws Exception {

    String json = """
        {
          "action": "UNKNOWN",
          "source": null,
          "destination": null,
          "finished": false
        }
        """;

    AgentDecision decision =
        parser.parse(json);

    assertEquals(
        Action.UNKNOWN,
        decision.command().action()
    );

    assertFalse(
        decision.finished()
    );
  }

  @Test
  void shouldRejectUnsupportedAction() {

    String json = """
        {
          "action": "DELETE",
          "source": "test.txt",
          "destination": null,
          "finished": false
        }
        """;

    assertThrows(
        IllegalArgumentException.class,
        () -> parser.parse(json)
    );
  }



  @Test
  void shouldConvertStringNullToNull() throws Exception {

    String json = """
      {
        "action": "LIST",
        "source": "null",
        "destination": "null",
        "finished": false
      }
      """;

    AgentDecision decision =
        parser.parse(json);

    assertFalse(decision.finished());

    assertNotNull(decision.command());

    assertNull(
        decision.command().source()
    );

    assertNull(
        decision.command().destination()
    );
  }
}