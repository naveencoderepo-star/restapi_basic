package com.api.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * ══════════════════════════════════════════════════════════════
 *  JsonReader.java
 *  Layer  : utils (utility / helper)
 *  Role   : Reads test data from JSON files in resources/testdata/
 * ══════════════════════════════════════════════════════════════
 *
 *  WHY DOES THIS CLASS EXIST?
 *  ─────────────────────────
 *  Bad approach (AVOID this):
 *    Map<String,Object> body = new HashMap<>();
 *    body.put("mobile", "9840127991");       ← hardcoded in test
 *    body.put("password", "12345678");       ← hardcoded in test
 *
 *  Better approach (what we do here):
 *    1. Store the data in testdata/login.json
 *    2. Use JsonReader to load it → your test stays clean
 *
 *  BENEFITS:
 *    • Change test data without touching test code
 *    • Non-technical teammates can update the JSON file
 *    • Easy to add new scenarios (just add a new JSON block)
 *
 *  HOW TO USE:
 *  ───────────
 *    // Load the whole file
 *    JsonNode root = JsonReader.read("testdata/login.json");
 *
 *    // Load ONE block from the file
 *    Map<String,Object> body = JsonReader.getAsMap("testdata/login.json", "valid_user");
 *
 *    // Use directly in RestAssured
 *    given().body(body).post("/login");
 *
 *  KEY JAVA CONCEPT — ObjectMapper:
 *  ─────────────────────────────────
 *    ObjectMapper is Jackson's main class.
 *    It converts JSON text  →  Java objects  (readTree, readValue)
 *    It converts Java objects  →  JSON text  (writeValueAsString)
 */
public class JsonReader {

    // One shared ObjectMapper is enough — it is thread-safe for reading
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Reads an entire JSON file and returns it as a JsonNode (a tree).
     * Use this when you want to navigate the JSON manually.
     *
     * @param filePath  path relative to src/test/resources/
     *                  e.g.  "testdata/login.json"
     * @return JsonNode representing the root of the JSON file
     */
    public static JsonNode read(String filePath) {
        try (InputStream inputStream =
                     JsonReader.class
                             .getClassLoader()
                             .getResourceAsStream(filePath)) {

            if (inputStream == null) {
                throw new RuntimeException(
                        "\n[JsonReader] File not found: " + filePath +
                        "\nMake sure it exists under src/test/resources/"
                );
            }

            // readTree() parses the JSON and returns a navigable tree
            return objectMapper.readTree(inputStream);

        } catch (IOException e) {
            throw new RuntimeException("[JsonReader] Failed to read: " + filePath, e);
        }
    }

    /**
     * Reads a SPECIFIC BLOCK from a JSON file and converts it to
     * a Map<String, Object> that RestAssured can use directly as the request body.
     *
     * Example — login.json:
     * {
     *   "valid_user": {
     *     "mobile": "9840127991",
     *     "password": "12345678"
     *   }
     * }
     *
     * JsonReader.getAsMap("testdata/login.json", "valid_user")
     * → { "mobile" → "9840127991", "password" → "12345678" }
     *
     * @param filePath    path to the JSON file (relative to resources/)
     * @param sectionKey  the top-level key in the JSON (e.g. "valid_user")
     * @return Map ready to be used as a RestAssured request body
     */
    public static Map<String, Object> getAsMap(String filePath, String sectionKey) {

        // First read the whole file
        JsonNode root = read(filePath);

        // Navigate to the specific block (e.g., "valid_user")
        JsonNode section = root.get(sectionKey);

        if (section == null) {
            throw new RuntimeException(
                    "\n[JsonReader] Section '" + sectionKey + "' not found in " + filePath +
                    "\nAvailable sections: " + root.fieldNames()
            );
        }

        // TypeReference tells Jackson: convert this JsonNode to Map<String, Object>
        // This is the format RestAssured's .body() method accepts
        return objectMapper.convertValue(section, new TypeReference<Map<String, Object>>() {});
    }
}
