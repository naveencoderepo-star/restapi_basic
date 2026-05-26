package com.api.tests;

import com.api.config.ConfigReader;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeClass;

import static io.restassured.RestAssured.given;

/**
 * ══════════════════════════════════════════════════════════════
 *  BaseTest.java
 *  Layer  : tests/base (shared test setup)
 *  Role   : Sets up RestAssured once — all test classes inherit this
 * ══════════════════════════════════════════════════════════════
 *
 *  WHY DOES THIS CLASS EXIST?
 *  ─────────────────────────
 *  Every API test needs the same starting setup:
 *    • Base URL (where to send requests)
 *    • Content-Type: application/json header
 *    • Logging (so we can see what is happening)
 *
 *  Without BaseTest, you would repeat those 3 things
 *  in EVERY single test method. That is duplication.
 *
 *  With BaseTest:
 *    public class LoginTest extends BaseTest { ... }
 *
 *  LoginTest now automatically inherits all the setup.
 *
 *  KEY TESTNG ANNOTATION:
 *  ──────────────────────
 *  @BeforeClass  → runs ONCE before the first test in the class
 *  @BeforeMethod → runs before EVERY single test method
 *
 *  We use @BeforeClass here because base URL only needs to be
 *  set once — not before every test.
 */
public class BaseTest {

    // The endpoint path stays here so all tests in this package can see it
    // "protected" means: visible to this class AND any class that extends it
    protected static final String LOGIN_ENDPOINT = "/sbioa-auth/v2/auth/password-login";
    protected static final String TEST_DATA_FILE = "testdata/login.json";

    /**
     * @BeforeClass — runs automatically before any @Test method runs.
     *
     * RestAssured.baseURI  →  the host part of every request URL
     *   e.g.  RestAssured.baseURI = "https://sbioa-gateway-dev.coherent.in"
     *   Then  .post("/sbioa-auth/v2/auth/password-login")
     *   Sends →  POST https://sbioa-gateway-dev.coherent.in/sbioa-auth/v2/auth/password-login
     */
    @BeforeClass
    public void setup() {
        // Read base URL from config.properties — NOT hardcoded here
        RestAssured.baseURI = ConfigReader.get("base.url");

        System.out.println("\n========================================");
        System.out.println("  Test Suite Starting");
        System.out.println("  Base URL : " + RestAssured.baseURI);
        System.out.println("========================================\n");
    }

    /**
     * Returns a pre-configured RestAssured request spec.
     *
     * Every test calls:   baseRequest().body(...).post(...)
     *
     * The 3 things we set up here:
     *   1. contentType(JSON) → adds header: Content-Type: application/json
     *   2. accept(JSON)      → adds header: Accept: application/json
     *   3. log().all()       → prints the full request to console (great for debugging)
     *
     * RestAssured pattern to memorise:
     *   given()         ← set up (headers, body, params)
     *     .when()       ← action (get, post, put, delete)
     *     .then()       ← verify (status code, body)
     */
    protected RequestSpecification baseRequest() {
        return given()
                .contentType(ContentType.JSON)   // Header: Content-Type: application/json
                .accept(ContentType.JSON)         // Header: Accept: application/json
                .log().all();                     // Print request details to console
    }
}
