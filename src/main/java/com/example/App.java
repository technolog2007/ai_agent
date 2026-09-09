package com.example;

import java.nio.file.Path;
import java.util.Scanner;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class App {

  public static void main(String[] args) {

    log.info("Local File Agent");
    log.info("Agent started.");

    Path allowedFolder =
        Path.of("C:\\Temp\\AgentTest");

    FileManager fileManager =
        new FileManager(allowedFolder);

    CommandValidator validator =
        new CommandValidator();

    Agent agent =
        new Agent(
            fileManager,
            validator
        );

    CommandParser parser =
        new CommandParser();

    Scanner scanner =
        new Scanner(System.in);

    System.out.println();
    System.out.println("Local File Agent");
    System.out.println("Allowed folder: " + allowedFolder);
    System.out.println();
    System.out.println("Available commands:");
    System.out.println("  list");
    System.out.println("  read <file>");
    System.out.println("  mkdir <directory>");
    System.out.println("  move <source> <destination>");
    System.out.println("  exit");
    System.out.println();

    while (true) {

      System.out.print("> ");

      String input = scanner.nextLine();

      if (input.equalsIgnoreCase("exit")) {
        break;
      }

      try {

        AgentCommand command =
            parser.parse(input);

        agent.execute(command);

      } catch (IllegalArgumentException e) {

        System.out.println(
            "Command error: " + e.getMessage()
        );
      }
    }

    scanner.close();

    log.info("Agent stopped.");
  }
}