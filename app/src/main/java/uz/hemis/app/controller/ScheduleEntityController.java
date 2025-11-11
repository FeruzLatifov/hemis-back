package uz.hemis.app.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.domain.entity.Schedule;
import uz.hemis.domain.repository.ScheduleRepository;

import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "Schedule")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_ESchedule")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class ScheduleEntityController {

    private final ScheduleRepository repository;
    private static final String ENTITY_NAME = "hemishe_ESchedule";

    @GetMapping("/{entityId}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable UUID entityId, @RequestParam(required = false) Boolean returnNulls) {
        Optional<Schedule> entity = repository.findById(entityId);
        if (entity.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(toMap(entity.get(), returnNulls));
    }

    @PutMapping("/{entityId}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable UUID entityId, @RequestBody Map<String, Object> body, @RequestParam(required = false) Boolean returnNulls) {
        Optional<Schedule> existingOpt = repository.findById(entityId);
        if (existingOpt.isEmpty()) return ResponseEntity.notFound().build();
        Schedule saved = repository.save(existingOpt.get());
        return ResponseEntity.ok(toMap(saved, returnNulls));
    }

    @DeleteMapping("/{entityId}")
    public ResponseEntity<Void> delete(@PathVariable UUID entityId) {
        Optional<Schedule> entity = repository.findById(entityId);
        if (entity.isEmpty()) return ResponseEntity.notFound().build();
        repository.delete(entity.get());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchGet(@RequestParam(required = false) String filter, @RequestParam(required = false) Boolean returnNulls) {
        List<Schedule> entities = repository.findAll();
        return ResponseEntity.ok(entities.stream().map(e -> toMap(e, returnNulls)).collect(Collectors.toList()));
    }

    @PostMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchPost(@RequestBody(required = false) Map<String, Object> filter, @RequestParam(required = false) Boolean returnNulls) {
        List<Schedule> entities = repository.findAll();
        return ResponseEntity.ok(entities.stream().map(e -> toMap(e, returnNulls)).collect(Collectors.toList()));
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAll(@RequestParam(defaultValue = "0") Integer offset, @RequestParam(defaultValue = "50") Integer limit, @RequestParam(required = false) String sort, @RequestParam(required = false) Boolean returnNulls) {
        Sort sorting = Sort.unsorted();
        if (sort != null && !sort.isEmpty()) {
            String[] parts = sort.split("-");
            sorting = Sort.by(parts.length > 1 && "desc".equalsIgnoreCase(parts[1]) ? Sort.Direction.DESC : Sort.Direction.ASC, parts[0]);
        }
        Page<Schedule> entityPage = repository.findAll(PageRequest.of(offset / limit, limit, sorting));
        return ResponseEntity.ok(entityPage.getContent().stream().map(e -> toMap(e, returnNulls)).collect(Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body, @RequestParam(required = false) Boolean returnNulls) {
        Schedule entity = new Schedule();
        Schedule saved = repository.save(entity);
        return ResponseEntity.ok(toMap(saved, returnNulls));
    }

    private Map<String, Object> toMap(Schedule entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", ENTITY_NAME);
        map.put("_instanceName", "Schedule-" + entity.getId());
        map.put("id", entity.getId());
        putIfNotNull(map, "_university", entity.getUniversity(), returnNulls);
        putIfNotNull(map, "_group", entity.getGroup(), returnNulls);
        putIfNotNull(map, "_course", entity.getCourse(), returnNulls);
        putIfNotNull(map, "_teacher", entity.getTeacher(), returnNulls);
        putIfNotNull(map, "_auditorium", entity.getAuditorium(), returnNulls);
        putIfNotNull(map, "scheduleDate", entity.getScheduleDate(), returnNulls);
        putIfNotNull(map, "startTime", entity.getStartTime(), returnNulls);
        putIfNotNull(map, "endTime", entity.getEndTime(), returnNulls);
        putIfNotNull(map, "dayOfWeek", entity.getDayOfWeek(), returnNulls);
        putIfNotNull(map, "pairNumber", entity.getPairNumber(), returnNulls);
        putIfNotNull(map, "academicYear", entity.getAcademicYear(), returnNulls);
        putIfNotNull(map, "semester", entity.getSemester(), returnNulls);
        putIfNotNull(map, "weekNumber", entity.getWeekNumber(), returnNulls);
        putIfNotNull(map, "_lessonType", entity.getLessonType(), returnNulls);
        putIfNotNull(map, "_scheduleType", entity.getScheduleType(), returnNulls);
        putIfNotNull(map, "active", entity.getActive(), returnNulls);
        putIfNotNull(map, "isCancelled", entity.getIsCancelled(), returnNulls);
        putIfNotNull(map, "createTs", entity.getCreateTs(), returnNulls);
        putIfNotNull(map, "createdBy", entity.getCreatedBy(), returnNulls);
        putIfNotNull(map, "updateTs", entity.getUpdateTs(), returnNulls);
        putIfNotNull(map, "updatedBy", entity.getUpdatedBy(), returnNulls);
        return map;
    }

    private void putIfNotNull(Map<String, Object> map, String key, Object value, Boolean returnNulls) {
        if (value != null || Boolean.TRUE.equals(returnNulls)) {
            map.put(key, value);
        }
    }
}
