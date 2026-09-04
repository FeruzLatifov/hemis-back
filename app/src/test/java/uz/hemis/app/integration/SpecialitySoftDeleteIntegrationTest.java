package uz.hemis.app.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@code h_speciality} SOFT DELETE (M013) end-to-end, against the real PostgreSQL container.
 *
 * <p>The unit tests ({@code HSpecialityServiceDeleteTest} / {@code HSpecialityServiceRestoreTest})
 * pin the service's decisions with mocks. They cannot see the two things that make soft delete
 * actually work, because both live in the database and in Hibernate's SQL generation:</p>
 * <ol>
 *   <li><b>{@code @SQLRestriction("deleted_at IS NULL")}</b> — is the stamped row really invisible to
 *       the grid, the tree, {@code GET/PUT/DELETE /{id}} and the OTM distribution pull, while still
 *       physically present? Only a real query can answer that.</li>
 *   <li><b>{@code uq_h_speciality_identity_live}</b> — M013 swapped the full-table identity constraint
 *       for a PARTIAL unique index ({@code WHERE deleted_at IS NULL}). If that swap were wrong, the
 *       admin could delete a speciality and then be unable to re-create it — a dead end with no error
 *       message that makes sense. {@link #deleteFreesTheIdentitySlot()} is the proof, and the single
 *       most valuable assertion in this file.</li>
 * </ol>
 *
 * <p><b>No {@code @Transactional} on this class, deliberately.</b> Each MockMvc call must COMMIT so the
 * raw {@link JdbcTemplate} probes observe what the database really holds; a test-managed transaction
 * would let the service's writes sit unflushed in the same transaction and the native counts would
 * read stale rows. Isolation comes from {@link #NAME_PREFIX}-scoped rows and {@link #cleanUp()} instead.</p>
 *
 * <p>Harness (Testcontainers + hand-run Liquibase + legacy CUBA stub) comes from
 * {@link AbstractIntegrationTest}; the MockMvc is built by hand with {@code springSecurity()} because
 * Boot 4 dropped the auto-applied security setup, so {@code @WithMockUser} would otherwise never reach
 * the filter chain.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        // JWT validatsiyasini testda o'chirish (@WithMockUser bilan ishlaymiz)
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=",
        "spring.security.oauth2.resourceserver.jwt.issuer-uri="
})
@WithMockUser(username = SpecialitySoftDeleteIntegrationTest.CURATOR, authorities = {
        "classifiers.speciality.view",
        "classifiers.speciality.create",
        "classifiers.speciality.edit",
        "classifiers.speciality.approve",
        "classifiers.speciality.delete"
})
@DisplayName("h_speciality soft delete (M013) — real container")
class SpecialitySoftDeleteIntegrationTest extends AbstractIntegrationTest {

    static final String CURATOR = "int.curator";

    private static final String BASE = "/api/v1/web/classifiers/speciality";
    private static final String DISTRIBUTION = "/api/v1/university/classifiers/speciality";

    /** '11' = Bakalavr (V022 seeds it unconditionally, so the FK resolves on a legacy-less container). */
    private static final String EDU_TYPE = "11";

    /**
     * Every row this class creates is named with this prefix — ASCII only (MockMvc hands the body back
     * in the response's own charset, so non-ASCII names would compare as mojibake) and unique enough
     * that {@link #cleanUp()} can never reach a seeded classifier row.
     */
    private static final String NAME_PREFIX = "ZZ M013 ";

    /** An OTM code the legacy stub fixture inserts — the attachment FK target. */
    private static final String UNIVERSITY_CODE = "301";

    @Autowired private WebApplicationContext context;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private DataSource dataSource;

    private MockMvc mockMvc;
    private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
        jdbc = new JdbcTemplate(dataSource);
    }

    /**
     * Removes only this class's own rows — including the soft-deleted ones, which no API call can
     * reach any more. Children before parents (self-FK {@code ON DELETE RESTRICT}), attachments and
     * years before the specialities they point at.
     */
    @AfterEach
    void cleanUp() {
        jdbc.update("DELETE FROM university_speciality_attachment WHERE speciality_id IN "
                + "(SELECT id FROM h_speciality WHERE name_uz LIKE ?)", NAME_PREFIX + "%");
        jdbc.update("DELETE FROM h_speciality_year WHERE speciality_id IN "
                + "(SELECT id FROM h_speciality WHERE name_uz LIKE ?)", NAME_PREFIX + "%");
        jdbc.update("DELETE FROM h_speciality WHERE name_uz LIKE ? AND parent_id IS NOT NULL", NAME_PREFIX + "%");
        jdbc.update("DELETE FROM h_speciality WHERE name_uz LIKE ?", NAME_PREFIX + "%");
    }

    // =====================================================
    // (a) + (b) — the row stays, but nothing can see it
    // =====================================================

    @Test
    @DisplayName("DELETE keeps the row and its years, and hides it from every read path")
    void deleteStampsTheRowAndHidesIt() throws Exception {
        String code = "99900001";
        String id = createSpeciality(code, NAME_PREFIX + "Alpha", null, List.of(2025, 2026));

        assertThat(countRows(id)).as("the row exists before the delete").isEqualTo(1);

        mockMvc.perform(delete(BASE + "/{id}", id).with(csrf()))
                .andExpect(status().isNoContent());

        // (a) STILL PRESENT physically — 224 OTMs and the legacy student tables reference this UUID.
        assertThat(countRows(id)).as("soft delete must NOT remove the row").isEqualTo(1);
        assertThat(deletedAt(id)).as("deleted_at must be stamped").isNotNull();
        assertThat(jdbc.queryForObject(
                "SELECT deleted_by FROM h_speciality WHERE id = ?::uuid", String.class, id))
                .isEqualTo(CURATOR);
        // Years are KEPT — the ON DELETE CASCADE can no longer fire, so restore returns an intact row.
        assertThat(yearCount(id)).as("editions survive the delete").isEqualTo(2);

        // (b) …and invisible everywhere.
        mockMvc.perform(get(BASE + "/{id}", id)).andExpect(status().isNotFound());
        mockMvc.perform(put(BASE + "/{id}", id).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("nameUz", NAME_PREFIX + "Alpha", "years", List.of(2026)))))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete(BASE + "/{id}", id).with(csrf()))
                .andExpect(status().isNotFound());

        // Flat grid: the code is unique to this test, so an empty page is an exact statement.
        mockMvc.perform(get(BASE).param("q", code))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0));

        // Tree: the id must not appear anywhere in the rendered hierarchy.
        String tree = mockMvc.perform(get(BASE + "/tree").param("educationType", EDU_TYPE))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(tree).doesNotContain(id);

        // The deleted bin is the ONLY place it is still visible — and it names who removed it.
        String bin = mockMvc.perform(get(BASE + "/deleted"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(bin).contains(id).contains(CURATOR);
    }

    /**
     * The OTM distribution pull must lose the row because it is DELETED, not merely because it is
     * NEEDS_REVIEW.
     *
     * <p>A created row is born NEEDS_REVIEW, and {@code findAllForDistribution} already filters that
     * out — so deleting it and finding it absent would prove nothing. The status is therefore flipped
     * in raw SQL on both sides of the delete: APPROVED + live must be IN the pull, APPROVED + deleted
     * must be OUT of it. The only difference between the two probes is {@code deleted_at}, which makes
     * this an assertion about {@code @SQLRestriction} and nothing else.</p>
     *
     * <p>Cache note: {@code getDistribution} is {@code @Cacheable("specialityDistribution")}, and both
     * {@code create} and {@code delete} are {@code @CacheEvict(allEntries = true)}. Each pull below is
     * therefore preceded by an evicting write, so neither reads a stale snapshot.</p>
     */
    @Test
    @DisplayName("a deleted row leaves the OTM distribution pull — even when it looks distributable")
    void deletedRowLeavesTheDistributionPull() throws Exception {
        String id = createSpeciality("99900002", NAME_PREFIX + "Beta", null, List.of(2026));

        // Live + APPROVED → the pull carries it (proves the probe below is not vacuous).
        setReviewStatus(id, "APPROVED");
        assertThat(distributionBody()).as("a live APPROVED row must reach the OTMs").contains(id);

        // Delete needs NEEDS_REVIEW; the DELETE itself evicts the distribution cache.
        setReviewStatus(id, "NEEDS_REVIEW");
        mockMvc.perform(delete(BASE + "/{id}", id).with(csrf()))
                .andExpect(status().isNoContent());

        // Same row, same APPROVED status, only deleted_at differs.
        setReviewStatus(id, "APPROVED");
        assertThat(distributionBody()).as("a deleted row must never reach the OTMs").doesNotContain(id);
    }

    // =====================================================
    // (c) — the partial unique index
    // =====================================================

    /**
     * The heart of M013: {@code uq_h_speciality_identity_live} is PARTIAL
     * ({@code WHERE deleted_at IS NULL}), so a deleted row no longer owns its
     * {@code (education_type, code, name_search)} slot and the admin can re-create what they removed.
     * Under the pre-M013 full-table constraint this POST would fail with a raw 23505.
     */
    @Test
    @DisplayName("re-creating the SAME (type, code, name, year) after a delete SUCCEEDS")
    void deleteFreesTheIdentitySlot() throws Exception {
        String code = "99900003";
        String name = NAME_PREFIX + "Gamma";
        String firstId = createSpeciality(code, name, null, List.of(2026));

        mockMvc.perform(delete(BASE + "/{id}", firstId).with(csrf()))
                .andExpect(status().isNoContent());

        // Identical payload — not a merge either: findExactTwins is JPQL, so it cannot see the
        // deleted row and a brand-new row is inserted rather than a 409 or a year-merge.
        String secondId = createSpeciality(code, name, null, List.of(2026));

        assertThat(secondId).as("a NEW row, not the resurrected one").isNotEqualTo(firstId);
        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM h_speciality WHERE education_type = ? AND code = ? AND name_uz = ?",
                Integer.class, EDU_TYPE, code, name))
                .as("both rows coexist — one deleted, one live").isEqualTo(2);
        assertThat(deletedAt(firstId)).isNotNull();
        assertThat(deletedAt(secondId)).isNull();
    }

    // =====================================================
    // (d) — restore
    // =====================================================

    @Test
    @DisplayName("POST /{id}/restore brings the row back with its years intact")
    void restoreBringsTheRowBackWithYears() throws Exception {
        String id = createSpeciality("99900004", NAME_PREFIX + "Delta", null, List.of(2024, 2025, 2026));

        mockMvc.perform(delete(BASE + "/{id}", id).with(csrf()))
                .andExpect(status().isNoContent());

        mockMvc.perform(post(BASE + "/{id}/restore", id).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.years.length()").value(3))
                .andExpect(jsonPath("$.data.years", containsInAnyOrder(2024, 2025, 2026)));

        assertThat(deletedAt(id)).as("the stamp is cleared").isNull();
        assertThat(jdbc.queryForObject(
                "SELECT deleted_by FROM h_speciality WHERE id = ?::uuid", String.class, id)).isNull();
        assertThat(yearCount(id)).isEqualTo(3);

        // Back in every read path.
        mockMvc.perform(get(BASE + "/{id}", id)).andExpect(status().isOk());
        mockMvc.perform(get(BASE + "/deleted"))
                .andExpect(status().isOk())
                .andExpect(result -> assertThat(result.getResponse().getContentAsString()).doesNotContain(id));
    }

    @Test
    @DisplayName("restore is refused (422) once a live row has taken the identity slot")
    void restoreRefusesWhenIdentityWasReclaimed() throws Exception {
        String code = "99900005";
        String name = NAME_PREFIX + "Epsilon";
        String deletedId = createSpeciality(code, name, null, List.of(2026));

        mockMvc.perform(delete(BASE + "/{id}", deletedId).with(csrf()))
                .andExpect(status().isNoContent());
        createSpeciality(code, name, null, List.of(2026)); // the slot is taken again

        mockMvc.perform(post(BASE + "/{id}/restore", deletedId).with(csrf()))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.error").value("SPECIALITY_RESTORE_IDENTITY_TAKEN"));

        assertThat(deletedAt(deletedId)).as("a refused restore leaves the row deleted").isNotNull();
    }

    // =====================================================
    // (e) — the three delete guards, unchanged rule codes
    // =====================================================

    @Test
    @DisplayName("guard 1 — APPROVED row: 422 SPECIALITY_DELETE_APPROVED_FORBIDDEN, no stamp")
    void deleteGuardApproved() throws Exception {
        String id = createSpeciality("99900006", NAME_PREFIX + "Zeta", null, List.of(2026));
        setReviewStatus(id, "APPROVED");

        mockMvc.perform(delete(BASE + "/{id}", id).with(csrf()))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.error").value("SPECIALITY_DELETE_APPROVED_FORBIDDEN"));

        assertThat(deletedAt(id)).isNull();
    }

    @Test
    @DisplayName("guard 2 — parent with a child: 422 SPECIALITY_HAS_CHILDREN_DELETE_FIRST, no stamp")
    void deleteGuardChildren() throws Exception {
        String parentId = createSpeciality("99900007", NAME_PREFIX + "Eta parent", null, List.of(2026));
        String childId = createSpeciality("99900008", NAME_PREFIX + "Eta child",
                UUID.fromString(parentId), List.of(2026));

        mockMvc.perform(delete(BASE + "/{id}", parentId).with(csrf()))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.error").value("SPECIALITY_HAS_CHILDREN_DELETE_FIRST"))
                // The message names the blocker, so the admin knows what to move first.
                .andExpect(jsonPath("$.message", containsString("99900008")));

        assertThat(deletedAt(parentId)).isNull();
        assertThat(deletedAt(childId)).isNull();

        // Deleting the child first unblocks the parent — the guard is about LIVE children only.
        mockMvc.perform(delete(BASE + "/{id}", childId).with(csrf())).andExpect(status().isNoContent());
        mockMvc.perform(delete(BASE + "/{id}", parentId).with(csrf())).andExpect(status().isNoContent());
        assertThat(deletedAt(parentId)).isNotNull();
    }

    @Test
    @DisplayName("guard 3 — attached to an OTM: 422 SPECIALITY_ATTACHED_TO_UNIVERSITY, no stamp")
    void deleteGuardAttachment() throws Exception {
        String id = createSpeciality("99900009", NAME_PREFIX + "Theta", null, List.of(2026));
        jdbc.update("INSERT INTO university_speciality_attachment "
                        + "(university_code, speciality_id, education_form, edu_year, status) "
                        + "VALUES (?, ?::uuid, '11', 2026, 'ACTIVE')",
                UNIVERSITY_CODE, id);

        mockMvc.perform(delete(BASE + "/{id}", id).with(csrf()))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.error").value("SPECIALITY_ATTACHED_TO_UNIVERSITY"))
                // The blocking OTM code is named — the admin knows where to detach.
                .andExpect(jsonPath("$.message", containsString(UNIVERSITY_CODE)));

        assertThat(deletedAt(id)).isNull();
    }

    @Test
    @DisplayName("a DELETED child still pins its parent's level — no resurrect-at-broken-depth")
    void deletedChildStillBlocksALevelChange() throws Exception {
        // R(1) -> A(2) -> B(3) -> C(4): the exact shape the review used to reproduce the defect.
        String rootId = createSpeciality("99900020", NAME_PREFIX + "Root", null, List.of(2026));
        String aId = createChild("99900021", NAME_PREFIX + "Alpha lvl2", rootId, 2);
        String bId = createChild("99900022", NAME_PREFIX + "Beta lvl3", aId, 3);
        String cId = createChild("99900023", NAME_PREFIX + "Gamma lvl4", bId, 4);

        mockMvc.perform(delete(BASE + "/{id}", cId).with(csrf())).andExpect(status().isNoContent());
        assertThat(deletedAt(cId)).isNotNull();

        // B now looks childless in every list (@SQLRestriction hides C), but C is restorable and keeps
        // hierarchy_level=4 — so moving B to level 2 would resurrect C at a depth that breaks
        // parent.level + 1, and SpecialityDistItemDto would ship that broken depth to the 224 OTMs.
        mockMvc.perform(put(BASE + "/{id}", bId).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(movePayload(NAME_PREFIX + "Beta lvl3", "99900022", 2, rootId)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.error").value("SPECIALITY_HAS_CHILDREN_MOVE_FIRST"));

        // Unchanged on the blocked path, and the way out is real: restore C, then B moves once C is
        // re-placed. Here we only assert the depth invariant held.
        assertThat(jdbc.queryForObject(
                "SELECT hierarchy_level FROM h_speciality WHERE id = ?::uuid", Integer.class, bId))
                .isEqualTo(3);

        mockMvc.perform(post(BASE + "/{id}/restore", cId).with(csrf())).andExpect(status().isOk());
        assertThat(jdbc.queryForObject(
                "SELECT hierarchy_level FROM h_speciality WHERE id = ?::uuid", Integer.class, cId))
                .isEqualTo(4);
    }

    // =====================================================
    // Helpers
    // =====================================================

    /** POST a child at an explicit depth — the create endpoint takes the same placement pair as PUT. */
    private String createChild(String code, String nameUz, String parentId, int level) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", code);
        body.put("nameUz", nameUz);
        body.put("educationType", EDU_TYPE);
        body.put("parentId", parentId);
        body.put("hierarchyLevel", level);
        body.put("years", List.of(2026));

        String json = mockMvc.perform(post(BASE).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(json).path("data").path("id").asText();
    }

    /** PUT body that only re-places a row (name/code unchanged, years required by the DTO). */
    private String movePayload(String nameUz, String code, int level, String parentId) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", code);
        body.put("nameUz", nameUz);
        body.put("educationType", EDU_TYPE);
        body.put("hierarchyLevel", level);
        body.put("parentId", parentId);
        body.put("years", List.of(2026));
        return objectMapper.writeValueAsString(body);
    }

    /** POST a new speciality and return its id. Born NEEDS_REVIEW — the only deletable status. */
    private String createSpeciality(String code, String nameUz, UUID parentId, List<Integer> years)
            throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", code);
        body.put("nameUz", nameUz);
        body.put("educationType", EDU_TYPE);
        body.put("parentId", parentId == null ? null : parentId.toString());
        body.put("years", years);

        String json = mockMvc.perform(post(BASE).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(json).path("data").path("id").asText();
    }

    /** The OTM bootstrap pull, as a raw body — searched for an id, not parsed. */
    private String distributionBody() throws Exception {
        return mockMvc.perform(get(DISTRIBUTION).param("educationType", EDU_TYPE))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    /**
     * Raw status flip. Promotion through {@code PUT /{id}} would also work, but it re-validates the
     * whole payload and re-places the row; here the status is the ONLY thing that may change.
     */
    private void setReviewStatus(String id, String status) {
        jdbc.update("UPDATE h_speciality SET review_status = ? WHERE id = ?::uuid", status, id);
    }

    private int countRows(String id) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM h_speciality WHERE id = ?::uuid", Integer.class, id);
        return n == null ? 0 : n;
    }

    /** The soft-delete stamp itself — {@code null} means the row is live. */
    private Timestamp deletedAt(String id) {
        return jdbc.queryForObject(
                "SELECT deleted_at FROM h_speciality WHERE id = ?::uuid", Timestamp.class, id);
    }

    private int yearCount(String id) {
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM h_speciality_year WHERE speciality_id = ?::uuid", Integer.class, id);
        return n == null ? 0 : n;
    }
}
