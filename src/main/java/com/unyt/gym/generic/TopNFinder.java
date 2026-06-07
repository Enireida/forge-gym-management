package com.unyt.gym.generic;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Generic helper that returns the top-N elements of a collection sorted by
 * a {@link Comparable} key.
 *
 * <p>Uses a <b>bounded type parameter</b> ({@code <T extends
 * Comparable<T>>}) — exactly the pattern the requirement document
 * encourages.</p>
 *
 * <p><b>Course requirement:</b> R2 Generics with bounded type parameters,
 * R3/R4 Stream + Lambda usage in {@link #topN}.</p>
 */
public final class TopNFinder {

    private TopNFinder() { /* utility class */ }

    /**
     * Sort the given collection in <i>descending</i> natural order and
     * return at most {@code n} elements.
     *
     * @param input collection of comparable elements
     * @param n     maximum number of elements to return
     * @param <T>   element type, must be comparable to itself
     * @return immutable list with up to {@code n} elements
     */
    public static <T extends Comparable<T>> List<T> topN(Collection<T> input, int n) {
        if (input == null || input.isEmpty() || n <= 0) {
            return List.of();
        }
        return input.stream()
                .sorted(Comparator.reverseOrder())
                .limit(n)
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Top-N variant that lets the caller supply a key extractor when the
     * elements themselves are not directly {@link Comparable}.
     */
    public static <T, K extends Comparable<K>> List<T> topNBy(Collection<T> input,
                                                              java.util.function.Function<T, K> keyExtractor,
                                                              int n) {
        if (input == null || input.isEmpty() || n <= 0) {
            return List.of();
        }
        return input.stream()
                .sorted(Comparator.comparing(keyExtractor).reversed())
                .limit(n)
                .collect(Collectors.toUnmodifiableList());
    }
}
