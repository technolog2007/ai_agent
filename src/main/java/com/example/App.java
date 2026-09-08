package com.example;

import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class App {

  public static void main(String[] args) {

    log.info("Local File Agent");
    log.info("Agent started.");

    Path allowedFolder = Path.of("C:\\Temp\\AgentTest");

    FileManager fileManager = new FileManager(allowedFolder);

    fileManager.showFiles();

    log.info("document.txt exists: {}",
        fileManager.fileExists("document.txt"));

    log.info("unknown.txt exists: {}",
        fileManager.fileExists("unknown.txt"));
  }
}
