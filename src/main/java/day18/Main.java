package day18;

import com.google.common.io.Resources;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        final List<Dig> digs = Resources.readLines(Resources.getResource("day18.txt"), StandardCharsets.UTF_8)
                .stream()
                .map(Dig::parse).toList();
        System.out.println("Poly area " + computeArea(digs));
        System.out.println("Part 2 poly area " + computeArea(digs.stream().map(Dig::bigDig).toList()));
    }

    public static long computeArea(final List<Dig> digs) {
        Point prev = new Point(0,0);
        final List<Point> poly = new ArrayList<>();
        poly.add(new Point(0, 0));
        long circumference = 0;
        for (Dig d : digs) {
            long dy = d.dir == Dir.U ? -d.count : d.dir == Dir.D ? d.count : 0;
            long dx = d.dir == Dir.L ? -d.count : d.dir == Dir.R ? d.count : 0;
            circumference += Math.abs(dy)+Math.abs(dx);
            final Point p2 = new Point(prev.x  + dx, prev.y + dy);
            poly.add(p2);
            prev = p2;
        }
        long area = (long) calculatePolygonArea(poly);
        return area + circumference/2 + 1;
    }

    public static double calculatePolygonArea(List<Point> points) {
        long n = points.size();
        double area = 0.0;

        for (int i = 0; i < n - 1; i++) {
            Point current = points.get(i);
            Point next = points.get(i + 1);
            area += (current.x * next.y) - (next.x * current.y);
        }
        area = 0.5 * Math.abs(area);

        return area;
    }

    private record Point(long x, long y) {}

    private record Dig(Dir dir, long count, Dig bigDig) {

        public static Dig parse(final String str) {
            final String[] split = str.split(" ");
            long bigLen = HexFormat.fromHexDigits(split[2].substring(2, 7));
            Dir bigDir = Dir.fromChar(split[2].charAt(7));

            return new Dig(
                    Dir.valueOf(split[0]),
                    Integer.parseInt(split[1]),
                    new Dig(bigDir, bigLen, null)
            );
        }
    }
    private enum Dir {
        U, D, L, R;

        public static Dir fromChar(char ch) {
            return switch (ch) {
                case '0' -> R;
                case '1' -> D;
                case '2' -> L;
                case '3' -> U;
                default -> throw new IllegalStateException("Unexpected value: " + ch);
            };
        }
    }
}