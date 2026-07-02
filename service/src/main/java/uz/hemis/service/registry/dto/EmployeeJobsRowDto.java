package uz.hemis.service.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * Employee Jobs Registry Row DTO - flat list row.
 *
 * <p>READ-ONLY. Source table: {@code hemishe_e_employee_jobs} (read via EntityManager native
 * query). Employee resolved by full name only (PINFL/PII never exposed).</p>
 */
@Schema(name = "EmployeeJobsRegistryRow", description = "Employee job row in the flat paginated registry list")
public record EmployeeJobsRowDto(
    String id,
    String employeeId,
    String employeeName,
    String universityCode,
    String universityName,
    String departmentCode,
    String departmentName,
    String employeeTypeCode,
    String employeeTypeName,
    String positionCode,
    String positionName,
    String statusCode,
    String statusName,
    LocalDate jobStartDate,
    LocalDate jobEndDate,
    Boolean active
) {}
