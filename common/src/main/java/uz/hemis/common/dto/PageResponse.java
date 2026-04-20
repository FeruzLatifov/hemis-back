package uz.hemis.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.io.Serializable;
import java.util.List;

/**
 * Page Response DTO — Spring Data {@code Page} serialization uchun stabil wrapper.
 *
 * <p><strong>Maqsad:</strong> Spring 3.3+ ogohlantirishini hal qilish
 * ({@code Serializing PageImpl instances as-is is not supported}) va shu bilan birga
 * {@code PageImpl} default JSON formatini <b>100% backward-compatible</b> saqlash.</p>
 *
 * <p><strong>Backward compat:</strong> Barcha {@code PageImpl} JSON fieldlari saqlanadi —
 * {@code content}, {@code pageable}, {@code totalElements}, {@code totalPages},
 * {@code last}, {@code first}, {@code numberOfElements}, {@code size}, {@code number},
 * {@code sort}, {@code empty}. Frontend/client kodi <b>zero o'zgartirish</b> bilan ishlaydi.</p>
 *
 * <p><strong>Foydalanish:</strong></p>
 * <pre>{@code
 * @GetMapping
 * public PageResponse<UserDto> list(Pageable pageable) {
 *     return PageResponse.of(service.findAll(pageable));
 * }
 * }</pre>
 *
 * @param <T> content type
 * @since 2.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResponse<T> implements Serializable {

    private static final long serialVersionUID = 2L;

    @JsonProperty("content")
    private List<T> content;

    /** Spring {@code PageImpl.getNumber()} bilan mos — joriy sahifa (0-indexed). */
    @JsonProperty("number")
    private Integer number;

    @JsonProperty("size")
    private Integer size;

    @JsonProperty("totalElements")
    private Long totalElements;

    @JsonProperty("totalPages")
    private Integer totalPages;

    @JsonProperty("numberOfElements")
    private Integer numberOfElements;

    @JsonProperty("first")
    private Boolean first;

    @JsonProperty("last")
    private Boolean last;

    @JsonProperty("empty")
    private Boolean empty;

    @JsonProperty("pageable")
    private PageableInfo pageable;

    @JsonProperty("sort")
    private SortInfo sort;

    // ---------------------------------------------------------------------
    // Legacy backward-compat aliases (eski PageResponse format)
    // ---------------------------------------------------------------------
    // hemis-front kabi mavjud iste'molchilar `page`, `hasNext`, `hasPrevious`
    // field'larini ishlatishi mumkin (audit.api.ts dagi PagedResponse interface).
    // PageImpl shape (number, pageable, sort) bilan yonma-yon saqlanadi —
    // additive, breaking emas. ANY consumer ishlaydi.

    /** Alias: {@link #number} bilan bir xil — eski PageResponse format uchun. */
    @JsonProperty("page")
    private Integer page;

    @JsonProperty("hasNext")
    private Boolean hasNext;

    @JsonProperty("hasPrevious")
    private Boolean hasPrevious;

    // ---------------------------------------------------------------------
    // Factory methods
    // ---------------------------------------------------------------------

    public static <T> PageResponse<T> of(Page<T> springPage) {
        return PageResponse.<T>builder()
                .content(springPage.getContent())
                .number(springPage.getNumber())
                .size(springPage.getSize())
                .totalElements(springPage.getTotalElements())
                .totalPages(springPage.getTotalPages())
                .numberOfElements(springPage.getNumberOfElements())
                .first(springPage.isFirst())
                .last(springPage.isLast())
                .empty(springPage.isEmpty())
                .pageable(PageableInfo.of(springPage))
                .sort(SortInfo.of(springPage.getSort()))
                .page(springPage.getNumber())
                .hasNext(springPage.hasNext())
                .hasPrevious(springPage.hasPrevious())
                .build();
    }

    /** Transformed content (mappers) bilan factory. */
    public static <T, E> PageResponse<T> of(Page<E> springPage, List<T> content) {
        return PageResponse.<T>builder()
                .content(content)
                .number(springPage.getNumber())
                .size(springPage.getSize())
                .totalElements(springPage.getTotalElements())
                .totalPages(springPage.getTotalPages())
                .numberOfElements(content.size())
                .first(springPage.isFirst())
                .last(springPage.isLast())
                .empty(content.isEmpty())
                .pageable(PageableInfo.of(springPage))
                .sort(SortInfo.of(springPage.getSort()))
                .page(springPage.getNumber())
                .hasNext(springPage.hasNext())
                .hasPrevious(springPage.hasPrevious())
                .build();
    }

    // ---------------------------------------------------------------------
    // Nested: Pageable info (PageImpl.pageable JSON shape bilan mos)
    // ---------------------------------------------------------------------

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageableInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        @JsonProperty("pageNumber")
        private Integer pageNumber;

        @JsonProperty("pageSize")
        private Integer pageSize;

        @JsonProperty("sort")
        private SortInfo sort;

        @JsonProperty("offset")
        private Long offset;

        @JsonProperty("paged")
        private Boolean paged;

        @JsonProperty("unpaged")
        private Boolean unpaged;

        public static PageableInfo of(Page<?> page) {
            var pageable = page.getPageable();
            return PageableInfo.builder()
                    .pageNumber(page.getNumber())
                    .pageSize(page.getSize())
                    .sort(SortInfo.of(page.getSort()))
                    .offset(pageable.isPaged() ? pageable.getOffset() : 0L)
                    .paged(pageable.isPaged())
                    .unpaged(pageable.isUnpaged())
                    .build();
        }
    }

    // ---------------------------------------------------------------------
    // Nested: Sort info (PageImpl.sort JSON shape bilan mos)
    // ---------------------------------------------------------------------

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SortInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        @JsonProperty("empty")
        private Boolean empty;

        @JsonProperty("sorted")
        private Boolean sorted;

        @JsonProperty("unsorted")
        private Boolean unsorted;

        public static SortInfo of(Sort sort) {
            return SortInfo.builder()
                    .empty(sort.isEmpty())
                    .sorted(sort.isSorted())
                    .unsorted(sort.isUnsorted())
                    .build();
        }
    }
}
