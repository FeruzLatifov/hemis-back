package uz.hemis.common.spring.converter;

import org.springframework.data.domain.Page;
import uz.hemis.common.dto.PageResponse;

import java.util.List;

/**
 * Page Response Converter - Spring Page to PageResponse conversion
 *
 * <p><strong>Clean Architecture:</strong></p>
 * <ul>
 *   <li>This class bridges Spring Data Page with framework-agnostic PageResponse</li>
 *   <li>Services use this converter instead of direct Spring dependency in common module</li>
 *   <li>PageResponse DTO remains framework-agnostic in common module</li>
 * </ul>
 *
 * <p><strong>Usage:</strong></p>
 * <pre>
 * // In service layer
 * Page&lt;StudentEntity&gt; page = studentRepository.findAll(pageable);
 * List&lt;StudentDto&gt; dtos = page.getContent().stream()
 *     .map(mapper::toDto)
 *     .toList();
 * PageResponse&lt;StudentDto&gt; response = PageResponseConverter.toPageResponse(page, dtos);
 * </pre>
 *
 * @since 2.0.0
 */
public final class PageResponseConverter {

    private PageResponseConverter() {
        // Utility class - no instantiation
    }

    /**
     * Convert Spring Data Page to PageResponse
     *
     * @param page Spring Data Page
     * @param <T> content type
     * @return PageResponse with pagination metadata
     */
    public static <T> PageResponse<T> toPageResponse(Page<T> page) {
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
     * Convert Spring Data Page to PageResponse with content transformation
     *
     * <p>Useful when converting entities to DTOs</p>
     *
     * @param page Spring Data Page (source)
     * @param content transformed content (DTOs)
     * @param <T> DTO type
     * @param <E> Entity type
     * @return PageResponse with pagination metadata
     */
    public static <T, E> PageResponse<T> toPageResponse(Page<E> page, List<T> content) {
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
}
