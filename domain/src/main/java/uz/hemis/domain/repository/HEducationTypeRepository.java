package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.hemis.domain.entity.classifier.HEducationType;

import java.util.List;

/**
 * Read access to the modern {@code h_education_type} classifier (PK = type code).
 * Resolves a type code → display name and lists the active types.
 */
public interface HEducationTypeRepository extends JpaRepository<HEducationType, String> {

    /** All active education types, in display order. */
    List<HEducationType> findByIsActiveTrueOrderBySortOrderAscCodeAsc();
}
