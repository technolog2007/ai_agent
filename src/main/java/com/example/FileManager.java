package com.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileManager {

  private final Path allowedFolder;

  public FileManager(Path allowedFolder) {

    this.allowedFolder = allowedFolder
        .toAbsolutePath()
        .normalize();

    if (!Files.isDirectory(this.allowedFolder)) {
      throw new IllegalArgumentException(
          "Allowed folder does not exist: " + this.allowedFolder
      );
    }
  }

  public void showFiles() {

    log.info("Allowed folder:");
    log.info(allowedFolder.toString());

    try {
      List<Path> files = Files.list(allowedFolder)
          .toList();

      log.info("Files:");

      for (Path file : files) {
        log.info(file.getFileName().toString());
      }

    } catch (IOException e) {
      log.error("Cannot read folder:", e);
    }
  }

  public boolean fileExists(String fileName) {

    Path safePath = getSafePath(fileName);

    return Files.exists(safePath);
  }

  private Path getSafePath(String fileName) {

    Path requestedPath = allowedFolder
        .resolve(fileName)
        .normalize();

    if (!requestedPath.startsWith(allowedFolder)) {
      throw new SecurityException(
          "Access denied: " + fileName
      );
    }
    return requestedPath;
  }
  public String readFile(String fileName) throws IOException {

    Path safePath = getSafePath(fileName);

    return Files.readString(safePath);
  }

  public List<Path> listFiles() throws IOException {

    try (Stream<Path> stream = Files.list(allowedFolder)) {
      return stream.toList();
    }
  }

  public void moveFile(String source, String destination) throws IOException {

    Path sourcePath = getSafePath(source);
    Path destinationPath = getSafePath(destination);

    Files.move(sourcePath, destinationPath);
  }
  public void createDirectory(String directoryName) throws IOException {

    Path directoryPath = getSafePath(directoryName);

    Files.createDirectories(directoryPath);
  }
}