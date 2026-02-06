package uz.hemis.api.legacy.controller.system;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * CUBA Catch-All Controller
 *
 * <p>Mavjud bo'lmagan entity va service endpointlar uchun CUBA formatida 404 qaytaradi.</p>
 * <p>Old-hemis (CUBA Platform) aniq formatda 404 qaytaradi:</p>
 * <ul>
 *   <li>Service not found: {"error": "Service method not found", "details": "serviceName.methodName()"}</li>
 *   <li>Entity not found: {"error": "MetaClass not found", "details": "MetaClass hemishe_X not found"}</li>
 * </ul>
 *
 * <p>Bu controller eng past prioritetda ishlaydi — faqat mavjud controllerlar handle qilmaydigan
 * URL lar shu yerga tushadi.</p>
 *
 * @since 2.0.0
 */
@RestController
@Slf4j
public class CubaCatchAllController {

    /**
     * Mavjud bo'lmagan service method uchun CUBA 404
     * Pattern: /app/rest/v2/services/{service}/{method}
     *
     * Spring MVC da aniq service controllerlar (masalan, /app/rest/v2/services/student/...)
     * bu catch-all dan ustun turadi.
     */
    @RequestMapping("/app/rest/v2/services/{service}/{method}")
    public ResponseEntity<Map<String, Object>> serviceMethodNotFound(
            @PathVariable String service,
            @PathVariable String method,
            HttpServletRequest request) {

        log.debug("CUBA catch-all: service not found - {}.{}() [{}]",
                service, method, request.getRequestURI());

        Map<String, Object> error = new LinkedHashMap<>();
        error.put("error", "Service method not found");
        error.put("details", service + "." + method + "()");
        return ResponseEntity.status(404).body(error);
    }

    /**
     * Mavjud bo'lmagan service method (path segment bilan) uchun CUBA 404
     * Pattern: /app/rest/v2/services/{service}/{method}/{extra}
     */
    @RequestMapping("/app/rest/v2/services/{service}/{method}/{extra}")
    public ResponseEntity<Map<String, Object>> serviceMethodNotFoundWithExtra(
            @PathVariable String service,
            @PathVariable String method,
            @PathVariable String extra,
            HttpServletRequest request) {

        log.debug("CUBA catch-all: service not found - {}.{}() extra={} [{}]",
                service, method, extra, request.getRequestURI());

        Map<String, Object> error = new LinkedHashMap<>();
        error.put("error", "Service method not found");
        error.put("details", service + "." + method + "()");
        return ResponseEntity.status(404).body(error);
    }

    /**
     * Mavjud bo'lmagan entity uchun CUBA 404 — GET list
     * Pattern: /app/rest/v2/entities/{entity}
     */
    @RequestMapping("/app/rest/v2/entities/{entity}")
    public ResponseEntity<Map<String, Object>> entityNotFound(
            @PathVariable String entity,
            HttpServletRequest request) {

        log.debug("CUBA catch-all: entity not found - {} [{}]", entity, request.getRequestURI());

        Map<String, Object> error = new LinkedHashMap<>();
        error.put("error", "MetaClass not found");
        error.put("details", "MetaClass " + entity + " not found");
        return ResponseEntity.status(404).body(error);
    }

    /**
     * Mavjud bo'lmagan entity uchun CUBA 404 — GET by ID / PUT / DELETE
     * Pattern: /app/rest/v2/entities/{entity}/{entityId}
     */
    @RequestMapping("/app/rest/v2/entities/{entity}/{entityId}")
    public ResponseEntity<Map<String, Object>> entityWithIdNotFound(
            @PathVariable String entity,
            @PathVariable String entityId,
            HttpServletRequest request) {

        log.debug("CUBA catch-all: entity not found - {} id={} [{}]",
                entity, entityId, request.getRequestURI());

        Map<String, Object> error = new LinkedHashMap<>();
        error.put("error", "MetaClass not found");
        error.put("details", "MetaClass " + entity + " not found");
        return ResponseEntity.status(404).body(error);
    }
}
