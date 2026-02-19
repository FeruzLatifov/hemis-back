package uz.hemis.common.dto.student;

import java.io.Serializable;

/**
 * Lightweight duplicate group summary for list view.
 * Does NOT include student details — use the detail endpoint for full analysis.
 *
 * @param pinfl              the shared PINFL
 * @param count              total student records with this PINFL
 * @param universityCount    number of distinct universities
 * @param activeCount        number of currently active records (status = '11')
 * @param reason             duplicate reason: NORMAL, CROSS_UNIVERSITY, SAME_UNIVERSITY, MULTI_LEVEL
 * @param representativeName full name from the most relevant student record
 */
public record DuplicateGroupDto(
        String pinfl,
        int count,
        int universityCount,
        int activeCount,
        String reason,
        String representativeName
) implements Serializable {}
