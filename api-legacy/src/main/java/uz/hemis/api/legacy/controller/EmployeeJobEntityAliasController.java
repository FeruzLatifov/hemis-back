package uz.hemis.api.legacy.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Alias controller to preserve legacy singular entity path used by old-HEMIS.
 * Forwards all requests from /app/rest/v2/entities/hemishe_EEmployeeJob/**
 * to the implemented plural endpoint /app/rest/v2/entities/hemishe_EEmployeeJobs/**
 * without changing HTTP method, path suffix, query string, or body.
 */
@Tag(name = "Employee Jobs")
@Hidden
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_EEmployeeJob")
@RequiredArgsConstructor
@Slf4j
public class EmployeeJobEntityAliasController {

    @RequestMapping("/**")
    public void forwardToPlural(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String context = request.getContextPath() == null ? "" : request.getContextPath();
        String uri = request.getRequestURI();
        String prefix = context + "/app/rest/v2/entities/hemishe_EEmployeeJob";
        String suffix = uri.length() > prefix.length() ? uri.substring(prefix.length()) : "";
        String target = "/app/rest/v2/entities/hemishe_EEmployeeJobs" + suffix;
        log.debug("Forwarding legacy EEmployeeJob request: {} -> {}", uri, target);
        request.getRequestDispatcher(target).forward(request, response);
    }
}


