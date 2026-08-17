package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.hemis.domain.entity.classifier.HEducationForm;

import java.util.List;

/**
 * Read access to the modern {@code h_education_form} classifier (PK = form code).
 * Used to populate the attachment education-form picker and to resolve/validate a form code.
 */
public interface HEducationFormRepository extends JpaRepository<HEducationForm, String> {

    /** All active forms, in display order — the dictionary for the FE dropdown. */
    List<HEducationForm> findByIsActiveTrueOrderBySortOrderAscCodeAsc();
}
