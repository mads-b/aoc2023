package day24;

import com.google.common.io.Resources;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Main {
    public static void main(String[] args) throws Exception {
        final List<Hailstone> input = Resources.readLines(Resources.getResource("day24.txt"), StandardCharsets.UTF_8)
                .stream()
                .map(Hailstone::parse)
                .toList();
        // Part 1:
        int count = 0;
        double min = 200000000000000L;
        double max = 400000000000000L;
        for (int i = 0; i < input.size(); i++) {
            for (int j = i+1; j < input.size(); j++) {
                count += Hailstone.intersection2d(input.get(i), input.get(j))
                        .filter(p -> p.x > min && p.x < max && p.y > min && p.y < max)
                        .isPresent() ? 1 : 0;
            }
        }
        System.out.println("%d intersections in data".formatted(count));

        var a = input.get(0);
        var b = input.get(1);
        var c = input.get(2);

        var aMatrix = new double[][] {
        { a.vel.y - b.vel.y, b.vel.x - a.vel.x, 0,         b.pos.y - a.pos.y, a.pos.x - b.pos.x, 0       },
        { a.vel.y - c.vel.y, c.vel.x - a.vel.x, 0,         c.pos.y - a.pos.y, a.pos.x - c.pos.x, 0       },
        { b.vel.z - a.vel.z, 0,         a.vel.x - b.vel.x, a.pos.z - b.pos.z, 0,       b.pos.x - a.pos.x },
        { c.vel.z - a.vel.z, 0,         a.vel.x - c.vel.x, a.pos.z - c.pos.z, 0,       c.pos.x - a.pos.x },
        { 0,         a.vel.z - b.vel.z, b.vel.y - a.vel.y, 0,       b.pos.z - a.pos.z, a.pos.y - b.pos.y },
        { 0,         a.vel.z - c.vel.z, c.vel.y - a.vel.y, 0,       c.pos.z - a.pos.z, a.pos.y - c.pos.y }
    };
        var determinant = new double[]{
                (b.pos.y * b.vel.x - b.pos.x * b.vel.y) - (a.pos.y * a.vel.x - a.pos.x * a.vel.y),
                (c.pos.y * c.vel.x - c.pos.x * c.vel.y) - (a.pos.y * a.vel.x - a.pos.x * a.vel.y),
                (b.pos.x * b.vel.z - b.pos.z * b.vel.x) - (a.pos.x * a.vel.z - a.pos.z * a.vel.x),
                (c.pos.x * c.vel.z - c.pos.z * c.vel.x) - (a.pos.x * a.vel.z - a.pos.z * a.vel.x),
                (b.pos.z * b.vel.y - b.pos.y * b.vel.z) - (a.pos.z * a.vel.y - a.pos.y * a.vel.z),
                (c.pos.z * c.vel.y - c.pos.y * c.vel.z) - (a.pos.z * a.vel.y - a.pos.y * a.vel.z)
        };
        double[] solution = gaussianSolver(aMatrix, determinant);
        System.out.println(Arrays.toString(solution));
        System.out.println("sum: " + Math.round(solution[0] + solution[1] + solution[2]));
    }


    private record Hailstone(P3D pos, P3D vel) {
        public static Hailstone parse(String s) {
            var split = Arrays.stream(s.split("@")).map(P3D::parse).toList();
            return new Hailstone(split.get(0), split.get(1));
        }

        public static Optional<P3D> intersection2d(Hailstone h1, Hailstone h2) {
            if (h1.vel.crossProduct2D(h2.vel) == 0) { // Velocity vectors are parallel
                return Optional.empty();
            }

            var t1 = timeIntersection2d(h1, h2);
            var t2 = timeIntersection2d(h2, h1);
            if (t1 < 0 || t2 < 0) {
                return Optional.empty();
            }
            final P3D cross = h1.atT(t1);
            return Optional.of(cross);
        }

        private static double timeIntersection2d(Hailstone h1, Hailstone h2) {
            return ((h2.pos.x - h1.pos.x)*h2.vel.y - (h2.pos.y - h1.pos.y)*h2.vel.x)
                    / (h1.vel.x * h2.vel.y - h1.vel.y * h2.vel.x);
        }

        public P3D atT(double t) {
            return new P3D(pos.x + vel.x * t,
                    pos.y + vel.y * t,
                    pos.z + vel.z * t);
        }
    }

    private record P3D(double x, double y, double z) {
        public static P3D parse(String s) {
            var split = Arrays.stream(s.split(",")).map(String::trim).map(Long::parseLong).toList();
            return new P3D(split.get(0), split.get(1), split.get(2));
        }

        public double crossProduct2D(final P3D other) {
            return x*other.y - y*other.x;
        }
    }

    public static double[] gaussianSolver(double[][] A, double[] b) {
        int n = b.length;

        for (int p = 0; p < n; p++) {

            // find pivot row and swap
            int max = p;
            for (int i = p + 1; i < n; i++) {
                if (Math.abs(A[i][p]) > Math.abs(A[max][p])) {
                    max = i;
                }
            }
            double[] temp = A[p]; A[p] = A[max]; A[max] = temp;
            double t = b[p]; b[p] = b[max]; b[max] = t;

            // pivot within A and b
            for (int i = p + 1; i < n; i++) {
                double alpha = A[i][p] / A[p][p];
                b[i] -= alpha * b[p];
                for (int j = p; j < n; j++) {
                    A[i][j] -= alpha * A[p][j];
                }
            }
        }

        // back substitution
        double[] x = new double[n];
        for (int i = n - 1; i >= 0; i--) {
            double sum = 0L;
            for (int j = i + 1; j < n; j++) {
                sum += A[i][j] * x[j];
            }
            x[i] = (b[i] - sum) / A[i][i];
        }
        return x;
    }
}