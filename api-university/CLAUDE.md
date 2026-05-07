# api-university module — University-Scoped Endpoints

> **Markaziy HEMIS-back** ichida **224 ta Univer Yii2 PHP backend** uchun B2B integratsiya kanali (vazirlik ↔ OTM).
>
> **Auth:** OAuth 2.1 `client_credentials` (per-OTM client_id + secret + IP whitelist) — ADR-0005.
>
> **Multi-tenant:** bitta markaziy DB ichida 230 OTM ma'lumoti. UNIVERSITY_ADMIN role + `university_code` filter — har OTM faqat o'z rows'ini ko'radi (rows level isolation).
>
> **Mijozlari:**
> - 224 ta Univer Yii2 PHP (per-OTM, OAuth client_credentials orqali)
> - Kelajakda davlat sistemalari (MyGov, MSPD) ham `oauth_client.client_type='GOVERNMENT'` orqali

---

## Critical Rules

### 1. University Scope Validation — MAJBURIY

Har endpoint UNIVERSITY_ADMIN uchun:
- User'ning `universityId` claim'i token'da
- Resource'ning `universityId` ga mos bo'lishi shart
- SUPER_ADMIN/MINISTRY_ADMIN bypass

```java
// ✓ TO'G'RI — university scope check
@GetMapping("/{id}")
@PreAuthorize("hasAuthority('students.view') and @universityScope.canAccess(#id, authentication)")
public ResponseEntity<StudentDto> getStudent(@PathVariable Long id) { ... }

// Bean
@Component("universityScope")
@RequiredArgsConstructor
public class UniversityScopeChecker {

    private final StudentRepository studentRepo;

    public boolean canAccess(Long studentId, Authentication auth) {
        // SUPER_ADMIN bypass
        if (hasAuthority(auth, "admin.full")) return true;

        Long userUniversityId = ((CustomPrincipal) auth.getPrincipal()).universityId();
        if (userUniversityId == null) return false;  // No scope → deny

        return studentRepo.findUniversityIdByStudentId(studentId)
            .map(uni -> uni.equals(userUniversityId))
            .orElse(false);
    }

    private boolean hasAuthority(Authentication auth, String authority) {
        return auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals(authority));
    }
}
```

### 2. List Endpoint — Auto-filter by University

```java
// ✗ XATO — barcha universitet'lardan talaba qaytadi
@GetMapping
public Page<StudentDto> list(Pageable pageable) {
    return studentService.findAll(pageable);
}

// ✓ TO'G'RI — service'da auto-filter
@GetMapping
@PreAuthorize("hasAuthority('students.view')")
public Page<StudentDto> list(Pageable pageable, Authentication auth) {
    Long universityId = extractUniversityId(auth);
    return studentService.findAllByUniversity(universityId, pageable);
}
```

**Service implementation:**
```java
public Page<StudentDto> findAllByUniversity(Long universityId, Pageable pageable) {
    if (universityId == null) {
        // SUPER_ADMIN: barcha universitetlar
        return studentRepo.findAll(pageable).map(mapper::toDto);
    }
    return studentRepo.findByUniversityId(universityId, pageable).map(mapper::toDto);
}
```

### 3. Cross-University Access TAQIQ

UNIVERSITY_ADMIN boshqa universitet ma'lumotini hech qachon ko'rmasligi shart. Validation:

```java
@PostMapping("/transfer")
@PreAuthorize("hasAuthority('students.transfer')")
public ResponseEntity<...> transferStudent(@RequestBody TransferRequest req, Authentication auth) {
    Long callerUniversityId = extractUniversityId(auth);
    Long studentUniversityId = studentService.getUniversityId(req.studentId());

    if (callerUniversityId != null && !callerUniversityId.equals(studentUniversityId)) {
        throw new ForbiddenException("Cannot transfer student from another university");
    }
    // ... transfer logic
}
```

### 4. Aggregate Query — Performance Trap

```java
// ✗ XATO — har universitet uchun alohida query
public Map<Long, Long> studentCountPerUniversity() {
    Map<Long, Long> result = new HashMap<>();
    for (Long uniId : getAllUniversityIds()) {
        result.put(uniId, studentRepo.countByUniversityId(uniId));  // 230 query!
    }
    return result;
}

// ✓ TO'G'RI — single query group by
@Query("""
    SELECT s.universityId, COUNT(s)
    FROM Student s
    WHERE s.deletedAt IS NULL
    GROUP BY s.universityId
""")
List<Object[]> countByUniversityNative();
```

---

## Multi-Tenant Best Practices

### Database — Single Schema, Tenant Column

Bizdagi pattern: bitta schema, har entity'da `university_id` column. Avzallik: cross-tenant report oson.

```java
@Entity
@Table(name = "hemishe_e_student",
       indexes = @Index(name = "idx_student_university", columnList = "university_id, faculty_id"))
public class Student extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "university_id", nullable = false)
    private University university;

    // Composite index (university_id, faculty_id) — most queries filter both
}
```

### Hibernate Filter (alternativa)

```java
@FilterDef(name = "universityFilter", parameters = @ParamDef(name = "uniId", type = Long.class))
@Filter(name = "universityFilter", condition = "university_id = :uniId")

// Service'da:
em.unwrap(Session.class)
    .enableFilter("universityFilter")
    .setParameter("uniId", currentUniversityId);
```

**Trade-off:** Filter har query'ga avto qo'shiladi (good), lekin native query'da ishlamaydi (bad).

### Cache Key — University-Scoped

```java
// ✓ TO'G'RI — cache key universityId bilan
@Cacheable(value = "studentsByFaculty",
           key = "#universityId + ':' + #facultyId + ':' + #pageable.pageNumber")
public Page<StudentDto> findByFacultyId(Long universityId, Long facultyId, Pageable p) { ... }
```

Aks holda: Universitet A admin Universitet B cache'idagi ma'lumotni ko'radi (data leak!).

---

## URL Pattern

```
/api/v1/university/students
/api/v1/university/faculties
/api/v1/university/employees
/api/v1/university/curriculum
```

`/{universityId}/` URL'da YOQ — universityId token'dan olinadi (URL spoofing'dan himoya).

---

## PR Checklist

- [ ] Har endpoint `@PreAuthorize` + university scope check
- [ ] List query — service'da auto-filter by university
- [ ] Mutation endpoint — cross-university check (Forbidden)
- [ ] Cache key includes universityId
- [ ] Aggregate query — single SQL with GROUP BY (not N queries)
- [ ] No `/{universityId}/` in URL (use token claim)
- [ ] Test: cross-university access returns 403
- [ ] Test: SUPER_ADMIN bypass works

---

## See Also
- `../security/CLAUDE.md` — `@PreAuthorize` patterns, custom SpEL
- `../api-web/CLAUDE.md` — Modern REST patterns
