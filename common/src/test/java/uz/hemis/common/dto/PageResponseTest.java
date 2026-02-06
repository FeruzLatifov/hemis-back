package uz.hemis.common.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PageResponse")
class PageResponseTest {

    // =====================================================
    // of(Page) Factory Method
    // =====================================================

    @Nested
    @DisplayName("of(Page) factory method")
    class OfPageFactory {

        @Test
        @DisplayName("of(page) sets all pagination fields correctly")
        void of_page_setsAllPaginationFields() {
            List<String> content = List.of("alpha", "beta", "gamma");
            // page 0, size 3, total 10 elements => 4 total pages
            Page<String> springPage = new PageImpl<>(content, PageRequest.of(0, 3), 10);

            PageResponse<String> result = PageResponse.of(springPage);

            assertThat(result.getContent()).containsExactly("alpha", "beta", "gamma");
            assertThat(result.getPage()).isEqualTo(0);
            assertThat(result.getSize()).isEqualTo(3);
            assertThat(result.getTotalElements()).isEqualTo(10L);
            assertThat(result.getTotalPages()).isEqualTo(4);
            assertThat(result.getFirst()).isTrue();
            assertThat(result.getLast()).isFalse();
            assertThat(result.getHasNext()).isTrue();
            assertThat(result.getHasPrevious()).isFalse();
            assertThat(result.getNumberOfElements()).isEqualTo(3);
            assertThat(result.getEmpty()).isFalse();
        }

        @Test
        @DisplayName("of(page) with empty page sets zero values")
        void of_emptyPage_setsZeroValues() {
            Page<String> emptyPage = new PageImpl<>(
                    Collections.emptyList(), PageRequest.of(0, 10), 0
            );

            PageResponse<String> result = PageResponse.of(emptyPage);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getPage()).isEqualTo(0);
            assertThat(result.getSize()).isEqualTo(10);
            assertThat(result.getTotalElements()).isEqualTo(0L);
            assertThat(result.getTotalPages()).isEqualTo(0);
            assertThat(result.getFirst()).isTrue();
            assertThat(result.getLast()).isTrue();
            assertThat(result.getHasNext()).isFalse();
            assertThat(result.getHasPrevious()).isFalse();
            assertThat(result.getNumberOfElements()).isEqualTo(0);
            assertThat(result.getEmpty()).isTrue();
        }
    }

    // =====================================================
    // of(Page, List) Factory Method with Transformed Content
    // =====================================================

    @Nested
    @DisplayName("of(Page, List) factory method with transformed content")
    class OfPageWithTransformedContent {

        @Test
        @DisplayName("of(page, transformedContent) uses the new content list")
        void of_pageWithTransformedContent_usesNewContent() {
            // Original page holds Integer entities
            List<Integer> entities = List.of(1, 2, 3);
            Page<Integer> springPage = new PageImpl<>(entities, PageRequest.of(1, 3), 9);

            // Transform to String DTOs
            List<String> dtos = List.of("one", "two", "three");

            PageResponse<String> result = PageResponse.of(springPage, dtos);

            // Content should be the transformed list, not the original
            assertThat(result.getContent()).containsExactly("one", "two", "three");
            // Pagination metadata comes from the original page
            assertThat(result.getPage()).isEqualTo(1);
            assertThat(result.getSize()).isEqualTo(3);
            assertThat(result.getTotalElements()).isEqualTo(9L);
            assertThat(result.getTotalPages()).isEqualTo(3);
            // numberOfElements and empty come from the transformed content list
            assertThat(result.getNumberOfElements()).isEqualTo(3);
            assertThat(result.getEmpty()).isFalse();
        }
    }

    // =====================================================
    // Helper Methods
    // =====================================================

    @Nested
    @DisplayName("helper methods")
    class HelperMethods {

        @Test
        @DisplayName("isSinglePage returns true when page is both first and last")
        void isSinglePage_onePage_returnsTrue() {
            // Single page: 2 items, size 10, total 2 => 1 page
            Page<String> singlePage = new PageImpl<>(
                    List.of("a", "b"), PageRequest.of(0, 10), 2
            );

            PageResponse<String> result = PageResponse.of(singlePage);

            assertThat(result.isSinglePage()).isTrue();
        }

        @Test
        @DisplayName("isSinglePage returns false when there are multiple pages")
        void isSinglePage_multiplePages_returnsFalse() {
            // Multiple pages: 3 items on page 0, size 3, total 7 => 3 pages
            Page<String> multiPage = new PageImpl<>(
                    List.of("a", "b", "c"), PageRequest.of(0, 3), 7
            );

            PageResponse<String> result = PageResponse.of(multiPage);

            assertThat(result.isSinglePage()).isFalse();
        }

        @Test
        @DisplayName("getPageNumber returns 1-indexed page number")
        void getPageNumber_returnsOneIndexed() {
            // Page index 0 in Spring Data should become page number 1
            Page<String> page = new PageImpl<>(
                    List.of("x"), PageRequest.of(0, 5), 1
            );

            PageResponse<String> result = PageResponse.of(page);

            assertThat(result.getPage()).isEqualTo(0);
            assertThat(result.getPageNumber()).isEqualTo(1);
        }
    }
}
