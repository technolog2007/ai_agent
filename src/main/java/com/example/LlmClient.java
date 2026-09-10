package com.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class LlmClient {

  private static final String OLLAMA_URL =
      "http://localhost:11434/api/generate";

  private static final String MODEL =
      "llama3.2:3b";

  private static final String SYSTEM_PROMPT = """
      You are a command parser for a local file management agent.

      Your ONLY task is to convert the user's request
      into a JSON command.

      Allowed actions:

      LIST
      READ
      MOVE
      MOVE_MATCHING
      CREATE_DIRECTORY

      The response MUST have exactly this structure:

      {
        "action": "ACTION",
        "source": "SOURCE_OR_NULL",
        "destination": "DESTINATION_OR_NULL"
      }

      Rules:

      1. LIST:
         source = null
         destination = null

      2. READ:
         source = file name
         destination = null

      3. MOVE:
         source = source file
         destination = destination directory

      4. MOVE_MATCHING:
         source = file pattern such as "*.jpg"
         destination = directory

      5. CREATE_DIRECTORY:
         source = directory name
         destination = null

        If the user's request cannot be mapped
        to one of the allowed actions,
        return:
           
        {
          "action": "UNKNOWN",
          "source": null,
          "destination": null
        }
           
        IMPORTANT:
        Never convert an unsupported operation
        into another allowed operation.
           
        For example:
        - "delete test.txt" -> UNKNOWN
        - "remove test.txt" -> UNKNOWN
        - "send test.txt by email" -> UNKNOWN
        - "open browser" -> UNKNOWN
           
        Do not interpret DELETE, REMOVE or ERASE
        as READ.

      NEVER return explanations.
      NEVER return markdown.
      NEVER use ```json.
      Return ONLY valid JSON.
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

    String json = """
        {
          "model": "%s",
          "prompt": "%s",
          "stream": false
        }
        """.formatted(
        MODEL,
        escapeJson(prompt)
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

  private String escapeJson(String text) {

    return text
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r");
  }
}