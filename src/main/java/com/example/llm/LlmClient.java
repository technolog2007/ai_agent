package com.example.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LlmClient {

  private static final String OLLAMA_URL =
      "http://localhost:11434/api/generate";

  private static final String MODEL =
      "llama3.2:3b";

  private static final String SYSTEM_PROMPT = """
  You are an AI file management assistant. Your task is to interpret user requests and output structured JSON commands.

  AVAILABLE ACTIONS:
  - READ: Read file content. Requires "source". "destination" MUST be null.
  - LIST: List files in directory. "source" and "destination" MUST be null.
  - MOVE: Move a single file. Requires "source" (file) and "destination" (folder).
  - MOVE_MATCHING: Move multiple files by pattern. Requires "source" (e.g. *.jpg) and "destination" (folder).
  - CREATE_DIRECTORY: Create a folder. Requires "source" (folder name). "destination" MUST be null.

  CRITICAL RULE FOR "finished":
  - Set "finished": false WHENEVER you specify an action to execute (CREATE_DIRECTORY, MOVE, READ, etc.).
  - Set "finished": true ONLY WHEN "action" IS null AND no more actions are needed.

  RULES FOR MULTI-STEP REQUESTS:
  - For requests like "створи папку X та перемісти туди Y":
    STEP 1: Return action CREATE_DIRECTORY with "source": "X", "finished": false.
    STEP 2 (after receiving success result): Return action MOVE with "source": "Y", "destination": "X", "finished": false.

  EXAMPLES:

  User: створи папку Code та перемісти туди program.java
  Response:
  {
    "action": "CREATE_DIRECTORY",
    "source": "Code",
    "destination": null,
    "finished": false
  }
  """;  private final HttpClient httpClient;
  private final ObjectMapper objectMapper;

  public LlmClient() {
    this.httpClient = HttpClient.newHttpClient();
    this.objectMapper = new ObjectMapper();
  }

  public String ask(
      String userRequest,
      String previousResult
  ) throws IOException, InterruptedException {

    StringBuilder prompt = new StringBuilder();
    prompt.append(SYSTEM_PROMPT);
    prompt.append("\n\n====================\nCURRENT USER REQUEST\n====================\n");
    prompt.append(userRequest);

    if (previousResult != null) {
      prompt.append("\n\n====================\nPREVIOUS AGENT RESULT\n====================\n");
      prompt.append(previousResult);
      prompt.append("\n\n====================\nNEXT DECISION\n====================\n");
      prompt.append("""
              The previous action has already been executed.

          CRITICAL RULES FOR NEXT DECISION:
          1. If the previous action succeeded and was CREATE_DIRECTORY, now perform the SECOND step of user request (e.g., MOVE the file into the created directory).
          2. For MOVE, specify action: "MOVE", source: <file_name>, destination: <directory_name>.
          3. If all requested operations are finished, return IMMEDIATELY:
             {
               "action": null,
               "source": null,
               "destination": null,
               "finished": true
             }

          Return ONLY JSON.
          """);
    }

    Map<String, Object> requestBody = Map.of(
        "model", MODEL,
        "prompt", prompt.toString(),
        "stream", false,
        "format", createSchema()
    );

    log.debug("Full LLM Prompt:\n{}", prompt);
    log.info("-> Запит до LLM (Ollama)...");

    String json = objectMapper.writeValueAsString(requestBody);

    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(OLLAMA_URL))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(json))
        .build();

    HttpResponse<String> response = httpClient.send(
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

    JsonNode root = objectMapper.readTree(response.body());
    return root.get("response").asText();
  }

  private Map<String, Object> createSchema() {
    return Map.of(
        "type", "object",
        "properties", Map.of(
            "action", Map.of("type", new String[]{"string", "null"}),
            "source", Map.of("type", new String[]{"string", "null"}),
            "destination", Map.of("type", new String[]{"string", "null"}),
            "finished", Map.of("type", "boolean")
        ),
        "required", new String[]{"action", "source", "destination", "finished"},
        "additionalProperties", false
    );
  }
}