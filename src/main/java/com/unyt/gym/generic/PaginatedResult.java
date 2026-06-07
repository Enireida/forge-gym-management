package com.unyt.gym.generic;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Generic, immutable container that wraps a paginated result set together
 * with the metadata needed by the UI to render pagination controls.
 *
 * <p>This class is parameterised on the element type {@code T}; the class
 * also exposes a generic {@link #map(Function)} method that converts the
 * page in-place into a {@code PaginatedResult<R>}. This is the kind of
 * type-safe utility the requirement R2 asks for.</p>
 *
 * <p><b>Course requirement:</b> R2 Generics — generic class with a generic
 * method ({@code <R> map}).</p>
 *
 * @param <T> element type of the page
 */
public final class PaginatedResult<T> {

    private final List<T> items;
    private final long totalElements;
    private final int page;
    private final int size;

    public PaginatedResult(List<T> items, long totalElements, int page, int size) {
        this.items = items == null ? List.of() : List.copyOf(items);
        this.totalElements = totalElements;
        this.page = page;
        this.size = size;
    }

    public static <T> PaginatedResult<T> empty() {
        return new PaginatedResult<>(Collections.emptyList(), 0, 0, 0);
    }

    public static <T> PaginatedResult<T> of(List<T> items, int page, int size) {
        return new PaginatedResult<>(items, items.size(), page, size);
    }

    /**
     * Generic map operation. Demonstrates a method-level type parameter
     * separate from the class-level {@code T}.
     *
     * @param mapper conversion function applied to every element
     * @param <R>    type of the resulting page
     * @return a new {@code PaginatedResult<R>} preserving page metadata
     */
    public <R> PaginatedResult<R> map(Function<T, R> mapper) {
        List<R> mapped = items.stream().map(mapper).collect(Collectors.toList());
        return new PaginatedResult<>(mapped, totalElements, page, size);
    }

    public int getTotalPages() {
        return size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
    }

    public boolean hasNext() {
        return (page + 1) * (long) size < totalElements;
    }

    public boolean hasPrevious() {
        return page > 0;
    }

    public List<T> getItems() { return items; }
    public long getTotalElements() { return totalElements; }
    public int getPage() { return page; }
    public int getSize() { return size; }
}
