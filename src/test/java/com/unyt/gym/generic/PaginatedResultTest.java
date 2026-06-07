package com.unyt.gym.generic;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the generic {@link PaginatedResult} container.
 *
 * <p>Verifies R2 (Generics — class-level and method-level type
 * parameters) and that the page-metadata maths is correct.</p>
 */
class PaginatedResultTest {

    @Test
    void empty_returnsZeroValues() {
        PaginatedResult<String> empty = PaginatedResult.empty();
        assertTrue(empty.getItems().isEmpty());
        assertEquals(0, empty.getTotalElements());
        assertEquals(0, empty.getTotalPages());
        assertFalse(empty.hasNext());
        assertFalse(empty.hasPrevious());
    }

    @Test
    void totalPages_computedCorrectly() {
        PaginatedResult<Integer> r = new PaginatedResult<>(List.of(1, 2, 3), 25, 0, 10);
        assertEquals(3, r.getTotalPages());     // ceil(25/10)
        assertTrue(r.hasNext());
        assertFalse(r.hasPrevious());
    }

    @Test
    void hasNext_lastPage_isFalse() {
        PaginatedResult<Integer> r = new PaginatedResult<>(List.of(21, 22, 23, 24, 25), 25, 2, 10);
        assertFalse(r.hasNext());
        assertTrue(r.hasPrevious());
    }

    @Test
    void map_convertsElementsAndPreservesMetadata() {
        PaginatedResult<Integer> ints = new PaginatedResult<>(List.of(1, 2, 3), 30, 1, 10);

        PaginatedResult<String> strs = ints.map(i -> "n=" + i);

        assertEquals(List.of("n=1", "n=2", "n=3"), strs.getItems());
        assertEquals(30, strs.getTotalElements());
        assertEquals(1,  strs.getPage());
        assertEquals(10, strs.getSize());
        assertEquals(3,  strs.getTotalPages());
    }

    @Test
    void constructor_storesDefensiveCopy() {
        java.util.List<Integer> mutable = new java.util.ArrayList<>(List.of(1, 2));
        PaginatedResult<Integer> r = new PaginatedResult<>(mutable, 2, 0, 10);

        mutable.add(99);   // mutate original
        assertEquals(2, r.getItems().size());   // page is unaffected
    }
}
