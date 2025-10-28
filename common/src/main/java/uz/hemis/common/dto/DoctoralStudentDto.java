package uz.hemis.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DoctoralStudentDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;

    @JsonProperty("doctoral_code")
    private String doctoralCode;

    @JsonProperty("_student")
    private UUID student;

    @JsonProperty("_university")
    private String university;

    @JsonProperty("_department")
    private UUID department;

    @JsonProperty("_scientific_advisor")
    private UUID scientificAdvisor;

    @JsonProperty("_doctoral_student_type")
    private String doctoralStudentType;

    @JsonProperty("dissertation_topic")
    private String dissertationTopic;

    @JsonProperty("dissertation_topic_uz")
    private String dissertationTopicUz;

    @JsonProperty("dissertation_topic_ru")
    private String dissertationTopicRu;

    @JsonProperty("dissertation_topic_en")
    private String dissertationTopicEn;

    @JsonProperty("_speciality_code")
    private String specialityCode;

    @JsonProperty("admission_date")
    private LocalDate admissionDate;

    @JsonProperty("expected_defense_date")
    private LocalDate expectedDefenseDate;

    @JsonProperty("actual_defense_date")
    private LocalDate actualDefenseDate;

    @JsonProperty("_defense_status")
    private String defenseStatus;

    @JsonProperty("order_number")
    private String orderNumber;

    @JsonProperty("order_date")
    private LocalDate orderDate;

    @JsonProperty("research_direction")
    private String researchDirection;

    @JsonProperty("notes")
    private String notes;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("create_ts")
    private LocalDateTime createTs;

    @JsonProperty("created_by")
    private String createdBy;

    @JsonProperty("update_ts")
    private LocalDateTime updateTs;

    @JsonProperty("updated_by")
    private String updatedBy;
}
