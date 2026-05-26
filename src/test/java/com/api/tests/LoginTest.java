package com.api.tests;

import com.api.utils.JsonReader;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * ══════════════════════════════════════════════════════════════
 *  LoginTest.java
 *  Layer  : tests
 *  Role   : Tests for POST /sbioa-auth/v2/auth/password-login
 * ══════════════════════════════════════════════════════════════
 *
 *  WHAT THIS FILE COVERS:
 *  ──────────────────────
 *  Test 1  → Valid login         → expects 200 (success)
 *  Test 2  → Wrong password      → expects 4xx (error)
 *  Test 3  → Empty mobile        → expects 4xx (validation error)
 *  Test 4  → Response time check → expects < 5 seconds
 *  Test 5  → Read a field from response body (template for you to customise)
 *
 *  HOW EVERY TEST IS STRUCTURED (read this carefully):
 *  ───────────────────────────────────────────────────
 *  STEP 1 → Load test data from login.json   (not hardcoded)
 *  STEP 2 → Send the HTTP request            (RestAssured)
 *  STEP 3 → Print the response               (so you can see it)
 *  STEP 4 → Assert what you expected         (TestNG Assert)
 *
 *  RESTASSURED PATTERN (memorise this):
 *  ─────────────────────────────────────
 *    Response response =
 *      given()           ← set up headers, body, params
 *        .body(...)
 *      .when()           ← perform the action
 *        .post("/endpoint")
 *    ;
 *
 *    // Then check things on the response object
 *    response.statusCode()
 *    response.getBody().asString()
 *    response.jsonPath().getString("someField")
 *    response.time()
 */
public class LoginTest extends BaseTest {

    // ──────────────────────────────────────────────────────────────────
    //  TEST 1 — Happy Path: Valid credentials should log in successfully
    // ──────────────────────────────────────────────────────────────────
    @Test(description = "Valid credentials → 200 OK")
    public void test01_validLogin_shouldReturn200() {

        // ── STEP 1: Load test data ─────────────────────────────────────
        // JsonReader.getAsMap(file, section)
        //   → opens testdata/login.json
        //   → finds the "valid_user" block
        //   → returns it as Map<String,Object> = { "mobile":"9840127991", ... }
        Map<String, Object> requestBody = JsonReader.getAsMap(TEST_DATA_FILE, "valid_user");

        System.out.println("\n▶ TEST 1: Valid Login");
        System.out.println("  Request data loaded: " + requestBody);

        // ── STEP 2: Send the POST request ─────────────────────────────
        // baseRequest()        → adds Content-Type:application/json header
        // .body(requestBody)   → attaches the JSON payload
        // .when().post(...)    → sends the POST request to the endpoint
        // The full URL becomes:
        //   https://sbioa-gateway-dev.coherent.in  +  /sbioa-auth/v2/auth/password-login
        Response response = baseRequest()
                .body(requestBody)          // attach the JSON body
                .when()
                .post(LOGIN_ENDPOINT);      // send the POST

        // ── STEP 3: Print the response ────────────────────────────────
        System.out.println("\n◀ RESPONSE:");
        System.out.println("  Status Code  : " + response.statusCode());
        System.out.println("  Response Time: " + response.time() + " ms");
        System.out.println("  Response Body:");
        response.prettyPrint();             // nicely formatted JSON in console

        // ── STEP 4: Assert (check) what we expect ────────────────────
        // Assert.assertEquals(actual, expected, "message if test fails")
        // For a successful login the API should return HTTP 200
        Assert.assertEquals(
                response.statusCode(),      // actual value from API
                200,                        // expected value
                "Valid login should return 200 OK"
        );

        // Also check that the response body is not completely empty
        Assert.assertFalse(
                response.getBody().asString().isEmpty(),
                "Response body should not be empty on success"
        );

        System.out.println("\n✅ TEST 1 PASSED\n");
    }


    // ──────────────────────────────────────────────────────────────────
    //  TEST 2 — Wrong Password: API should reject bad credentials
    // ──────────────────────────────────────────────────────────────────
    @Test(description = "Wrong password → error response")
    public void test02_wrongPassword_shouldReturnError() {

        // ── STEP 1: Load test data ─────────────────────────────────────
        // Using the "wrong_password" block from login.json
        // (same mobile, but password = "WRONG999")
        Map<String, Object> requestBody = JsonReader.getAsMap(TEST_DATA_FILE, "wrong_password");

        System.out.println("\n▶ TEST 2: Wrong Password");

        // ── STEP 2: Send the POST request ─────────────────────────────
        Response response = baseRequest()
                .body(requestBody)
                .when()
                .post(LOGIN_ENDPOINT);

        // ── STEP 3: Print the response ────────────────────────────────
        System.out.println("\n◀ RESPONSE:");
        System.out.println("  Status Code : " + response.statusCode());
        System.out.println("  Response Body:");
        response.prettyPrint();

        // ── STEP 4: Assert ────────────────────────────────────────────
        // Wrong password must NOT return 200 (that would be a security bug!)
        // We use assertNotEquals to say "it must be anything except 200"
        Assert.assertNotEquals(
                response.statusCode(),
                200,
                "Wrong password should NOT return 200 OK"
        );

        // Once you know the exact error code your API returns,
        // replace the above with a specific check like:
        //   Assert.assertEquals(response.statusCode(), 401, "...");
        //   or
        //   Assert.assertEquals(response.statusCode(), 400, "...");

        System.out.println("\n✅ TEST 2 PASSED — API correctly rejected wrong password\n");
    }


    // ──────────────────────────────────────────────────────────────────
    //  TEST 3 — Empty Mobile: API should return a validation error
    // ──────────────────────────────────────────────────────────────────
    @Test(description = "Empty mobile → 4xx validation error")
    public void test03_emptyMobile_shouldReturn4xxError() {

        // ── STEP 1: Load test data ─────────────────────────────────────
        // "empty_mobile" block has mobile: ""
        Map<String, Object> requestBody = JsonReader.getAsMap(TEST_DATA_FILE, "empty_mobile");

        System.out.println("\n▶ TEST 3: Empty Mobile Number");

        // ── STEP 2: Send the request ──────────────────────────────────
        Response response = baseRequest()
                .body(requestBody)
                .when()
                .post(LOGIN_ENDPOINT);

        // ── STEP 3: Print ─────────────────────────────────────────────
        System.out.println("\n◀ RESPONSE:");
        System.out.println("  Status Code : " + response.statusCode());
        response.prettyPrint();

        // ── STEP 4: Assert ────────────────────────────────────────────
        // 4xx means client error (400=bad request, 401=unauthorised, etc.)
        // We check: is the status code between 400 and 499?
        int status = response.statusCode();
        boolean isClientError = (status >= 400 && status <= 499);

        Assert.assertTrue(
                isClientError,
                "Empty mobile should return 4xx. Actual status: " + status
        );

        System.out.println("\n✅ TEST 3 PASSED — API correctly rejected empty mobile\n");
    }


    // ──────────────────────────────────────────────────────────────────
    //  TEST 4 — Response Time: API should reply within 5 seconds
    // ──────────────────────────────────────────────────────────────────
    @Test(description = "Response time should be under 5000ms")
    public void test04_responseTime_shouldBeLessThan5Seconds() {

        Map<String, Object> requestBody = JsonReader.getAsMap(TEST_DATA_FILE, "valid_user");

        System.out.println("\n▶ TEST 4: Response Time Check");

        Response response = baseRequest()
                .body(requestBody)
                .when()
                .post(LOGIN_ENDPOINT);

        // response.time() returns how many milliseconds the call took
        long responseTimeMs = response.time();
        System.out.println("  Response Time: " + responseTimeMs + " ms");

        // 5000 ms = 5 seconds. Adjust this to your team's SLA (Service Level Agreement)
        Assert.assertTrue(
                responseTimeMs < 5000,
                "API too slow! Expected < 5000ms, got: " + responseTimeMs + "ms"
        );

        System.out.println("\n✅ TEST 4 PASSED — Response time is acceptable\n");
    }


    // ──────────────────────────────────────────────────────────────────
    //  TEST 5 — Read a field from the response body
    //  This test is a LEARNING TEMPLATE.
    //  Run it first → look at the printed response body
    //  Then update the jsonPath below to match your actual API response
    // ──────────────────────────────────────────────────────────────────
    @Test(description = "Read a specific field from the response JSON")
    public void test05_readFieldFromResponse_learningTemplate() {

        Map<String, Object> requestBody = JsonReader.getAsMap(TEST_DATA_FILE, "valid_user");

        System.out.println("\n▶ TEST 5: Reading Response Fields (Learning Template)");

        Response response = baseRequest()
                .body(requestBody)
                .when()
                .post(LOGIN_ENDPOINT);

        // Always print the full response first so you can see the structure
        System.out.println("\n◀ FULL RESPONSE BODY (use this to find your field paths):");
        response.prettyPrint();

        // ── HOW TO READ FIELDS FROM A JSON RESPONSE ──────────────────
        //
        // response.jsonPath() gives you a navigator for the response JSON.
        //
        // Example — if your API returns:
        //   {
        //     "status": "success",
        //     "data": {
        //       "token": "eyJhbGciOi...",
        //       "userId": "USR001"
        //     }
        //   }
        //
        // Read top-level field:
        //   String status = response.jsonPath().getString("status");
        //
        // Read nested field:
        //   String token  = response.jsonPath().getString("data.token");
        //   String userId = response.jsonPath().getString("data.userId");
        //
        // Read a number:
        //   int code = response.jsonPath().getInt("code");
        //
        // ─────────────────────────────────────────────────────────────
        // EXERCISE FOR YOU:
        //   1. Run this test and look at the printed response body above
        //   2. Find a field you want to check (e.g., "status" or "data.token")
        //   3. Uncomment and update ONE of the examples below:
        //
        // String status = response.jsonPath().getString("status");
        // System.out.println("  status field = " + status);
        // Assert.assertEquals(status, "success", "Status should be success");
        //
        // String token = response.jsonPath().getString("data.token");
        // System.out.println("  token = " + token);
        // Assert.assertNotNull(token, "Token should not be null");
        // ─────────────────────────────────────────────────────────────

        // For now, just confirm we got a 200 back
        Assert.assertEquals(response.statusCode(), 200,
                "Test 5 needs a 200 response to explore the response body");

        System.out.println("\n📚 NEXT STEP:");
        System.out.println("   Look at the response body printed above.");
        System.out.println("   Find a field, then assert it using response.jsonPath().getString(\"fieldName\")");
        System.out.println("\n✅ TEST 5 PASSED — Explore the response body printed above\n");
    }
}
