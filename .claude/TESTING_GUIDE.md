# Testing Guide

> **Integration and unit testing standards for HEMIS Backend**

---

## Test Environment

Tests in the HEMIS backend are gated by the `TESTS_ENABLED` flag.  If `TESTS_ENABLED=true` is present in your
`.env` file or environment, the Gradle `test` task will execute; otherwise it aborts.  This prevents accidental
tests on environments where a database is not configured.

- **Environment variables** – Before running tests, load variables from `.env`.  They define database
  connections, Redis host and other settings.  Tests do not hardcode credentials.
- **Database usage** – Test configuration differs by module:
  - **app**: uses an H2 in‑memory database configured in `app/src/test/resources/application-test.yml`.  Flyway
    migrations are disabled and the schema is created and dropped automatically.  This allows fast
    integration tests without touching a real database.
  - **service** and **security**: connect to the master PostgreSQL database specified in your `.env` via
    `DB_MASTER_*` variables.  Flyway is disabled, so the tests run against the existing schema.  Use a
    dedicated local or staging database; **never** run tests against a production database.
- **Redis & other services** – Tests will connect to Redis and other services using the variables defined in
  `.env`.  Provide mock or local services for isolation.

## Integration Tests (Mandatory)

Every REST endpoint must have an integration test covering:

- ✅ Success case (`200` or `201` status) with valid input.
- ✅ Not found (`404`) when the requested resource does not exist.
- ✅ Unauthorised (`401`) when no authentication is provided.
- ✅ Forbidden (`403`) when the user lacks the required authority.
- ✅ Validation errors (`400`) when the input is invalid.
- ✅ Conflict (`409`) when a duplicate resource is created.
- ✅ Invalid input types (e.g. malformed IDs).

**Best practices:**

- Use `@SpringBootTest` with `@AutoConfigureMockMvc`.
- Use a test profile (`@ActiveProfiles("test")`) to isolate test configuration.
- Set up data in `@BeforeEach` or `@BeforeAll`; clean up in `@AfterEach`.
- Use `MockMvc` to perform requests and assert the responses.
- Use `@WithMockUser(username, authorities = {...})` to test authorisation scenarios.
- Always verify the JSON payload structure using `jsonPath`.

---

## Unit Tests (Mandatory)

Every service method must have a unit test covering:

- ✅ Successful execution.
- ✅ All expected exceptions and edge cases.
- ✅ Input validation logic.
- ✅ Interaction with dependencies (repositories, mappers, external services).

**Best practices:**

- Use JUnit 5 and Mockito for mocking dependencies.
- Keep tests fast and isolated; avoid hitting the database in unit tests.
- Test only one class at a time; use integration tests for multi‑component scenarios.
- Assert both the returned value and side effects (e.g. interactions with mocks).
- Use clear and descriptive test names.

---

## Example: Integration Test

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StudentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentRepository studentRepository;

    @BeforeEach
    void setup() {
        studentRepository.save(new Student(1L, "Alice"));
    }

    @Test
    void getStudent_success() throws Exception {
        mockMvc.perform(get("/api/students/1").with(user("admin").authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(1))
               .andExpect(jsonPath("$.name").value("Alice"));
    }

    @AfterEach
    void cleanup() {
        studentRepository.deleteAll();
    }
}
```
