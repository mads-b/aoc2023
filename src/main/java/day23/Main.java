package day23;

import com.google.common.io.Resources;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    private static int maxX;
    private static int maxY;
    private static boolean skipSlopes = true;
    public static void main(String[] args) throws Exception {
        final char[][] input = Resources.readLines(Resources.getResource("day23.txt"), StandardCharsets.UTF_8)
                .stream()
                .map(String::toCharArray)
                .toArray(char[][]::new);
        maxX = input[0].length-1;
        maxY = input.length-1;

        //Make all nodes
        final Junction[][] junctions = new Junction[input.length][input[0].length];
        for (int y = 0; y <= maxY; y++) {
            for (int x = 0; x <= maxX; x++) {
                junctions[y][x] = new Junction(input[y][x], new Pt(x, y));
            }
        }

        // Add all neighbors
        Set<Junction> allJunctions = new HashSet<>();
        for (int y = 0; y <= maxY; y++) {
            for (int x = 0; x <= maxX; x++) {
                final Point pt = new Point(x, y, input[y][x]);
                junctions[y][x].neighbors = pt.dirs().stream()
                        .filter(dir -> dir.at(input) != '#')
                        .map(dir -> new Edge(junctions[dir.y][dir.x], 1, dir.at(input) != '.' && dir.at(input) != dir.dir))
                        .toList();
                if (input[y][x] != '#') {
                    allJunctions.add(junctions[y][x]);
                }
            }
        }
        // Prune all junctions with two neighbors by directly linking their neighbors to sparsify the graph
        System.out.println("Original junctions: " + allJunctions.size());
        boolean pruned = false;
        while (!pruned) {
            pruned = true;
            for (final Junction j : allJunctions) {
                if (j.neighbors.size() != 2) {
                    continue;
                }
                pruned = false;
                final Edge e1 = j.neighbors.get(0);
                final Edge e2 = j.neighbors.get(1);
                final int newDist = e1.dist + e2.dist;
                // Join the edges together, eliminating this junction
                e1.j.neighbors = e1.j.neighbors.stream()
                        .map(e -> e.j != j ? e : new Edge(e2.j, newDist, e.uphill || e2.uphill))
                        .toList();
                e2.j.neighbors = e2.j.neighbors.stream()
                        .map(e -> e.j != j ? e : new Edge(e1.j, newDist, e.uphill || e1.uphill))
                        .toList();
                j.neighbors = List.of();
            }
        }
        final List<Junction> prunedJunctions = allJunctions.stream().filter(j -> !j.neighbors.isEmpty()).toList();

        final Stack<State> unexplored = new Stack<>();
        final State initial = State.start(prunedJunctions.stream().filter(j -> j.loc.y == 0).findFirst().orElseThrow());
        unexplored.add(initial);
        long mostOhs = initial.dist();

        while (!unexplored.isEmpty()) {
            var explored = unexplored.pop();
            var ohs = explored.dist();
            if (explored.pos().y == maxY && ohs > mostOhs) {
                mostOhs = ohs;
                System.out.println("Candidate walk is " + mostOhs);
            }
            unexplored.addAll(explored.neighbors().stream()
                    .map(explored::add).toList());
        }

        System.out.println("Longest walk is " + (mostOhs - initial.dist()));
    }

    private static class State {
        private List<Edge> steps;
        private Set<Junction> visited;

        public State(final List<Edge> steps) {
            this.steps = steps;
            this.visited = steps.stream().map(e -> e.j).collect(Collectors.toSet());
        }

        public static State start(final Junction st) {
            return new State(List.of(new Edge(st, 0, false)));
        }

        public State add(final Edge e) {
            return new State(Stream.concat(steps.stream(), Stream.of(e)).toList());
        }

        public Pt pos() {
            return steps.get(steps.size()-1).j().loc;
        }

        public List<Edge> neighbors() {
            return steps.get(steps.size()-1).j().neighbors.stream()
                    .filter(n -> !n.uphill || skipSlopes)
                    .filter(e -> !visited.contains(e.j)) // Can't revisit junctions
                    .toList();
        }

        public int dist() {
            return steps.stream().map(j -> j.dist).mapToInt(j -> j).sum();
        }
    }

    private static class Junction {
        private final char ch;
        private final Pt loc;
        private List<Edge> neighbors = List.of();
        public Junction(char ch, Pt loc) {
            this.ch = ch;
            this.loc = loc;
        }
    }

    private record Edge(Junction j, int dist, boolean uphill) {}

    private record Pt(int x, int y) {}

    private record Point(int x, int y, char dir){

        public List<Point> dirs() {
            return List.of(
                    new Point(x+1, y, '>'),
                    new Point(x-1, y, '<'),
                    new Point(x, y+1, 'v'),
                    new Point(x, y-1, '^'));
        }

        public char at(char[][] board) {
            if (x < 0 || x > maxX || y < 0 || y > maxY) {
                return '#';
            }
            return board[y][x];
        }
    }
}