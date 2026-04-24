package uz.hemis.service.infrastructure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uz.hemis.domain.entity.infrastructure.UniversityBuilding;
import uz.hemis.domain.entity.university.UniversityCadastre;
import uz.hemis.domain.repository.UniversityCadastreRepository;

import java.util.Optional;

/**
 * cad_number orqali cadastre'dan address/area'ni avtomatik to'ldiradi.
 * Faqat user qo'lda kiritmagan field'lar — override saqlanadi.
 * Cadastre topilmasa yoki xato bo'lsa — building saqlashga xalaqit bermaydi.
 */
@Component
@RequiredArgsConstructor
@Slf4j
class BuildingCadastreAutoFiller {

    private final UniversityCadastreRepository cadastreRepo;
    private final BuildingMetrics metrics;

    void autoFill(UniversityBuilding b) {
        String cadNumber = b.getCadNumber();
        if (cadNumber == null || cadNumber.isBlank()) {
            return;
        }
        try {
            Optional<UniversityCadastre> found = cadastreRepo.findByCadNumber(cadNumber);
            if (found.isPresent()) {
                applyFields(b, found.get());
                metrics.recordAutofillHit();
            } else {
                metrics.recordAutofillMiss();
            }
        } catch (Exception e) {
            log.warn("Cadastre lookup failed for cad_number={}: {}", cadNumber, e.getMessage());
            metrics.recordAutofillMiss();
        }
    }

    private void applyFields(UniversityBuilding b, UniversityCadastre c) {
        if (b.getAddress() == null) {
            b.setAddress(c.getAddress());
        }
        if (b.getTotalArea() == null && c.getObjectArea() != null) {
            b.setTotalArea(c.getObjectArea());
        }
        if (b.getUsableArea() == null && c.getObjectAreaU() != null) {
            b.setUsableArea(c.getObjectAreaU());
        }
    }
}
