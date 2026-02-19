package uz.hemis.common.dto.student;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Student Statistics DTO for Web UI stats cards
 *
 * @since 2.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentStatsDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("total")
    private long total;

    @JsonProperty("grantCount")
    private long grantCount;

    @JsonProperty("contractCount")
    private long contractCount;

    @JsonProperty("graduateCount")
    private long graduateCount;
}
