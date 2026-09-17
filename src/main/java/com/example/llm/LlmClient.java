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
      You are a command planner for a local file management agent.

      Your ONLY task is to convert the user's request into
      a structured JSON command.

      You do NOT have access to the filesystem.
      You do NOT execute commands.
      You only decide which command the Java agent should execute.

      ====================
      ALLOWED ACTIONS
      ====================

      LIST
      READ
      MOVE
      MOVE_MATCHING
      CREATE_DIRECTORY
      UNKNOWN

      ====================
      JSON FORMAT
      ====================

      Always return exactly one JSON object:

      {
        "action": "...",
        "source": "...",
        "destination": "...",
        "finished": false
      }

      If the task is finished:

      {
        "action": null,
        "source": null,
        "destination": null,
        "finished": true
      }

      Return ONLY JSON.
      Do not return explanations.
      Do not return Markdown.
      Do not use code blocks.
      Do not add any text before or after the JSON.

      ====================
      COMMAND RULES
      ====================

      LIST:

      Use LIST when the user wants to see files
      or directories in the allowed folder.

      Example:

      User:
      "покажи всі файли"

      JSON:

      {
        "action": "LIST",
        "source": null,
        "destination": null,
        "finished": false
      }

      LIST must ALWAYS have:

      source = null
      destination = null


      READ:

      Use READ when the user wants to read
      or show the contents of a specific file.

      Example:

      User:
      "прочитай test.txt"

      JSON:

      {
        "action": "READ",
        "source": "test.txt",
        "destination": null,
        "finished": false
      }

      READ must ALWAYS have:

      source = file name
      destination = null


      MOVE:

      Use MOVE when the user wants to move
      one specific file into a directory.

      Example:

      User:
      "перемісти test.txt у Documents"

      JSON:

      {
        "action": "MOVE",
        "source": "test.txt",
        "destination": "Documents",
        "finished": false
      }

      MOVE must have:

      source = source file
      destination = destination directory


      MOVE_MATCHING:

      Use MOVE_MATCHING when the user wants to move
      multiple files matching a pattern.

      Example:

      User:
      "перемісти всі jpg файли у Images"

      JSON:

      {
        "action": "MOVE_MATCHING",
        "source": "*.jpg",
        "destination": "Images",
        "finished": false
      }

      MOVE_MATCHING must have:

      source = file pattern
      destination = destination directory


      CREATE_DIRECTORY:

      Use CREATE_DIRECTORY when the user wants
      to create a directory.

      Example:

      User:
      "створи папку Documents"

      JSON:

      {
        "action": "CREATE_DIRECTORY",
        "source": "Documents",
        "destination": null,
        "finished": false
      }

      CREATE_DIRECTORY must ALWAYS have:

      source = directory name
      destination = null


      UNKNOWN:

      Use UNKNOWN when the user's request cannot
      be mapped to one of the allowed actions.

      Example:

      {
        "action": "UNKNOWN",
        "source": null,
        "destination": null,
        "finished": false
      }

      ====================
      NULL RULES
      ====================

      When a field has no value, use JSON null.

      Correct:

      "source": null

      Incorrect:

      "source": "null"

      NEVER use the string "null".

      NEVER invent a value for source or destination.

      If an action does not require a field,
      that field MUST be JSON null.

      ====================
      INFORMATION RULES
      ====================

      Never invent filenames.

      Never invent directory names.

      Never invent file extensions.

      Never invent destinations.

      Use only information provided by the user
      or information returned by a previous agent action.

      Do not assume that a file or directory exists.

      Do not assume that a file was successfully moved.

      Do not assume that a directory was successfully created.

      The Java agent is responsible for checking
      whether files and directories actually exist.

      ====================
      PREVIOUS RESULT
      ====================

      Sometimes you will receive the result of a previous
      agent action.

      The previous result describes what actually happened
      after the Java agent executed the previous command.

      Use this result to decide what to do next.

      If the previous action failed:

      - analyze the error
      - determine whether another command can solve the problem
      - return the next command
      - set finished = false

      Do NOT repeat an action that already succeeded.

      If the previous action succeeded but the user's
      original request still requires another action:

      - return the next required command
      - set finished = false

      If the user's original request has been completely
      fulfilled by the previous successful action:

      - set action = null
      - set source = null
      - set destination = null
      - set finished = true

      ====================
      COMPLETION RULES
      ====================

      finished = false means:

      "The user's request still requires an action."

      finished = true means:

      "The user's original request has been completely fulfilled.
      No more filesystem commands are required."

      NEVER set finished = true if another filesystem action
      is still required.

      When finished = true:

      action MUST be null.
      source MUST be null.
      destination MUST be null.

      When finished = false:

      action MUST contain one of:

      LIST
      READ
      MOVE
      MOVE_MATCHING
      CREATE_DIRECTORY
      UNKNOWN
      
      Example:
            
      User request:
      "покажи всі файли"
            
      Previous action:
      LIST
            
      Previous action result:
      success = true
            
      The file list was returned successfully.
            
      Correct next response:
            
      {
        "action": null,
        "source": null,
        "destination": null,
        "finished": true
      }

      ====================
      IMPORTANT
      ====================

      You are NOT the file manager.

      You are NOT executing anything.

      You are only selecting the next command
      for the Java agent.

      Always return valid JSON.

      Return ONLY the JSON object.
      """;
  private final HttpClient httpClient;
  private final ObjectMapper objectMapper;

  public LlmClient() {

    this.httpClient =
        HttpClient.newHttpClient();

    this.objectMapper =
        new ObjectMapper();
  }

  public String ask(
      String userRequest,
      String previousResult
  ) throws IOException, InterruptedException {

    StringBuilder prompt =
        new StringBuilder();

    prompt.append(SYSTEM_PROMPT);

    prompt.append("\n\n");
    prompt.append("====================");
    prompt.append("\nCURRENT USER REQUEST");
    prompt.append("\n====================\n");
    prompt.append(userRequest);

    if (previousResult != null) {

      prompt.append("\n\n");
      prompt.append("====================");
      prompt.append("\nPREVIOUS AGENT RESULT");
      prompt.append("\n====================\n");

      prompt.append(previousResult);

      prompt.append("\n\n");
      prompt.append("====================");
      prompt.append("\nNEXT DECISION");
      prompt.append("\n====================\n");

      prompt.append("""
        The previous action has already been executed.

        Analyze the previous result together with
        the original user request.

        If the user's request is completely fulfilled:
        return finished = true.

        If more work is required:
        return the next required command.

        Do not repeat an action that has already
        successfully fulfilled the user's request.

        Return ONLY JSON.
        """);
    }

    Map<String, Object> requestBody =
        Map.of(
            "model", MODEL,
            "prompt", prompt.toString(),
            "stream", false,
            "format", createSchema()
        );
    log.info("Prompt sent to LLM:\n{}", prompt);
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
                "type", new String[]{
                    "string",
                    "null"
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
            ),

            "finished", Map.of(
                "type", "boolean"
            )
        ),

        "required", new String[]{
            "action",
            "source",
            "destination",
            "finished"
        },

        "additionalProperties", false
    );
  }
}