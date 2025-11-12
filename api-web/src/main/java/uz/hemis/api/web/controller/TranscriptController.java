package uz.hemis.api.web.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.hemis.common.dto.ResponseWrapper;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Tag(name = "Transcripts")
@RestController
@RequestMapping("/app/rest/v2/transcripts")
@RequiredArgsConstructor
@Slf4j
public class TranscriptController {

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN', 'DEAN', 'STUDENT')")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getStudentTranscript(
            @PathVariable UUID studentId,
            @RequestParam(required = false) String academicYear
    ) {
        log.debug("Getting transcript for student: {}, year: {}", studentId, academicYear);

        Map<String, Object> transcript = new HashMap<>();
        transcript.put("studentId", studentId);
        transcript.put("academicYear", academicYear);
        transcript.put("gpa", 0.0);
        transcript.put("totalCredits", 0);
        transcript.put("generatedAt", LocalDate.now());

        return ResponseEntity.ok(ResponseWrapper.success(transcript));
    }

    @GetMapping("/generate/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN', 'DEAN')")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> generateTranscript(
            @PathVariable UUID studentId,
            @RequestParam(required = false, defaultValue = "pdf") String format
    ) {
        log.debug("Generating transcript for student: {} in format: {}", studentId, format);

        Map<String, Object> result = new HashMap<>();
        result.put("studentId", studentId);
        result.put("format", format);
        result.put("status", "generated");
        result.put("downloadUrl", "/api/transcripts/download/" + studentId);
        result.put("generatedAt", LocalDate.now());

        return ResponseEntity.ok(ResponseWrapper.success(result));
    }

    @GetMapping("/verify/{transcriptId}")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> verifyTranscript(
            @PathVariable String transcriptId
    ) {
        log.debug("Verifying transcript: {}", transcriptId);

        Map<String, Object> verification = new HashMap<>();
        verification.put("transcriptId", transcriptId);
        verification.put("isValid", true);
        verification.put("verifiedAt", LocalDate.now());

        return ResponseEntity.ok(ResponseWrapper.success(verification));
    }
}
