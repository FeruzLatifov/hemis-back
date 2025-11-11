package uz.hemis.app.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.hemis.common.dto.ResponseWrapper;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "Classifiers")
@RestController
@RequestMapping("/app/rest/v2/classifiers-copy")
@RequiredArgsConstructor
@Slf4j
public class ClassifierCopyController {

    @PostMapping("/backup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> backupClassifiers() {
        log.info("Creating classifiers backup");

        Map<String, Object> result = new HashMap<>();
        result.put("status", "backed_up");
        result.put("backupId", "BKP-" + System.currentTimeMillis());

        return ResponseEntity.ok(ResponseWrapper.success(result));
    }

    @PostMapping("/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> restoreClassifiers(@RequestBody Map<String, String> request) {
        String backupId = request.get("backupId");
        log.info("Restoring classifiers from backup: {}", backupId);

        Map<String, Object> result = new HashMap<>();
        result.put("status", "restored");
        result.put("backupId", backupId);

        return ResponseEntity.ok(ResponseWrapper.success(result));
    }
}
