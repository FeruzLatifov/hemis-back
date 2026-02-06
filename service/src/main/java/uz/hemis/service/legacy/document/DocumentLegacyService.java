package uz.hemis.service.legacy.document;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.DiplomaBlank;
import uz.hemis.domain.repository.DiplomaBlankRepository;
import uz.hemis.service.legacy.CubaEntityMapHelper;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Document Legacy Service (Track 1)
 *
 * <p>CUBA Platform REST API compatible service for document entities:</p>
 * <ul>
 *   <li>DiplomaBlank (hemishe_EDiplomaBlank)</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DocumentLegacyService {

    private final DiplomaBlankRepository diplomaBlankRepository;

    private static final String DIPLOMA_BLANK_ENTITY_NAME = "hemishe_EDiplomaBlank";

    // ==================== DiplomaBlank ====================

    public Optional<DiplomaBlank> findDiplomaBlankById(UUID id) {
        return diplomaBlankRepository.findById(id);
    }

    public List<DiplomaBlank> findAllDiplomaBlank() {
        return diplomaBlankRepository.findAll();
    }

    public Page<DiplomaBlank> findAllDiplomaBlank(Pageable pageable) {
        return diplomaBlankRepository.findAll(pageable);
    }

    @Transactional
    public DiplomaBlank saveDiplomaBlank(DiplomaBlank entity) {
        return diplomaBlankRepository.save(entity);
    }

    @Transactional
    public void deleteDiplomaBlank(DiplomaBlank entity) {
        diplomaBlankRepository.delete(entity);
    }

    @Transactional
    public void softDeleteDiplomaBlank(DiplomaBlank entity) {
        entity.setDeleteTs(LocalDateTime.now());
        diplomaBlankRepository.save(entity);
    }

    public Map<String, Object> toDiplomaBlankMap(DiplomaBlank entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", DIPLOMA_BLANK_ENTITY_NAME);
        map.put("_instanceName", entity.getBlankCode() != null ? entity.getBlankCode() : "DiplomaBlank-" + entity.getId());
        map.put("id", entity.getId());

        CubaEntityMapHelper.putIfNotNull(map, "blankCode", entity.getBlankCode(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "series", entity.getSeries(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "number", entity.getNumber(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "_university", entity.getUniversity(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "_blankType", entity.getBlankType(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "_status", entity.getStatus(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "receivedDate", entity.getReceivedDate(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "issuedDate", entity.getIssuedDate(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "academicYear", entity.getAcademicYear(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "supplier", entity.getSupplier(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "batchNumber", entity.getBatchNumber(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "statusReason", entity.getStatusReason(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "securityFeatures", entity.getSecurityFeatures(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "notes", entity.getNotes(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "createTs", entity.getCreateTs(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "createdBy", entity.getCreatedBy(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "updateTs", entity.getUpdateTs(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "updatedBy", entity.getUpdatedBy(), returnNulls);

        return map;
    }

    public void updateDiplomaBlankFromMap(DiplomaBlank entity, Map<String, Object> map) {
        if (map.containsKey("blankCode")) entity.setBlankCode(CubaEntityMapHelper.getStringValue(map.get("blankCode")));
        if (map.containsKey("series")) entity.setSeries(CubaEntityMapHelper.getStringValue(map.get("series")));
        if (map.containsKey("number")) entity.setNumber(CubaEntityMapHelper.getStringValue(map.get("number")));
        if (map.containsKey("_university")) entity.setUniversity(CubaEntityMapHelper.getStringValue(map.get("_university")));
        if (map.containsKey("_blankType")) entity.setBlankType(CubaEntityMapHelper.getStringValue(map.get("_blankType")));
        if (map.containsKey("_status")) entity.setStatus(CubaEntityMapHelper.getStringValue(map.get("_status")));
        if (map.containsKey("receivedDate")) entity.setReceivedDate(CubaEntityMapHelper.parseLocalDate(map.get("receivedDate")));
        if (map.containsKey("issuedDate")) entity.setIssuedDate(CubaEntityMapHelper.parseLocalDate(map.get("issuedDate")));
        if (map.containsKey("academicYear")) entity.setAcademicYear(CubaEntityMapHelper.getIntegerValue(map.get("academicYear")));
        if (map.containsKey("supplier")) entity.setSupplier(CubaEntityMapHelper.getStringValue(map.get("supplier")));
        if (map.containsKey("batchNumber")) entity.setBatchNumber(CubaEntityMapHelper.getStringValue(map.get("batchNumber")));
        if (map.containsKey("statusReason")) entity.setStatusReason(CubaEntityMapHelper.getStringValue(map.get("statusReason")));
        if (map.containsKey("securityFeatures")) entity.setSecurityFeatures(CubaEntityMapHelper.getStringValue(map.get("securityFeatures")));
        if (map.containsKey("notes")) entity.setNotes(CubaEntityMapHelper.getStringValue(map.get("notes")));
    }
}
