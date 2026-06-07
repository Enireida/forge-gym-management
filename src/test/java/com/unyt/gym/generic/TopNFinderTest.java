package com.unyt.gym.generic;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the generic {@link TopNFinder} utility.
 *
 * <p>Exercises R2 (Generics with bounded type parameters),
 * R3 (lambdas / functional programming via key extractors), and
 * R4 (Stream API correctness).</p>
 */
class TopNFinderTest {

    @Test
    void topN_returnsDescendingOrder() {
        List<Integer> result = TopNFinder.topN(List.of(5, 1, 9, 3, 7), 3);
        assertEquals(List.of(9, 7, 5), result);
    }

    @Test
    void topN_handlesFewerElementsThanN() {
        List<Integer> result = TopNFinder.topN(List.of(2, 4), 5);
        assertEquals(List.of(4, 2), result);
    }

    @Test
    void topN_emptyInputReturnsEmpty() {
        assertTrue(TopNFinder.topN(List.<String>of(), 3).isEmpty());
    }

    @Test
    void topN_zeroOrNegativeNReturnsEmpty() {
        assertTrue(TopNFinder.topN(List.of(1, 2, 3), 0).isEmpty());
        assertTrue(TopNFinder.topN(List.of(1, 2, 3), -1).isEmpty());
    }

    @Test
    void topN_nullInputReturnsEmpty() {
        assertTrue(TopNFinder.topN(null, 3).isEmpty());
    }

    @Test
    void topN_returnedListIsImmutable() {
        List<Integer> result = TopNFinder.topN(List.of(1, 2, 3), 2);
        assertThrows(UnsupportedOperationException.class, () -> result.add(99));
    }

    @Test
    void topNBy_sortsUsingKeyExtractor() {
        record Athlete(String name, int score) { }

        List<Athlete> ranked = TopNFinder.topNBy(
                List.of(
                        new Athlete("Anila", 70),
                        new Athlete("Driton", 95),
                        new Athlete("Elira", 82)),
                Athlete::score,
                2);

        assertEquals(2, ranked.size());
        assertEquals("Driton", ranked.get(0).name());
        assertEquals("Elira",  ranked.get(1).name());
    }

    @Test
    void topNBy_emptyInputReturnsEmpty() {
        record Athlete(String name, int score) { }
        assertTrue(TopNFinder.topNBy(List.<Athlete>of(), Athlete::score, 3).isEmpty());
    }
}
