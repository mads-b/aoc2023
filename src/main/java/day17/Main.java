package day17;

import com.google.common.io.Resources;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws Exception {
        final int[][] costs = Resources.readLines(Resources.getResource("day17.txt"), StandardCharsets.UTF_8)
                .stream()
                .map(s -> s.chars().map(n -> Character.digit(n, 10)).toArray())
                .toArray(int[][]::new);

        final State start = new State(new Node(0, 0, Dir.R, 0), 0, null);
        System.out.println("Shortest path : " + calculateShortestPathFromSource(costs, start));
    }

    public static int calculateShortestPathFromSource(int[][] map, State start) {
        Set<Node> settledNodes = new HashSet<>();
        Queue<State> unsettledNodes = new PriorityQueue<>();

        unsettledNodes.add(start);

        while (!unsettledNodes.isEmpty()) {
            State currentState = unsettledNodes.remove();
            if (settledNodes.contains(currentState.node)) {
                continue;
            }

            for (State adjacentNode : adjacentsUltra(map, currentState)) {
                if (!settledNodes.contains(adjacentNode.node)) {
                    unsettledNodes.add(adjacentNode);
                }
            }
            settledNodes.add(currentState.node);
            if (currentState.node.x == map[0].length-1 && currentState.node.y == map.length-1) {
               return currentState.cost;
            }
        }
        return 0;
    }

    private static Set<State> adjacents(final int[][] map, final State current) {
        final Node node = current.node;
        final Set<State> neighborStates = new HashSet<>();
        if (node.x > 0 && node.canMove(Dir.L) && node.dir != Dir.R) {
            neighborStates.add(current.move(Dir.L, node.x - 1, node.y, map));
        }
        if (node.y > 0 && node.canMove(Dir.U) && node.dir != Dir.D) {
            neighborStates.add(current.move(Dir.U,  node.x, node.y-1, map));
        }
        if (node.x < map[0].length-1 && node.canMove(Dir.R)  && node.dir != Dir.L) {
            neighborStates.add(current.move(Dir.R,  node.x + 1, node.y, map));
        }
        if (node.y < map.length-1 && node.canMove(Dir.D) && node.dir != Dir.U) {
            neighborStates.add(current.move(Dir.D,  node.x, node.y+1, map));
        }
        return neighborStates;
    }

    private static Set<State> adjacentsUltra(final int[][] map, final State current) {
        final Node node = current.node;
        final Set<State> neighborStates = new HashSet<>();
        if (node.x > 0 && node.canMoveUltra(Dir.L) && node.dir != Dir.R) {
            neighborStates.add(current.move(Dir.L, node.x - 1, node.y, map));
        }
        if (node.y > 0 && node.canMoveUltra(Dir.U) && node.dir != Dir.D) {
            neighborStates.add(current.move(Dir.U,  node.x, node.y-1, map));
        }
        if (node.x < map[0].length-1 && node.canMoveUltra(Dir.R)  && node.dir != Dir.L) {
            neighborStates.add(current.move(Dir.R,  node.x + 1, node.y, map));
        }
        if (node.y < map.length-1 && node.canMoveUltra(Dir.D) && node.dir != Dir.U) {
            neighborStates.add(current.move(Dir.D,  node.x, node.y+1, map));
        }
        return neighborStates;
    }

    private record State(Node node, int cost, State prev) implements Comparable<State> {

        public State move(Dir dir, int newX, int newY, int[][] costs) {
            int steps = node.dir == dir ? node.steps + 1 : 1;
            final Node n = new Node(newX, newY, dir, steps);
            return new State(n, cost + costs[n.y][n.x], this);
        }

        @Override
        public int compareTo(final State o) {
            return cost - o.cost;
        }
    }
    private enum Dir {
        U, D, L, R;
    }

    private record Node(int x, int y, Dir dir, int steps) {

        public boolean canMove(final Dir dir) {
            if (dir != dir()) {
                return true;
            }
            return steps < 3;
        }

        public boolean canMoveUltra(final Dir dir) {
            if (dir != dir()) {
                if (steps < 4) { // Have to move at least 4 until turning is allowed.
                    return false;
                }
                return true;
            }
            return steps < 10;
        }
    }
}