package day16;

import com.google.common.io.Resources;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws Exception {
        final char[][] input = Resources.readLines(Resources.getResource("day16.txt"), StandardCharsets.UTF_8)
                .stream()
                .map(String::toCharArray)
                .toArray(char[][]::new);
        System.out.println("Part 1, energizing from 0,0,R " + countEnergize(input, new BeamFront(Dir.R, 0, 0)));

        int maxEnergized = 0;
        for (int i = 0; i < input.length; i++) {
            maxEnergized = Math.max(maxEnergized, countEnergize(input, new BeamFront(Dir.R, 0, i)));
            maxEnergized = Math.max(maxEnergized, countEnergize(input, new BeamFront(Dir.L, input[i].length-1, i)));
        }
        for (int i = 0; i < input[0].length; i++) {
            maxEnergized = Math.max(maxEnergized, countEnergize(input, new BeamFront(Dir.D, i, 0)));
            maxEnergized = Math.max(maxEnergized, countEnergize(input, new BeamFront(Dir.U, i, input.length)));
        }
        System.out.println("Part 2, max possible to energize from an edge: " + maxEnergized);
    }

    private static int countEnergize(final char[][] input, final BeamFront beam) {
        final boolean[][] energized = new boolean[input.length][input[0].length];
        final Set<BeamFront> seen = new HashSet<>();
        energize(seen, input, energized, beam);
        int count = 0;
        for (final boolean[] booleans : energized) {
            for (final boolean aBoolean : booleans) {
                count += aBoolean ? 1 : 0;
            }
        }
        return count;
    }

    private static void energize(
            final Set<BeamFront> seen,
            final char[][] input,
            final boolean[][] energized,
            final BeamFront beam) {
        if (beam.x < 0 || beam.y < 0 || beam.x >= input[0].length || beam.y >= input.length) {
            return; // Out of bounds.
        }
        if (seen.contains(beam)) {
            return;
        }
        seen.add(beam);
        
        energized[beam.y][beam.x] = true;
        char square = input[beam.y][beam.x];
        Dir dir = beam.d;
        int x = beam.x;
        int y = beam.y;
        
        if (square == '.'
                || ((dir == Dir.U || dir == Dir.D) && square == '|')
                || ((dir == Dir.L || dir == Dir.R) && square == '-')) {
            int dx = dir == Dir.L ? -1 : dir == Dir.R ? 1 : 0;
            int dy = dir == Dir.U ? -1 : dir == Dir.D ? 1 : 0;
            energize(seen, input, energized, new BeamFront(dir, x + dx, y + dy));
        } else if (square == '|') {
            energize(seen, input, energized, new BeamFront(Dir.U, x, y-1));
            energize(seen, input, energized, new BeamFront(Dir.D, x, y+1));
        } else if (square == '-') {
            energize(seen, input, energized, new BeamFront(Dir.L, x-1, y));
            energize(seen, input, energized, new BeamFront(Dir.R, x+1, y));
        } else if (square == '/') {
            switch (dir) {
                case D -> energize(seen, input, energized, new BeamFront(Dir.L, x - 1, y));
                case L -> energize(seen, input, energized, new BeamFront(Dir.D, x, y + 1));
                case R -> energize(seen, input, energized, new BeamFront(Dir.U, x, y - 1));
                case U -> energize(seen, input, energized, new BeamFront(Dir.R, x + 1, y));
            }
        } else if (square == '\\') {
            switch (dir) {
                case D -> energize(seen, input, energized, new BeamFront(Dir.R, x+1, y));
                case L -> energize(seen, input, energized, new BeamFront(Dir.U, x, y-1));
                case R -> energize(seen, input, energized, new BeamFront(Dir.D, x, y+1));
                case U -> energize(seen, input, energized, new BeamFront(Dir.L, x-1, y));
            }
        }
    }

    private record BeamFront(Dir d, int x, int y) {}
    private enum Dir {U, D, L, R}
}