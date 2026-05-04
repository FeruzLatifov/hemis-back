package uz.hemis.common.dto.student;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.io.Serializable;

/**
 * Duplicate student statistics — categorized by reason.
 *
 * <p><strong>Duplicate Categories:</strong></p>
 * <ul>
 *   <li><strong>NORMAL:</strong> 0-1 active record, same university & speciality — re-enrollment</li>
 *   <li><strong>CROSS_UNIVERSITY:</strong> Active in 2+ universities simultaneously — serious issue</li>
 *   <li><strong>SAME_UNIVERSITY:</strong> 2+ active records in same university — data entry error</li>
 *   <li><strong>MULTI_LEVEL:</strong> Active in same university, different education types (bachelor+master)</li>
 *   <li><strong>INTERNAL_TRANSFER:</strong> Same university, different speciality — internal transfer</li>
 *   <li><strong>EXTERNAL_TRANSFER:</strong> Different universities, 0-1 active — transferred</li>
 * </ul>
 *
 * @param totalDuplicatePinfls    total PINFLs appearing more than once
 * @param totalAffectedStudents   total student records in duplicate groups
 * @param normalCount             duplicates where 0-1 are active, same univ & speciality
 * @param crossUniversityCount    active in 2+ different universities (serious)
 * @param sameUniversityCount     2+ active in same university, same education type (data error)
 * @param multiLevelCount         active in same university, different education types
 * @param internalTransferCount   same university, different speciality (internal transfer)
 * @param externalTransferCount   different universities, 0-1 active (external transfer)
 * @param maxDuplicateCount       highest number of records sharing one PINFL
 */
@JsonPropertyOrder({
    "totalDuplicatePinfls", "totalAffectedStudents",
    "normalCount", "crossUniversityCount", "sameUniversityCount",
    "multiLevelCount", "internalTransferCount", "externalTransferCount",
    "maxDuplicateCount"
})
public record DuplicateStatsDto(
        long totalDuplicatePinfls,
        long totalAffectedStudents,
        long normalCount,
        long crossUniversityCount,
        long sameUniversityCount,
        long multiLevelCount,
        long internalTransferCount,
        long externalTransferCount,
        int maxDuplicateCount
) implements Serializable {}
