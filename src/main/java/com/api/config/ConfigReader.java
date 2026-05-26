package com.api.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * ══════════════════════════════════════════════════════════════
 *  ConfigReader.java
 *  Layer  : config
 *  Role   : Reads key=value pairs from config.properties
 * ══════════════════════════════════════════════════════════════
 *
 *  WHY DOES THIS CLASS EXIST?
 *  ─────────────────────────
 *  Imagine your base URL is https://sbioa-gateway-dev.coherent.in
 *  and you write it directly in 10 test files.
 *  One day the team moves to a staging URL.
 *  You now have to change 10 files. That is painful and error-prone.
 *
 *  With ConfigReader, the URL lives in ONE file (config.properties).
 *  You change it once. All 10 tests pick it up automatically.
 *
 *  HOW TO USE:
 *  ───────────
 *    String url = ConfigReader.get("base.url");
 *    // → "https://sbioa-gateway-dev.coherent.in"
 *
 *  KEY JAVA CONCEPTS USED HERE:
 *  ─────────────────────────────
 *    Properties   → built-in Java class for key=value files
 *    static {}    → runs automatically when the class is first used
 *    getClassLoader().getResourceAsStream() → reads files from resources folder
 */
public class ConfigReader {

    // Properties is Java's built-in class for handling .properties files
    private static final Properties properties = new Properties();

    /*
     * STATIC BLOCK
     * ─────────────
     * Code inside static { } runs ONCE automatically
     * the very first time Java loads this class.
     * We use it to load the file so it is ready before any test runs.
     */
    static {
        /*
         * try-with-resources: the InputStream is automatically closed
         * after the block ends — even if an error occurs.
         * InputStream = a stream of bytes (the file contents)
         */
        try (InputStream inputStream =
                     ConfigReader.class
                             .getClassLoader()
                             .getResourceAsStream("config.properties")) {

            // If Maven cannot find the file, tell the user clearly
            if (inputStream == null) {
                throw new RuntimeException(
                        "\n[ConfigReader] ERROR: config.properties not found!" +
                        "\nMake sure it exists at: src/test/resources/config.properties"
                );
            }

            // Parse the file — fills the properties object with key=value pairs
            properties.load(inputStream);
            System.out.println("[ConfigReader] config.properties loaded successfully.");

        } catch (IOException e) {
            throw new RuntimeException("[ConfigReader] Failed to read config.properties", e);
        }
    }

    /**
     * Returns the value for a given key from config.properties.
     *
     * @param key  the key to look up, e.g. "base.url"
     * @return     the value, e.g. "https://sbioa-gateway-dev.coherent.in"
     *
     * Example:
     *   ConfigReader.get("base.url")
     *   → "https://sbioa-gateway-dev.coherent.in"
     */
    public static String get(String key) {
        String value = properties.getProperty(key);

        // Fail loudly if the key does not exist — better than a silent null
        if (value == null || value.isBlank()) {
            throw new RuntimeException(
                    "\n[ConfigReader] Key '" + key + "' not found in config.properties." +
                    "\nAvailable keys: " + properties.stringPropertyNames()
            );
        }

        return value.trim();
    }
}
