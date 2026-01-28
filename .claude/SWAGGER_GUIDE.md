# Swagger Documentation Guide

> **Mandatory guidelines for annotating REST APIs with Swagger/OpenAPI**

---

## Why Swagger?

Proper Swagger documentation ensures that our APIs are discoverable, easy to consume and consistent across all modules. The CI/CD pipeline enforces Swagger requirements; missing documentation will cause your pull request to be rejected.

---

## Required Annotations

When creating or modifying a controller:

- Add a `@Tag(name, description)` annotation on every controller class.
- Annotate each endpoint with `@Operation(summary, description)` to describe its purpose.
- Provide an `@ApiResponses` annotation listing **all** possible HTTP status codes returned by the endpoint:
  - `200` / `201` – success
  - `400` – validation error
  - `401` – unauthenticated
  - `403` – forbidden
  - `404` – resource not found
  - `409` – conflict (e.g. duplicate record)
  - `500` – unexpected error
- Use `@Parameter` for each path variable and query parameter:
  - Provide a description, set `required=true` if applicable, and include an `example`.
  - Specify type and constraints via `@Schema(type, format, minimum, maximum, enumeration)`.
- For POST/PUT/PATCH endpoints, add a `@io.swagger.v3.oas.annotations.parameters.RequestBody` annotation:
  - Describe the request body and reference a DTO via `@Schema(implementation = YourDto.class)`.
  - Provide an example payload using `@ExampleObject(name, value = "...")`.

---

## Do and Don’t Checklist

- ✅ **Do** test your endpoints in Swagger UI (`/swagger-ui.html`) before committing.
- ✅ **Do** keep summaries short but descriptive; put details in `description`.
- ✅ **Do** include example values for all parameters and fields.
- ✅ **Do** use meaningful names for tags and operations.
- ❌ **Don’t** omit error responses; document all possible failures.
- ❌ **Don’t** duplicate schemas; use shared DTOs where possible.
- ❌ **Don’t** mix business logic into controllers; documentation should reflect the API, not internal details.

---

## Example Controller

```java
@RestController
@RequestMapping("/api/students")
@Tag(name = "Students", description = "Operations on student resources")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @Operation(
        summary = "Get student by ID",
        description = "Returns a single student matching the provided ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Student found"),
        @ApiResponse(responseCode = "404", description = "Student not found"),
        @ApiResponse(responseCode = "401", description = "Unauthenticated"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/{id}")
    public StudentDto getStudent(@Parameter(description = "ID of the student", required = true, example = "123")
                                 @PathVariable Long id) {
        return studentService.getStudent(id);
    }
}
```
