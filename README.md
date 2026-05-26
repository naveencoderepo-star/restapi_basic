# Basic RestAssured Framework — Step by Step

> **Purpose:** Learn API automation from scratch. Simple, readable, no magic.  
> **API under test:** `POST https://sbioa-gateway-dev.coherent.in/sbioa-auth/v2/auth/password-login`

---

## Project Structure

```
basic-restassured/
│
├── pom.xml                                      ← Maven: lists all libraries we need
│
├── src/
│   ├── main/java/com/api/
│   │   ├── config/
│   │   │   └── ConfigReader.java                ← Reads config.properties
│   │   └── utils/
│   │       └── JsonReader.java                  ← Reads testdata/*.json files
│   │
│   └── test/
│       ├── java/com/api/tests/
│       │   ├── BaseTest.java                    ← RestAssured setup (shared)
│       │   └── LoginTest.java                   ← Your test methods live here
│       │
│       └── resources/
│           ├── config.properties                ← Base URL (change this, not code)
│           ├── testng.xml                        ← Controls which tests run
│           └── testdata/
│               └── login.json                   ← Test data (change this, not code)
```

---

## What Each File Does (One Line Each)

| File | What it does |
|------|-------------|
| `pom.xml` | Downloads RestAssured, TestNG, Jackson for you |
| `config.properties` | Stores the base URL — one place to change it |
| `login.json` | Stores request data for each test scenario |
| `ConfigReader.java` | Reads `config.properties` and gives values to tests |
| `JsonReader.java` | Reads `login.json` and converts it to a Java Map |
| `BaseTest.java` | Sets up RestAssured once — all tests inherit this |
| `LoginTest.java` | The actual 5 test methods |
| `testng.xml` | Tells TestNG which test classes to run |

---

## How to Run

### Requirement
- Java 11 or higher
- Maven installed (`mvn -version` to check)

### Run all tests
```bash
cd basic-restassured
mvn clean test
```

### Run one specific test method
```bash
mvn clean test -Dtest="LoginTest#test01_validLogin_shouldReturn200"
```

---

## The RestAssured Pattern (Memorise This)

```java
Response response =
    given()                          // ← SETUP: headers, body, params
        .contentType("application/json")
        .body(requestBody)
    .when()                          // ← ACTION: which HTTP method + which URL
        .post("/sbioa-auth/v2/auth/password-login")
    ;

// ← CHECK: look at what came back
int    statusCode = response.statusCode();
String body       = response.getBody().asString();
long   timeTaken  = response.time();
String field      = response.jsonPath().getString("data.token");
```

---

## Your Learning Path

```
Step 1 (NOW)
  → Run the project as-is
  → Look at the console output (request + response printed)

Step 2
  → Open test05 in LoginTest.java
  → Look at the real response your API returns
  → Uncomment and fill in the jsonPath assertions

Step 3
  → Add a new test scenario in login.json
  → Write a new @Test method that uses it

Step 4
  → Add a second endpoint (e.g. logout, profile)
  → Create a new test file (LogoutTest.java extends BaseTest)

Step 5 (Advanced)
  → Capture the login token from test01
  → Pass it as a header to a protected endpoint test
```

---

## Changing the Base URL (e.g. Staging → Production)

Open `src/test/resources/config.properties` and change:
```properties
base.url=https://sbioa-gateway-prod.coherent.in
```

That's it. No test code changes needed.

---

## Adding a New Test Scenario

1. Open `src/test/resources/testdata/login.json`
2. Add a new block:
```json
{
  "valid_user": { ... },
  "wrong_password": { ... },

  "invalid_mobile_format": {
    "mobile": "NOTANUMBER",
    "password": "12345678",
    "accountType": "STAFF",
    "platformType": "WEB",
    "deviceId": "e7027d46-9446-4a0c-8ff8-5fde2de9af9c",
    "token": "",
    "latitude": "13.061361543287191",
    "longitude": "80.26007440007787"
  }
}
```
3. In `LoginTest.java` add a new `@Test` method:
```java
@Test
public void test06_invalidMobileFormat_shouldReturn400() {
    Map<String, Object> body = JsonReader.getAsMap(TEST_DATA_FILE, "invalid_mobile_format");
    Response response = baseRequest().body(body).when().post(LOGIN_ENDPOINT);
    Assert.assertEquals(response.statusCode(), 400, "Should reject invalid mobile format");
}
```
