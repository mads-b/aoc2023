package day10;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {
        final List<String> in =  Resources.readLines(Resources.getResource("day10.txt"), StandardCharsets.UTF_8);
        final List<String> padTopBottom = ImmutableList.<String>builder()
                .add("O".repeat(in.get(0).length()))
                .addAll(in)
                .add("O".repeat(in.get(0).length()))
                .build();
        final List<char[]> input = padTopBottom
                .stream()
                .map(str -> "O" + str + "O")
                .map(String::toCharArray)
                .toList();

        final int width = input.get(0).length;
        final int height = input.size();
        final Pipe[][] chart = new Pipe[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                chart[y][x] = new Pipe(x, y, input.get(y)[x]);
            }
        }
        Pipe start = null;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                final Pipe cur = chart[y][x];
                switch (cur.shape) {
                    case 'F' -> cur.setNeighbors(chart[y + 1][x], chart[y][x + 1]);
                    case 'J' -> cur.setNeighbors(chart[y][x - 1], chart[y - 1][x]);
                    case '|' -> cur.setNeighbors(chart[y - 1][x], chart[y + 1][x]);
                    case '-' -> cur.setNeighbors(chart[y][x - 1], chart[y][x + 1]);
                    case 'L' -> cur.setNeighbors(chart[y - 1][x], chart[y][x + 1]);
                    case '7' -> cur.setNeighbors(chart[y][x - 1], chart[y + 1][x]);
                    case 'S' -> start = cur;
                }
            }
        }

        Pipe prev = start;
        Pipe next;
        if (chart[start.y+1][start.x].shape != '.') next = chart[start.y+1][start.x];
        else if (chart[start.y-1][start.x].shape != '.') next = chart[start.y-1][start.x];
        else if (chart[start.y][start.x+1].shape != '.') next = chart[start.y][start.x+1];
        else next = chart[start.y][start.x-1];
        System.out.println("Start at: (" + start.x + "," + start.y + ") (" + start.shape + ")");

        int steps = 0;
        List<Pipe> trail = new ArrayList<>();
        trail.add(start);
        while(next != start) {
            trail.add(next);
            final Pipe tempPrev = next;
            next = next.next(prev);
            prev = tempPrev;
            steps++;
        }
        System.out.println("Steps to get around: " + steps + " half: " + Math.ceil(steps/2.));
        // Pruning non-trail pipe:
        for (int y = 1; y < height-1; y++) {
            for (int x = 1; x < width-1; x++) {
                if (!trail.contains(chart[y][x])) {
                    chart[y][x].shape = '.';
                }
            }
        }

        for (int i = 0; i < trail.size()-1;i++) {
            prev = trail.get(i);
            next = trail.get(i+1);
            int dx = next.x - prev.x;
            int dy = next.y - prev.y;
            // Normal is inverting x and y, so we can add the normal vector to the current point and get the point to
            // the "left" of the direction of travel.
            // Now, we don't really know whether the left side is outside or inside, so invert the symbols if it's wrong
            int mul = 1;
            floodFill(chart, 'I', next.x + dy*mul, next.y - dx*mul);
            floodFill(chart, 'O', next.x - dy*mul, next.y + dx*mul); 
            floodFill(chart, 'I', prev.x + dy*mul, prev.y - dx*mul);
            floodFill(chart, 'O', prev.x - dy*mul, prev.y + dx*mul);
        }

        int is = 0;
        int dots = 0;
        int os = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (chart[y][x].shape == 'I') is++;
                if (chart[y][x].shape == 'O') os++;
                if (chart[y][x].shape == '.') dots++;
                System.out.print(chart[y][x].shape);
            }
            System.out.println();
        }
        System.out.println("Inside: " + is);
        System.out.println("Outside: " + os);
        System.out.println("Dots: " + dots);
    }

    private static void floodFill(final Pipe[][] pipes, char newShape, final int x, final int y) {
        if (pipes[y][x].shape != '.') return;
        pipes[y][x].shape = newShape;
        floodFill(pipes, newShape, x+1, y);
        floodFill(pipes, newShape, x-1, y);
        floodFill(pipes, newShape, x, y+1);
        floodFill(pipes, newShape, x, y-1);
    }

    private static class Pipe {
        private final int x;
        private final int y;
        private char shape;
        private Pipe neighbor1;
        private Pipe neighbor2;

        public Pipe(final int x, final int y, final char shape) {
            this.x = x;
            this.y = y;
            this.shape = shape;
        }

        public void setNeighbors(final Pipe one, final Pipe two) {
            this.neighbor1 = one;
            this.neighbor2 = two;
        }

        public Pipe next(final Pipe prev) {
            return prev == neighbor1 ? neighbor2 : neighbor1;
        }
    }
}