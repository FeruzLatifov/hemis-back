package uz.hemis.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.List;

/**
 * Page Response DTO - Pagination metadata
 *
 * <p><strong>Purpose:</strong> Wrap paginated results with metadata</p>
 *
 * <p><strong>Spring Data Page Wrapper:</strong></p>
 * <ul>
 *   <li>Wraps Spring Data Page&lt;T&gt; with consistent JSON structure</li>
 *   <li>Provides pagination metadata (page number, size, total, etc.)</li>
 * </ul>
 *
 * @param <T> content type
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Page content (list of items)
     * JSON field: "content"
     */
    @JsonProperty("content")
    private List<T> content;

    /**
     * Current page number (0-indexed)
     * JSON field: "page"
     */
    @JsonProperty("page")
    private Integer page;

    /**
     * Page size (items per page)
     * JSON field: "size"
     */
    @JsonProperty("size")
    private Integer size;

    /**
     * Total number of elements across all pages
     * JSON field: "totalElements"
     */
    @JsonProperty("totalElements")
    private Long totalElements;

    /**
     * Total number of pages
     * JSON field: "totalPages"
     */
    @JsonProperty("totalPages")
    private Integer totalPages;

    /**
     * Is first page
     * JSON field: "first"
     */
    @JsonProperty("first")
    private Boolean first;

    /**
     * Is last page
     * JSON field: "last"
     */
    @JsonProperty("last")
    private Boolean last;

    /**
     * Has next page
     * JSON field: "hasNext"
     */
    @JsonProperty("hasNext")
    private Boolean hasNext;

    /**
     * Has previous page
     * JSON field: "hasPrevious"
     */
    @JsonProperty("hasPrevious")
    private Boolean hasPrevious;

    /**
     * Number of elements in current page
     * JSON field: "numberOfElements"
     */
    @JsonProperty("numberOfElements")
    private Integer numberOfElements;

    /**
     * Is empty page
     * JSON field: "empty"
     */
    @JsonProperty("empty")
    private Boolean empty;

    // =====================================================
    // Factory Method - From Spring Data Page
    // =====================================================

    /**
     * Create PageResponse from Spring Data Page
     *
     * <p>Converts Spring Data Page&lt;T&gt; to our PageResponse&lt;T&gt;</p>
     *
     * @param page Spring Data Page
     * @param <T> content type
     * @return PageResponse
     */
    public static <T> PageResponse<T> of(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .numberOfElements(page.getNumberOfElements())
                .empty(page.isEmpty())
                .build();
    }

    /**
     * Create PageResponse from Page with content transformation
     *
     * <p>Useful when converting entities to DTOs</p>
     *
     * @param page Spring Data Page
     * @param content transformed content
     * @param <T> DTO type
     * @param <E> Entity type
     * @return PageResponse
     */
    public static <T, E> PageResponse<T> of(Page<E> page, List<T> content) {
        return PageResponse.<T>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .numberOfElements(content.size())
                .empty(content.isEmpty())
                .build();
    }

    // =====================================================
    // Helper Methods
    // =====================================================

    /**
     * Check if page is first and last (single page)
     *
     * @return true if single page
     */
    public boolean isSinglePage() {
        return Boolean.TRUE.equals(first) && Boolean.TRUE.equals(last);
    }

    /**
     * Get 1-indexed page number (for display)
     *
     * @return page number (1-indexed)
     */
    public int getPageNumber() {
        return page != null ? page + 1 : 1;
    }
}
