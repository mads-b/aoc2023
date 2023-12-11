package day11;

import com.google.common.collect.Sets;
import com.google.common.io.Resources;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class Main {

    public static void main(String[] args) throws Exception {
        final List<String> in = Resources.readLines(Resources.getResource("day11.txt"), StandardCharsets.UTF_8);
        final Set<Point> points = new HashSet<>();
        for (int y = 0; y < in.size(); y++) {
            final String row = in.get(y);
            for (int x = 0; x < row.length(); x++) {
                if (row.charAt(x) == '#') {
                    points.add(new Point(x, y));
                }
            }
        }
        final SortedSet<Long> emptyRows = new TreeSet<>(Sets.difference(
                LongStream.range(0, in.size()).boxed().collect(Collectors.toSet()),
                points.stream().map(p -> p.y).collect(Collectors.toSet())));
        final SortedSet<Long> emptyCols = new TreeSet<>(Sets.difference(
                LongStream.range(0, in.get(0).length()).boxed().collect(Collectors.toSet()),
                points.stream().map(p -> p.x).collect(Collectors.toSet())));
        System.out.println("EmptyRows: " + emptyRows);
        System.out.println("EmptyCols: " + emptyCols);
        final Set<Point> adjustedPoints = new HashSet<>();
        final long add = 999999L; // 1 for part 1, 999999 for part 2.
        for (Point p : points) {
            long extraX = emptyCols.subSet(0L, p.x).size() * add;
            long extraY = emptyRows.subSet(0L, p.y).size() * add;
            adjustedPoints.add(new Point(p.x + extraX, p.y + extraY));
        }

        long sum = 0;
        for (Point p1 : adjustedPoints) {
            for (Point p2 : adjustedPoints) {
                if (p1 == p2) continue;
                sum += Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y);
            }
        }
        // We counted p1+p2 and p2+p1 as two separate pairs because we couldn't be arsed to loop n^2/2 times so we have to divide:
        System.out.println("Sum is " + sum/2);
    }

    private record Point(long x, long y) { }
}