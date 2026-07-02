package uz.hemis.service.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * Employee Jobs Registry Detail DTO - detail-drawer payload for a single employee job.
 *
 * <p>READ-ONLY. All {@link EmployeeJobsRowDto} fields plus contract/decree columns.
 * PINFL/PII is never exposed.</p>
 */
@Schema(name = "EmployeeJobsRegistryDetail", description = "Full employee job detail for the read-only detail drawer")
public record EmployeeJobsDetailDto(
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
    Boolean active,
    String employeeFormCode,
    String employeeFormName,
    String rate,
    String contractNumber,
    LocalDate contractDate,
    String decreeNumber,
    LocalDate decreeDate
) {}
