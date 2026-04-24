package uz.hemis.common.dto.building;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Univer sync natija — muvaffaqiyatli/xato hisobi.
 */
@Data
@Builder
@Schema(name = "BuildingSyncResult")
public class BuildingSyncResult implements Serializable {

    private int totalProcessed;
    private int successCount;
    private int failureCount;

    @Builder.Default
    private List<Failure> failures = new ArrayList<>();

    public void recordSuccess() {
        totalProcessed++;
        successCount++;
    }

    public void recordFailure(String sourceUid, String message) {
        totalProcessed++;
        failureCount++;
        failures.add(new Failure(sourceUid, message));
    }

    @Getter
    @Schema(name = "BuildingSyncFailure")
    public static class Failure implements Serializable {
        private final String sourceUid;
        private final String message;

        public Failure(String sourceUid, String message) {
            this.sourceUid = sourceUid;
            this.message = message;
        }
    }
}
