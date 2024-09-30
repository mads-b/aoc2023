package day21;

import com.google.common.io.Resources;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.Spliterators;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Main {
    private static It start;
    private static Set<Point> rocks;
    private static int width;
    private static int height;

    public static void main(String[] args) throws Exception {
        final char[][] input = Resources.readLines(Resources.getResource("day21.txt"), StandardCharsets.UTF_8)
                .stream()
                .map(String::toCharArray)
                .toArray(char[][]::new);
        width = input[0].length;
        height = input.length;
        start = loopAll(input).filter(it -> it.c == 'S').findFirst().orElseThrow();
        rocks = loopAll(input).filter(it -> it.c == '#').map(it -> new Point(it.x, it.y)).collect(Collectors.toSet());
        System.out.println("%d X %d".formatted(width, height));
        Set<Point> frontier = new HashSet<>();
        frontier.add(new Point(start.x, start.y));

        System.out.println("NMegMod: "  + Math.ceilMod(-130, 131));

        for (int i = 0; i < 327; i++) {
            // Need to control the steps so modify a BFS a little:
            final Set<Point> newFrontier = new HashSet<>();
            while(!frontier.isEmpty()) {
                Point it = frontier.iterator().next();
                frontier.remove(it);
                neighbors(it)
                        .filter(neigh -> !frontier.contains(neigh))
                        .forEach(newFrontier::add);
            }
            frontier.addAll(newFrontier);

            // Steps @ X*131+65.. For brute force solutions.
            if (i == 64 || i % 131 == 65) {
                System.out.println("I = " + i);
                System.out.println(frontier.size());
            }

            newFrontier.clear();
        }
        System.out.println("Discovered " + frontier.size());

        // Notes: After a certain number of steps, only counting the points in the origin square, it starts to oscillate between two constants.
        // It is very likely that this happens to all squares when the neighboring squares start to saturate.
        // 202300*131+65 = 26501365
        // The final number aligns with the time it takes to reach the border of the first square, and then the
        // time it takes to reach each new border.
        // At t=327 we have samples of all possible grid states. The only thing that will change from here on out are their counts
        // At t=327 the grid looks like this, with ascii symbols to show the different "kinds" of boundary states.
        // |...|../|.^.|\..|...|
        // |..-|.//|XXX|\\.|-..|
        // |..<|XXX|XXX|XXX|>..|
        // |..-|.\\|XXX|//.|-..|
        // |...|..\|.v.|/..|...|
        // In total there are 14 distinct exterior boundary states and the one interior filled one which has count 7427.
        // What remains then is counting the steps in each of the boundary conditions and figuring out how many of each of them there are at step 26501365
        // Width at that point (for i = x*131+65): 0->1 1->3 2->5 it follows width = 2x+1 which means width at 26501365 is 404601 (blocks)
        // As for the rest of the blocks, there will obviously only be one of each arrow-shaped block in each point of the diamond.
        // Looking at the upper left quadrant, the amount of blocks that have their bottom left filled are: (width-1)/2
        // In the same quadrant, the amount of blocks that have their top right unfilled are (width-1)/2-1
        // The number of filled quadrants are: 0 -> 0, 1 -> 1, 2 -> 6, 3 -> 13 (x^2+(x-1)^2)
        // The same numbers apply to the other quadrants.
        // Test numbers found by brute force to validate assumptions: Got 3922, 36220, 101824, 200604 for X=1, 2, 3, 4 for tests
        // Try our test battery:
        System.out.println("Sum for known values (3 (196)): " + estimateStepCountForX(frontier, 1));
        System.out.println("Sum for known values (5 (327)): " + estimateStepCountForX(frontier, 2));
        // TOO LOW: 7333801764991
        // Even er: 608194471781248
        // not      608193770812184
        System.out.println("SUM IS: " + estimateStepCountForX(frontier, 202300));
        // ^^ Never got this estimator to yield exact results. Something is off by one. However, looking at how it computes, I see I could've just done regression on three known points instead:
        long num = 202300;
        System.out.println(14861*num*num + 14994*num + 3791);

        /*for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input[i].length; j++) {
                final Point p = new Point(j, i);
                if (frontier.contains(p)) System.out.print('O');
                else System.out.print(rocks.contains(p.normalized()) ? '#' : '.');
            }
            System.out.println();
        }*/
    }

    public static long estimateStepCountForX(final Set<Point> frontier, final long x) {
        long filled = countSamplesInQuadrant(frontier, 0, 0);
        long filled2 = countSamplesInQuadrant(frontier, 1, 0);
        long tArr = countSamplesInQuadrant(frontier, 0, -2);
        long bArr = countSamplesInQuadrant(frontier, 0, 2);
        long lArr = countSamplesInQuadrant(frontier, -2, 0);
        long rArr = countSamplesInQuadrant(frontier, 2, 0);

        long trSmallDiag = countSamplesInQuadrant(frontier, 1, -2);
        long trBigDiag = countSamplesInQuadrant(frontier, 1, -1);

        long brSmallDiag = countSamplesInQuadrant(frontier, 1, 2);
        long brBigDiag = countSamplesInQuadrant(frontier, 1, 1);

        long tlSmallDiag = countSamplesInQuadrant(frontier, -1, -2);
        long tlBigDiag = countSamplesInQuadrant(frontier, -1, -1);

        long blSmallDiag = countSamplesInQuadrant(frontier, -1, 2);
        long blBigDiag = countSamplesInQuadrant(frontier, -1, 1);

        // The big reveal! Will adding these together give us our magic number?
        return (tArr + bArr + lArr + rArr)
                + (trSmallDiag + brSmallDiag + tlSmallDiag + blSmallDiag) * x
                + (trBigDiag + brBigDiag + tlBigDiag + blBigDiag) * (x-1)
                + x*x * filled
                + (x-1)*(x-1)*filled2;
    }

    public static Stream<Point> neighbors(final Point it) {
        return Stream.of(
                        new Point(it.x - 1, it.y),
                        new Point(it.x + 1, it.y),
                        new Point(it.x, it.y - 1),
                        new Point(it.x, it.y + 1))
                .filter(p -> !rocks.contains(p.normalized()));
    }

    private record It(int x, int y, char c) {}
    private record Point(int x, int y) {
        public Point normalized() {
            int nx = x % width;
            int ny = y % height;
            return new Point(
                    nx < 0 ? nx+width : nx,
                    ny < 0 ? ny+height : ny);
        }
    }

    private static long countSamplesInQuadrant(final Set<Point> frontier, final int quadX, final int quadY) {
        int minX = quadX * width;
        int minY = quadY * height;
        int maxX = (quadX+1)*width;
        int maxY = (quadY+1)*height;
        return frontier.stream().filter(p -> p.x >= minX && p.x < maxX && p.y >= minY && p.y < maxY).count();
    }

    private static Stream<It> loopAll(char[][] input) {
        return IntStream.range(0, input.length).mapToObj(y -> IntStream.range(0, input[y].length).mapToObj(x -> new It(x, y, input[y][x]))).flatMap(s -> s);
    }
}