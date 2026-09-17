package com.example.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.command.Action;
import com.example.command.AgentCommand;
import com.example.command.CommandValidator;
import com.example.filesystem.FileManager;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AgentTest {

  @TempDir
  Path tempDirectory;

  private FileManager fileManager;
  private CommandValidator validator;
  private Agent agent;

  @BeforeEach
  void setUp() {

    fileManager =
        new FileManager(tempDirectory);

    validator =
        new CommandValidator();

    agent =
        new Agent(
            fileManager,
            validator
        );
  }

  @Test
  void shouldListFiles() throws IOException {

    Files.createFile(
        tempDirectory.resolve("test.txt")
    );

    Files.createFile(
        tempDirectory.resolve("photo.jpg")
    );

    AgentCommand command =
        new AgentCommand(
            Action.LIST,
            null,
            null
        );

    AgentResult result =
        agent.execute(command);

    assertTrue(result.success());

    assertEquals(
        ResultType.SUCCESS,
        result.type()
    );

    assertEquals(
        "Files listed successfully",
        result.message()
    );

    assertTrue(
        result.data().contains("test.txt")
    );

    assertTrue(
        result.data().contains("photo.jpg")
    );
  }

  @Test
  void shouldReadFile() throws IOException {

    Files.writeString(
        tempDirectory.resolve("test.txt"),
        "Hello Agent"
    );

    AgentCommand command =
        new AgentCommand(
            Action.READ,
            "test.txt",
            null
        );

    AgentResult result =
        agent.execute(command);

    assertTrue(result.success());

    assertEquals(
        ResultType.SUCCESS,
        result.type()
    );

    assertEquals(
        "File read successfully",
        result.message()
    );

    assertEquals(
        "Hello Agent",
        result.data()
    );
  }

  @Test
  void shouldMoveFile() throws IOException {

    Files.createFile(
        tempDirectory.resolve("test.txt")
    );

    AgentCommand command =
        new AgentCommand(
            Action.MOVE,
            "test.txt",
            "Documents"
        );

    AgentResult result =
        agent.execute(command);

    assertTrue(result.success());

    assertEquals(
        ResultType.SUCCESS,
        result.type()
    );

    assertEquals(
        "File moved successfully",
        result.message()
    );

    assertTrue(
        Files.exists(
            tempDirectory
                .resolve("Documents")
                .resolve("test.txt")
        )
    );

    assertTrue(
        Files.notExists(
            tempDirectory.resolve("test.txt")
        )
    );
  }

  @Test
  void shouldMoveMatchingFiles() throws IOException {

    Files.createFile(
        tempDirectory.resolve("photo1.jpg")
    );

    Files.createFile(
        tempDirectory.resolve("photo2.jpg")
    );

    Files.createFile(
        tempDirectory.resolve("document.txt")
    );

    AgentCommand command =
        new AgentCommand(
            Action.MOVE_MATCHING,
            "*.jpg",
            "Images"
        );

    AgentResult result =
        agent.execute(command);

    assertTrue(result.success());

    assertEquals(
        ResultType.SUCCESS,
        result.type()
    );

    assertTrue(
        Files.exists(
            tempDirectory
                .resolve("Images")
                .resolve("photo1.jpg")
        )
    );

    assertTrue(
        Files.exists(
            tempDirectory
                .resolve("Images")
                .resolve("photo2.jpg")
        )
    );

    assertTrue(
        Files.exists(
            tempDirectory.resolve("document.txt")
        )
    );
  }

  @Test
  void shouldCreateDirectory() {

    AgentCommand command =
        new AgentCommand(
            Action.CREATE_DIRECTORY,
            "Test",
            null
        );

    AgentResult result =
        agent.execute(command);

    assertTrue(result.success());

    assertEquals(
        ResultType.SUCCESS,
        result.type()
    );

    assertEquals(
        "Directory created successfully",
        result.message()
    );

    assertTrue(
        Files.isDirectory(
            tempDirectory.resolve("Test")
        )
    );
  }

  @Test
  void shouldReturnValidationErrorForInvalidListCommand() {

    AgentCommand command =
        new AgentCommand(
            Action.LIST,
            null,
            "Documents"
        );

    AgentResult result =
        agent.execute(command);

    assertEquals(
        false,
        result.success()
    );

    assertEquals(
        ResultType.VALIDATION_ERROR,
        result.type()
    );

    assertEquals(
        "LIST command must not have source or destination",
        result.message()
    );
  }

  @Test
  void shouldReturnValidationErrorForInvalidCreateDirectoryCommand() {

    AgentCommand command =
        new AgentCommand(
            Action.CREATE_DIRECTORY,
            "Test",
            "Documents"
        );

    AgentResult result =
        agent.execute(command);

    assertEquals(
        false,
        result.success()
    );

    assertEquals(
        ResultType.VALIDATION_ERROR,
        result.type()
    );

    assertEquals(
        "CREATE_DIRECTORY command must not have destination",
        result.message()
    );
  }

  @Test
  void shouldReturnExecutionErrorWhenFileDoesNotExist() {

    AgentCommand command =
        new AgentCommand(
            Action.READ,
            "missing.txt",
            null
        );

    AgentResult result =
        agent.execute(command);

    assertEquals(
        false,
        result.success()
    );

    assertEquals(
        ResultType.EXECUTION_ERROR,
        result.type()
    );
  }

  @Test
  void shouldReturnValidationErrorForUnknownCommand() {

    AgentCommand command =
        new AgentCommand(
            Action.UNKNOWN,
            null,
            null
        );

    AgentResult result =
        agent.execute(command);

    assertEquals(
        false,
        result.success()
    );

    assertEquals(
        ResultType.VALIDATION_ERROR,
        result.type()
    );

    assertEquals(
        "Unknown or unsupported command",
        result.message()
    );
  }
}