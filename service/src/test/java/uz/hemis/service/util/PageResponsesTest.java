package uz.hemis.service.util;

import uz.hemis.common.dto.PageResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PageResponses adapter")
class PageResponsesTest {

    @Nested
    @DisplayName("from(Page) factory")
    class FromPageFactory {

        @Test
        @DisplayName("all pagination fields populated — PageImpl bilan mos")
        void setsAllFields() {
            List<String> content = List.of("alpha", "beta", "gamma");
            Page<String> springPage = new PageImpl<>(content, PageRequest.of(0, 3), 10);

            PageResponse<String> r = PageResponses.from(springPage);

            assertThat(r.getContent()).containsExactly("alpha", "beta", "gamma");
            assertThat(r.getNumber()).isEqualTo(0);
            assertThat(r.getSize()).isEqualTo(3);
            assertThat(r.getTotalElements()).isEqualTo(10L);
            assertThat(r.getTotalPages()).isEqualTo(4);
            assertThat(r.getNumberOfElements()).isEqualTo(3);
            assertThat(r.getFirst()).isTrue();
            assertThat(r.getLast()).isFalse();
            assertThat(r.getEmpty()).isFalse();
            // Legacy aliaslar (eski PageResponse format)
            assertThat(r.getPage()).isEqualTo(0);
            assertThat(r.getHasNext()).isTrue();
            assertThat(r.getHasPrevious()).isFalse();
            // Nested pageable/sort PageImpl JSON shape bilan mos
            assertThat(r.getPageable()).isNotNull();
            assertThat(r.getPageable().getPageNumber()).isEqualTo(0);
            assertThat(r.getPageable().getPageSize()).isEqualTo(3);
            assertThat(r.getPageable().getPaged()).isTrue();
            assertThat(r.getSort()).isNotNull();
            assertThat(r.getSort().getEmpty()).isTrue();
        }

        @Test
        @DisplayName("empty page — barcha zero/bo'sh qiymatlar")
        void emptyPage() {
            Page<String> emptyPage = new PageImpl<>(
                    Collections.emptyList(), PageRequest.of(0, 10), 0);

            PageResponse<String> r = PageResponses.from(emptyPage);

            assertThat(r.getContent()).isEmpty();
            assertThat(r.getNumber()).isEqualTo(0);
            assertThat(r.getTotalElements()).isEqualTo(0L);
            assertThat(r.getTotalPages()).isEqualTo(0);
            assertThat(r.getFirst()).isTrue();
            assertThat(r.getLast()).isTrue();
            assertThat(r.getEmpty()).isTrue();
        }

        @Test
        @DisplayName("sorted page — sort fieldlar to'g'ri")
        void sortedPage() {
            Page<String> page = new PageImpl<>(
                    List.of("a"), PageRequest.of(0, 10, Sort.by("name")), 1);

            PageResponse<String> r = PageResponses.from(page);

            assertThat(r.getSort().getSorted()).isTrue();
            assertThat(r.getSort().getUnsorted()).isFalse();
            assertThat(r.getPageable().getSort().getSorted()).isTrue();
        }
    }

    @Nested
    @DisplayName("from(Page, transformedContent) — mapper uchun")
    class FromPageWithTransformed {

        @Test
        void usesTransformedContent() {
            Page<Integer> springPage = new PageImpl<>(
                    List.of(1, 2, 3), PageRequest.of(1, 3), 9);
            List<String> dtos = List.of("one", "two", "three");

            PageResponse<String> r = PageResponses.from(springPage, dtos);

            assertThat(r.getContent()).containsExactly("one", "two", "three");
            assertThat(r.getNumber()).isEqualTo(1);
            assertThat(r.getTotalElements()).isEqualTo(9L);
        }
    }

    @Nested
    @DisplayName("JSON shape — PageImpl drop-in compatibility")
    class JsonShape {

        @Test
        @DisplayName("JSON da barcha PageImpl fieldlari mavjud (backward compat)")
        void jsonShapeMatchesPageImpl() throws Exception {
            Page<String> springPage = new PageImpl<>(
                    List.of("x"), PageRequest.of(0, 5, Sort.by("id")), 1);
            PageResponse<String> r = PageResponses.from(springPage);

            String json = new ObjectMapper().writeValueAsString(r);

            // Spring PageImpl default JSON fieldlari
            assertThat(json).contains("\"content\"");
            assertThat(json).contains("\"number\":0");          // ← CRITICAL: `page` emas, `number`
            assertThat(json).contains("\"size\":5");
            assertThat(json).contains("\"totalElements\":1");
            assertThat(json).contains("\"totalPages\":1");
            assertThat(json).contains("\"numberOfElements\":1");
            assertThat(json).contains("\"first\":true");
            assertThat(json).contains("\"last\":true");
            assertThat(json).contains("\"empty\":false");
            assertThat(json).contains("\"pageable\"");
            assertThat(json).contains("\"pageNumber\":0");
            assertThat(json).contains("\"pageSize\":5");
            assertThat(json).contains("\"paged\":true");
            assertThat(json).contains("\"unpaged\":false");
            assertThat(json).contains("\"sort\"");
            assertThat(json).contains("\"sorted\":true");
            // Legacy aliaslar — eski PageResponse format consumer'lari uchun
            assertThat(json).contains("\"page\":0");
            assertThat(json).contains("\"hasNext\":false");
            assertThat(json).contains("\"hasPrevious\":false");
        }
    }
}
