package uz.hemis.service.infrastructure;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.dto.building.CadastreDto;
import uz.hemis.common.exception.BadRequestException;
import uz.hemis.common.exception.BusinessRuleException;
import uz.hemis.domain.entity.infrastructure.UniversityCadastre;
import uz.hemis.domain.repository.UniversityCadastreRepository;
import uz.hemis.service.integration.GatewayService;
import uz.hemis.service.integration.model.GatewayResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Kadastr ingest — {@code 172.18.9.171/kadastr/*} javobini {@code university_cadastre}ga saqlaydi.
 *
 * <p>Oqim: {@code /by-inn {tin}} → {@code cadastr_list} (cad_number'lar) → har biriga
 * {@code /by-cadnum} → to'liq detal → upsert. Har cad_number O'Z tranzaksiyasida
 * ({@code REQUIRES_NEW} self-proxy orqali) — bittasi fail bo'lsa qolgani ta'sirlanmaydi (poison-batch yo'q).</p>
 *
 * <p>Chidamlilik: xom {@code raw} JSONB har doim saqlanadi; kadastr xato (code≠1) →
 * {@code CADASTRE_NOT_FOUND} (retry yo'q); API o'lik (503/tarmoq) → {@code fetch_status=PENDING} (retry).</p>
 */
@Service
@Slf4j
public class CadastreIngestService {

    private final UniversityCadastreRepository repo;
    private final GatewayService gatewayService;

    /** Self-proxy: ingestByCadNum'ga per-item REQUIRES_NEW tranzaksiya qo'llanishi uchun (AOP self-invocation trap). */
    @Autowired @Lazy
    private CadastreIngestService self;

    public CadastreIngestService(UniversityCadastreRepository repo, GatewayService gatewayService) {
        this.repo = repo;
        this.gatewayService = gatewayService;
    }

    /**
     * INN bo'yicha barcha kadastr obyektlarini ingest qiladi.
     * Har cad_number alohida tranzaksiyada — biri fail bo'lsa qolgani davom etadi.
     */
    public CadastreIngestResult ingestByInn(String tin, boolean force) {
        GatewayResult r = gatewayService.getCadastreByInn(tin);
        if (r.statusCode() != 200 || r.body() == null) {
            throw new BadRequestException("Kadastr by-inn muvaffaqiyatsiz: status=" + r.statusCode());
        }
        List<String> cadNumbers = new ArrayList<>();
        JsonNode list = r.body().path("cadastr_list");
        if (list.isArray()) {
            list.forEach(n -> { if (!n.isNull()) cadNumbers.add(n.asText()); });
        }
        // Inkremental: force=false → bazada allaqachon COMPLETE bo'lganlarni kadastrdan QAYTA OLMAYMIZ
        // (tashqi API borish-kelishini tejaydi; takroriy sync tez). force=true → hammasini yangilaydi.
        Set<String> alreadyComplete = (force || cadNumbers.isEmpty())
                ? Set.of()
                : new HashSet<>(repo.findCompleteCadNumbers(cadNumbers));
        int ok = 0, failed = 0, pending = 0, skipped = 0;
        List<CadastreIngestItem> items = new ArrayList<>();
        for (String cad : cadNumbers) {
            if (alreadyComplete.contains(cad)) {
                skipped++;
                items.add(new CadastreIngestItem(cad, "COMPLETE", "bazada mavjud (qayta olinmadi)"));
                continue;
            }
            try {
                UniversityCadastre c = self.ingestByCadNum(cad);
                String status = c.getFetchStatus();   // COMPLETE yoki PENDING
                if ("PENDING".equals(status)) pending++; else ok++;
                items.add(new CadastreIngestItem(cad, status, c.getFetchError()));
            } catch (Exception e) {
                failed++;
                log.warn("Cadastre ingest failed: cad={}, error={}", cad, e.getMessage());
                items.add(new CadastreIngestItem(cad, "FAILED", e.getMessage()));
            }
        }
        log.info("Cadastre ingest by-inn done: tin={}, total={}, ok={}, pending={}, failed={}, skipped={}, force={}",
                tin, cadNumbers.size(), ok, pending, failed, skipped, force);
        return new CadastreIngestResult(tin, cadNumbers.size(), ok, pending, failed, skipped, items);
    }

    /**
     * Bitta cad_number bo'yicha detalni oladi va upsert qiladi (o'z tranzaksiyasida).
     * <ul>
     *   <li>OK (code=1) → to'liq parse + {@code fetch_status=COMPLETE}.</li>
     *   <li>API o'lik (503 / tarmoq) → {@code fetch_status=PENDING} saqlanadi (retry uchun).</li>
     *   <li>Kadastr topmadi (code≠1 / 4xx) → {@code BusinessRuleException("CADASTRE_NOT_FOUND")}.</li>
     * </ul>
     */
    @Transactional
    public UniversityCadastre ingestByCadNum(String cadNum) {
        GatewayResult r;
        try {
            r = gatewayService.getCadastreByCadnum(cadNum);
        } catch (BadRequestException e) {
            // api-mspd token yo'q yoki tarmoq xatosi — vaqtincha; PENDING + retry.
            log.warn("Kadastr xizmati vaqtincha ishlamayapti (cad={}): {}", cadNum, e.getMessage());
            return upsertPending(cadNum, "api_error: " + e.getMessage());
        }

        if (r.statusCode() == 503) {
            log.warn("Kadastr xizmati vaqtincha ishlamayapti (cad={}, 503)", cadNum);
            return upsertPending(cadNum, "kadastr_unavailable_503");
        }
        JsonNode b = r.body();
        boolean ok = r.statusCode() == 200 && b != null && b.path("code").asInt(0) == 1;
        if (!ok) {
            // Xato/topilmagan cad_number — odam tuzatadi, retry befoyda.
            throw new BusinessRuleException("CADASTRE_NOT_FOUND",
                    "Kadastr raqami topilmadi yoki noto'g'ri: " + cadNum);
        }

        UniversityCadastre c = repo.findByCadNumber(cadNum).orElseGet(UniversityCadastre::new);
        mapFields(b, c);
        c.setCadNumber(cadNum); // kalit — javobda bo'sh bo'lsa ham to'g'ri qolsin
        c.setFetchStatus("COMPLETE");
        c.setFetchError(null);
        c.setLastFetchAttempt(LocalDateTime.now());
        c.setSyncedAt(LocalDateTime.now());
        return repo.save(c);
    }

    /**
     * SERVE: cad_number bo'yicha kadastr obyektini qaytaradi. "Bizda bormi → yo'q bo'lsa olib kelamiz":
     * COMPLETE bo'lsa DB'dan; aks holda kadastrdan fetch qilib saqlaydi va qaytaradi.
     * Xato/topilmagan cad_number → {@code BusinessRuleException(CADASTRE_NOT_FOUND)} (422 rasmiy xabar).
     */
    public CadastreDto getByCadNumberOrFetch(String cadNum) {
        UniversityCadastre c = repo.findByCadNumber(cadNum)
                .filter(x -> "COMPLETE".equals(x.getFetchStatus()))
                .orElseGet(() -> self.ingestByCadNum(cadNum));
        return toDto(c);
    }

    /**
     * FK kafolati — bino saqlashдан OLDIN chaqiriladi. university_cadastre'da cad_number qatori borligini
     * ta'minlaydi:
     * <ul>
     *   <li>Bor → hech narsa qilmaydi.</li>
     *   <li>Yo'q → kadastrdan oladi (COMPLETE), yoki API o'lik bo'lsa PENDING placeholder — bino saqlanaversin.</li>
     *   <li>Raqam noto'g'ri: {@code strict=true} (web) → {@code CADASTRE_NOT_FOUND} (422, typo rad etiladi);
     *       {@code strict=false} (bulk sync) → FAILED placeholder (OTM ma'lumoti yo'qolmasin).</li>
     * </ul>
     *
     * <p><b>REQUIRES_NEW — O'Z tranzaksiyasida (bino-yozuv tx'idан ajratilgan):</b>
     * <ol>
     *   <li>Kadastr qatori bino insert/update'idan OLDIN COMMIT bo'ladi → FK darrov bajariladi
     *       (flush-tartibiga tayanmaydi — cad_number oddiy ustun).</li>
     *   <li><b>Poison-batch YO'Q:</b> bulk sync'da bir raqam xatosi (hatto {@code DataIntegrityViolationException})
     *       faqat SHU alohida tx'ni yiqitadi; batch tx rollback-only bo'lmaydi (chaqiruvchi per-item
     *       {@code catch} bilan davom etadi). Avvalgi bir-umumiy-tx variantida flush xatosi butun batch'ni buzardi.</li>
     *   <li>Tashqi HTTP ({@code ingestByCadNum→getCadastreByCadnum}) bino-batch tx'i ichida emas.</li>
     * </ol>
     * ⚠️ Cheklov: bulk syncда tashqi fetch hali outer batch tx OCHIQ turganда suspend orqali ishlaydi
     * (outer tx uzoq yashaydi — P5 batch per-item tx bilan to'liq hal qilinadi). Sync hozir jonli emas (P7).</p>
     */
    @org.springframework.transaction.annotation.Transactional(
            propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void ensureCadastreExists(String cadNumber, boolean strict) {
        if (cadNumber == null || cadNumber.isBlank() || repo.existsByCadNumber(cadNumber)) {
            return;
        }
        try {
            // MUHIM: `this.` (self-proxy EMAS) — shu REQUIRES_NEW tx ichida bajarilsin, CADASTRE_NOT_FOUND'ni
            // tx'ni rollback-only qilmasdan ushlay olaylik. `self.`ga o'zgartirilsa trap qaytadi (SEV-4).
            ingestByCadNum(cadNumber);   // COMPLETE, yoki API o'lik → PENDING placeholder
        } catch (BusinessRuleException e) {   // CADASTRE_NOT_FOUND — kadastr raqami noto'g'ri
            if (strict) throw e;              // web → 422 (typo rad etiladi)
            upsertMissing(cadNumber, e.getMessage());  // bulk → FAILED placeholder
        }
    }

    /** PENDING kadastrlarni retry job to'ldiradi. */
    @Transactional(readOnly = true)
    public List<String> findPendingCadNumbers(int limit) {
        return repo.findByFetchStatusOrderByLastFetchAttemptAsc("PENDING",
                org.springframework.data.domain.PageRequest.of(0, limit))
                .stream().map(UniversityCadastre::getCadNumber).toList();
    }

    public static CadastreDto toDto(UniversityCadastre c) {
        return CadastreDto.builder()
                .cadNumber(c.getCadNumber()).cadNumberOld(c.getCadNumberOld()).name(c.getName())
                .region(c.getRegion()).district(c.getDistrict()).address(c.getAddress())
                .shortAddress(c.getShortAddress()).street(c.getStreet()).domNum(c.getDomNum())
                .neighborhood(c.getNeighborhood())
                .tip(c.getTip()).tipText(c.getTipText()).vid(c.getVid()).vidText(c.getVidText())
                .objectRooms(c.getObjectRooms())
                .landArea(c.getLandArea()).landAreaB(c.getLandAreaB()).objectArea(c.getObjectArea())
                .objectAreaL(c.getObjectAreaL()).objectAreaU(c.getObjectAreaU())
                .cost(c.getCost()).banIs(c.getBanIs())
                .subjects(c.getSubjects()).documents(c.getDocuments())
                .fetchStatus(c.getFetchStatus()).syncedAt(c.getSyncedAt())
                .build();
    }

    /** API o'lik bo'lganda PENDING placeholder (retry job keyin to'ldiradi). */
    private UniversityCadastre upsertPending(String cadNum, String error) {
        UniversityCadastre c = repo.findByCadNumber(cadNum).orElseGet(UniversityCadastre::new);
        if (c.getCadNumber() == null) c.setCadNumber(cadNum);
        // Mavjud COMPLETE'ni PENDING'ga tushirmaymiz (eski to'liq ma'lumot saqlanadi).
        if (!"COMPLETE".equals(c.getFetchStatus())) {
            c.setFetchStatus("PENDING");
        }
        c.setFetchError(error);
        c.setLastFetchAttempt(LocalDateTime.now());
        return repo.save(c);
    }

    /** Kadastr raqami noto'g'ri (bulk sync) — FK saqlanishi uchun FAILED placeholder (retry YO'Q). */
    private UniversityCadastre upsertMissing(String cadNum, String error) {
        UniversityCadastre c = repo.findByCadNumber(cadNum).orElseGet(UniversityCadastre::new);
        if (c.getCadNumber() == null) c.setCadNumber(cadNum);
        if (!"COMPLETE".equals(c.getFetchStatus())) c.setFetchStatus("FAILED");
        c.setFetchError(error);
        c.setLastFetchAttempt(LocalDateTime.now());
        return repo.save(c);
    }

    // =====================================================
    // Parser — 172.18.9.171/kadastr/by-cadnum jonli javob shakli (2026-08-19)
    // =====================================================
    private void mapFields(JsonNode b, UniversityCadastre c) {
        c.setCadNumberOld(text(b, "cad_number_old"));
        c.setName(text(b, "name"));
        c.setDataSource(text(b, "data_source"));
        c.setResponseId(asLong(b.path("response_id")));
        c.setRegionId(asInt(b.path("region_id")));
        c.setRegion(text(b, "region"));
        c.setDistrictId(asInt(b.path("district_id")));
        c.setDistrict(text(b, "district"));
        c.setAddress(text(b, "address"));
        c.setShortAddress(text(b, "short_address"));
        c.setStreet(text(b, "street"));
        c.setStreetCode(text(b, "street_code"));
        c.setDomNum(text(b, "dom_num"));
        c.setKvartiraNum(text(b, "kvartira_num"));
        c.setNeighborhood(text(b, "neighborhood"));
        c.setNeighborhoodId(text(b, "neighborhood_id"));
        c.setTip(text(b, "tip"));
        c.setTipText(text(b, "tipText"));
        c.setVid(text(b, "vid"));
        c.setVidText(text(b, "vidText"));
        c.setObjectRooms(asInt(b.path("object_rooms")));
        c.setLandArea(dec(b.path("land_area")));
        c.setLandAreaI(dec(b.path("land_area_i")));
        c.setLandAreaB(dec(b.path("land_area_b")));
        c.setLandAreaF(dec(b.path("land_area_f")));
        c.setLandAreaZ(dec(b.path("land_area_z")));
        c.setLandAreaD(dec(b.path("land_area_d")));
        c.setLandAreaU(dec(b.path("land_area_u")));
        c.setObjectArea(dec(b.path("object_area")));
        c.setObjectAreaL(dec(b.path("object_area_l")));
        c.setObjectAreaU(dec(b.path("object_area_u")));
        c.setCost(asLong(b.path("cost")));
        c.setBanIs(asBool01(b.path("ban_is")));
        c.setEcoZone(text(b, "eco_zone"));
        c.setLandFundType(text(b, "land_fund_type"));
        c.setLandUseType(text(b, "land_use_type"));
        c.setLandFundCategory(text(b, "land_fund_category"));
        c.setSubjects(jsonb(b.path("subjects")));
        c.setDocuments(jsonb(b.path("documents")));
        c.setDocumentsL(jsonb(b.path("documents_l")));
        c.setBans(jsonb(b.path("bans")));
        c.setRaw(b.toString());
    }

    private static String text(JsonNode node, String field) {
        JsonNode v = node.path(field);
        if (v.isMissingNode() || v.isNull()) return null;
        String s = v.asText();
        return s.isBlank() ? null : s;
    }

    private static Integer asInt(JsonNode v) {
        if (v == null || v.isMissingNode() || v.isNull()) return null;
        if (v.isNumber()) return v.asInt();
        try { return Integer.valueOf(v.asText().trim()); } catch (NumberFormatException e) { return null; }
    }

    private static Long asLong(JsonNode v) {
        if (v == null || v.isMissingNode() || v.isNull()) return null;
        if (v.isNumber()) return v.asLong();
        try {
            String s = v.asText().trim();
            return s.isEmpty() ? null : Long.valueOf(s);
        } catch (NumberFormatException e) { return null; }
    }

    private static BigDecimal dec(JsonNode v) {
        if (v == null || v.isMissingNode() || v.isNull()) return null;
        if (v.isNumber()) return v.decimalValue();
        try {
            String s = v.asText().trim();
            return s.isEmpty() ? null : new BigDecimal(s);
        } catch (NumberFormatException e) { return null; }
    }

    /** Kadastr "0"/"1" (string yoki number) → Boolean. */
    private static Boolean asBool01(JsonNode v) {
        if (v == null || v.isMissingNode() || v.isNull()) return null;
        if (v.isBoolean()) return v.asBoolean();
        String s = v.asText().trim();
        if (s.isEmpty()) return null;
        return !"0".equals(s) && !"false".equalsIgnoreCase(s);
    }

    /** JsonNode (array/object) → JSONB string; bo'sh/null → null. */
    private static String jsonb(JsonNode v) {
        if (v == null || v.isMissingNode() || v.isNull()) return null;
        if (v.isArray() && v.isEmpty()) return null;
        return v.toString();
    }

    /**
     * Ingest natija hisobi + har bir kadastr raqami holati.
     * {@code completed} = shu safar kadastrdan olib saqlandi; {@code skipped} = bazada allaqachon COMPLETE (force=false'да o'tkazildi).
     */
    public record CadastreIngestResult(String tin, int total, int completed, int pending, int failed,
                                       int skipped, List<CadastreIngestItem> items) {}

    /** Bitta kadastr raqami ingest holati: status = COMPLETE | PENDING | FAILED; message = xato sababi (yoki null). */
    public record CadastreIngestItem(String cadNumber, String status, String message) {}
}
