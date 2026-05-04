package uz.hemis.service.util;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import uz.hemis.common.dto.PageResponse;

import java.util.List;

/**
 * Spring-coupled adapter from {@link Page} → {@link PageResponse}.
 *
 * <p>Lives in the {@code service} module so {@code common} stays Spring-free
 * (per common/CLAUDE.md). Callers (controllers, services) replace
 * {@code PageResponse.of(page)} with {@code PageResponses.from(page)}.</p>
 *
 * @since 2.1.0
 */
public final class PageResponses {

    private PageResponses() {
    }

    /**
     * Build a PageImpl-shaped {@link PageResponse} from a Spring {@link Page}.
     *
     * @param springPage source Spring page
     * @param <T>        content type
     * @return ordered {@link PageResponse} matching PageImpl JSON shape with legacy aliases
     */
    public static <T> PageResponse<T> from(Page<T> springPage) {
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
                .pageable(toPageable(springPage))
                .sort(toSort(springPage.getSort()))
                .page(springPage.getNumber())
                .hasNext(springPage.hasNext())
                .hasPrevious(springPage.hasPrevious())
                .build();
    }

    /**
     * Build a PageImpl-shaped {@link PageResponse} with mapped content (e.g. entity → DTO).
     *
     * @param springPage source page (e.g. of entities)
     * @param content    transformed content (e.g. DTOs)
     * @param <T>        target content type
     * @param <E>        source content type
     */
    public static <T, E> PageResponse<T> from(Page<E> springPage, List<T> content) {
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
                .pageable(toPageable(springPage))
                .sort(toSort(springPage.getSort()))
                .page(springPage.getNumber())
                .hasNext(springPage.hasNext())
                .hasPrevious(springPage.hasPrevious())
                .build();
    }

    private static PageResponse.PageableInfo toPageable(Page<?> page) {
        var pageable = page.getPageable();
        return PageResponse.PageableInfo.builder()
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .sort(toSort(page.getSort()))
                .offset(pageable.isPaged() ? pageable.getOffset() : 0L)
                .paged(pageable.isPaged())
                .unpaged(pageable.isUnpaged())
                .build();
    }

    private static PageResponse.SortInfo toSort(Sort sort) {
        return PageResponse.SortInfo.builder()
                .empty(sort.isEmpty())
                .sorted(sort.isSorted())
                .unsorted(sort.isUnsorted())
                .build();
    }
}
