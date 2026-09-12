package com.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class LlmClient {

  private static final String OLLAMA_URL =
      "http://localhost:11434/api/generate";

  private static final String MODEL =
      "llama3.2:3b";

  private static final String SYSTEM_PROMPT = """
      You are a command parser for a local file management agent.

      Your ONLY task is to convert the user's request
      into a command.

      Allowed actions:

      LIST
      READ
      MOVE
      MOVE_MATCHING
      CREATE_DIRECTORY
      UNKNOWN

      Rules:

      LIST:
      source = null
      destination = null

      READ:
      source = file name
      destination = null

      MOVE:
      source = source file
      destination = destination directory

      MOVE_MATCHING:
      source = file pattern such as "*.jpg"
      destination = directory

      CREATE_DIRECTORY:
      source = directory name
      destination = null

      If the request cannot be mapped to one
      of the allowed actions, use UNKNOWN.

      Examples:

      "покажи всі файли"
      -> LIST

      "прочитай test.txt"
      -> READ, source = "test.txt"

      "перемісти test.txt у Documents"
      -> MOVE, source = "test.txt", destination = "Documents"

      "перемісти всі jpg у Images"
      -> MOVE_MATCHING, source = "*.jpg", destination = "Images"

      "створи папку Documents"
      -> CREATE_DIRECTORY, source = "Documents"

      "видали test.txt"
      -> UNKNOWN

      "відправ test.txt електронною поштою"
      -> UNKNOWN
      """;

  private final HttpClient httpClient;
  private final ObjectMapper objectMapper;

  public LlmClient() {

    this.httpClient =
        HttpClient.newHttpClient();

    this.objectMapper =
        new ObjectMapper();
  }

  public String ask(String userRequest)
      throws IOException, InterruptedException {

    String prompt =
        SYSTEM_PROMPT
            + "\nUser request:\n"
            + userRequest;

    Map<String, Object> requestBody =
        Map.of(
            "model", MODEL,
            "prompt", prompt,
            "stream", false,
            "format", createSchema()
        );

    String json =
        objectMapper.writeValueAsString(
            requestBody
        );

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(OLLAMA_URL))
            .header(
                "Content-Type",
                "application/json"
            )
            .POST(
                HttpRequest.BodyPublishers
                    .ofString(json)
            )
            .build();

    HttpResponse<String> response =
        httpClient.send(
            request,
            HttpResponse.BodyHandlers.ofString()
        );

    if (response.statusCode() != 200) {

      throw new IOException(
          "Ollama returned HTTP "
              + response.statusCode()
              + ": "
              + response.body()
      );
    }

    JsonNode root =
        objectMapper.readTree(
            response.body()
        );

    return root
        .get("response")
        .asText();
  }

  private Map<String, Object> createSchema() {

    return Map.of(
        "type", "object",

        "properties", Map.of(

            "action", Map.of(
                "type", "string",
                "enum", new String[]{
                    "LIST",
                    "READ",
                    "MOVE",
                    "MOVE_MATCHING",
                    "CREATE_DIRECTORY",
                    "UNKNOWN"
                }
            ),

            "source", Map.of(
                "type", new String[]{
                    "string",
                    "null"
                }
            ),

            "destination", Map.of(
                "type", new String[]{
                    "string",
                    "null"
                }
            )
        ),

        "required", new String[]{
            "action",
            "source",
            "destination"
        },

        "additionalProperties", false
    );
  }
}