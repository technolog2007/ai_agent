package com.example;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentTest {

  @Test
  void shouldCreateDirectory() throws Exception {

    Path tempFolder =
        Files.createTempDirectory("agent-test");

    try {

      FileManager fileManager =
          new FileManager(tempFolder);

      CommandValidator validator =
          new CommandValidator();

      Agent agent =
          new Agent(
              fileManager,
              validator
          );

      AgentCommand command =
          new AgentCommand(
              Action.CREATE_DIRECTORY,
              "Documents",
              null
          );

      agent.execute(command);

      assertTrue(
          Files.isDirectory(
              tempFolder.resolve("Documents")
          )
      );

    } finally {

      deleteDirectory(tempFolder);
    }
  }

  private void deleteDirectory(Path directory)
      throws Exception {

    try (var stream = Files.walk(directory)) {

      stream
          .sorted(
              (a, b) ->
                  b.compareTo(a)
          )
          .forEach(path -> {

            try {
              Files.deleteIfExists(path);
            } catch (Exception e) {
              throw new RuntimeException(e);
            }

          });
    }
  }
  @Test
  void shouldMoveFileToDirectory()
      throws Exception {

    Path tempFolder =
        Files.createTempDirectory("agent-test");

    try {

      Path source =
          tempFolder.resolve("test.txt");

      Path documents =
          tempFolder.resolve("Documents");

      Files.writeString(
          source,
          "Hello Agent!"
      );

      Files.createDirectory(documents);

      FileManager fileManager =
          new FileManager(tempFolder);

      CommandValidator validator =
          new CommandValidator();

      Agent agent =
          new Agent(
              fileManager,
              validator
          );

      AgentCommand command =
          new AgentCommand(
              Action.MOVE,
              "test.txt",
              "Documents"
          );

      agent.execute(command);

      assertTrue(
          Files.exists(
              documents.resolve("test.txt")
          )
      );

      assertTrue(
          Files.notExists(source)
      );

    } finally {

      deleteDirectory(tempFolder);
    }
  }
  @Test
  void shouldMoveMatchingFiles()
      throws Exception {

    Path tempFolder =
        Files.createTempDirectory("agent-test");

    try {

      Path images =
          tempFolder.resolve("Images");

      Files.createDirectory(images);

      Files.writeString(
          tempFolder.resolve("photo1.jpg"),
          "image 1"
      );

      Files.writeString(
          tempFolder.resolve("photo2.jpg"),
          "image 2"
      );

      Files.writeString(
          tempFolder.resolve("document.pdf"),
          "document"
      );

      FileManager fileManager =
          new FileManager(tempFolder);

      CommandValidator validator =
          new CommandValidator();

      Agent agent =
          new Agent(
              fileManager,
              validator
          );

      AgentCommand command =
          new AgentCommand(
              Action.MOVE_MATCHING,
              "*.jpg",
              "Images"
          );

      agent.execute(command);

      assertTrue(
          Files.exists(
              images.resolve("photo1.jpg")
          )
      );

      assertTrue(
          Files.exists(
              images.resolve("photo2.jpg")
          )
      );

      assertTrue(
          Files.exists(
              tempFolder.resolve("document.pdf")
          )
      );

    } finally {

      deleteDirectory(tempFolder);
    }
  }

  @Test
  void shouldNotFailWhenSourceFileDoesNotExist()
      throws Exception {

    Path tempFolder =
        Files.createTempDirectory("agent-test");

    try {

      Path documents =
          tempFolder.resolve("Documents");

      Files.createDirectory(documents);

      FileManager fileManager =
          new FileManager(tempFolder);

      CommandValidator validator =
          new CommandValidator();

      Agent agent =
          new Agent(
              fileManager,
              validator
          );

      AgentCommand command =
          new AgentCommand(
              Action.MOVE,
              "missing.txt",
              "Documents"
          );

      agent.execute(command);

    } finally {

      deleteDirectory(tempFolder);
    }
  }
}