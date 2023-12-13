package day12;

import com.google.common.io.Resources;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {

    private static boolean part2 = true;

    public static void main(String[] args) throws Exception {
        final List<InLine> in = Resources.readLines(Resources.getResource("day12.txt"), StandardCharsets.UTF_8).stream().map(InLine::parse).toList();

        long sum = 0;
        for (InLine inLine : in) {
            final Map<Coord, Long> cache = new HashMap<>();
            System.out.println("Line: " + new String(inLine.template));
            sum += countPermutations(cache, inLine, 0, 0);
        }
        System.out.println("Sum: " + sum);
    }

    private record Coord(int curChar, int curItem) {}

    private static long countPermutations(final Map<Coord, Long> cache, final InLine inLine, int curChar, int curItem) {
        if (curChar >= inLine.template.length) {
            return 0;
        }
        final Coord coord = new Coord(curChar, curItem);
        if (cache.containsKey(coord)) {
            return cache.get(coord);
        }

        final long count = switch (inLine.template[curChar]) {
            case '.' -> countPermutations(cache, inLine,curChar + 1, curItem);
            case '#' -> countPermutationsConsumeFrom(cache, inLine, curChar, curItem);
            case '?' -> countPermutationsConsumeFrom(cache, inLine, curChar, curItem)
                        + countPermutations(cache, inLine,curChar + 1, curItem);
            default -> throw new IllegalStateException("Illegal char: " + inLine.template[curChar]);
        };
        cache.put(coord, count);
        return count;
    }

    private static long countPermutationsConsumeFrom(final Map<Coord, Long> cache, final InLine inLine, int curChar, int curItem) {
        int curItemLength = inLine.pattern[curItem];

        if (curChar != 0 && inLine.template[curChar-1] == '#') {
            return 0; // No item can be preceeded by #
        }

        if (curChar + curItemLength > inLine.template.length) { // We ran out of string before applying the item..
            return 0;
        }

        for (int x = curChar; x < curChar + curItemLength; x++) { // Apply item
            if (inLine.template[x] == '.') {
                return 0; // Infeasible branch
            }
        }
        curChar += curItemLength;
        if (curChar != inLine.template.length && inLine.template[curChar] == '#') {
            return 0; // No item can be succeeded by #
        }

        boolean isLastItem = curItem+1 == inLine.pattern.length;
        // Check if we are done
        if (isLastItem) {
            // No items left to fit. Check if there are no more #s:
            for (int c = curChar; c < inLine.template.length; c++) {
                if (inLine.template[c] == '#') {
                    return 0; // Infeasible path, all hashes not consumed.
                }
            }
            return 1;
        }

        return countPermutations(cache, inLine, curChar+1, curItem+1);
    }

    private record InLine(char[] template, int[] pattern) {

        public static InLine parse(final String str) {
            final String[] split = str.split(" ");
            final int[] pattern = Arrays.stream(split[1].split(",")).mapToInt(Integer::parseInt).toArray();
            if (part2) {
                final char[] longcharArr = IntStream.range(0, 5)
                        .mapToObj(i -> split[0])
                        .collect(Collectors.joining("?"))
                        .toCharArray();
                final int[] longPat = Arrays.stream(IntStream.range(0, 5)
                        .mapToObj(i -> split[1])
                        .collect(Collectors.joining(","))
                        .split(","))
                        .mapToInt(Integer::parseInt)
                        .toArray();
                return new InLine(longcharArr, longPat);
            }
            return new InLine(split[0].toCharArray(), pattern);
        }
    }
}