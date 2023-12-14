package day14;

import com.google.common.io.Resources;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws Exception {
        final char[][] input = Resources.readLines(Resources.getResource("day14.txt"), StandardCharsets.UTF_8)
                .stream()
                .map(String::toCharArray)
                .toArray(char[][]::new);
        final Map<Integer, Integer> hashCodes = new HashMap<>();
        long totIterations = 1000000000;
        for (int i = 1; i <= 1000; i++) {
            cycle(input);
            int hashCode = Arrays.deepHashCode(input);
            if (hashCodes.containsKey(hashCode)) {
                final int prev = hashCodes.get(hashCode);
                final int delta = i-prev;
                System.out.println("Suspected cycle at iteration %d because same hashcode at %d. Delta: %s".formatted(i, prev, delta));

                // Cycle would prove that we have the same board at iteration prev + delta*N.
                // Now we just need to find the largest N such that prev + delta * N < totIterations
                long sameBoardStateAt = prev + delta*((totIterations-prev)/delta);
                System.out.println("Same board state at " + sameBoardStateAt);
                for (; sameBoardStateAt < totIterations; sameBoardStateAt++) {
                    cycle(input);
                }
                break;
            }
            hashCodes.put(hashCode, i);
        }
        System.out.println("Load: " + measureLoad(input));
    }

    private static void cycle(char[][] input) {
        tiltNorth(input);
        tiltWest(input);
        tiltSouth(input);
        tiltEast(input);
    }

    private static void tiltNorth(final char[][] board) {
        int[] minY = new int[board[0].length];
        for (int y = 0; y < board.length; y++) {
            for (int x = 0; x < board[y].length; x++) {
                if (board[y][x] == '#') {
                    minY[x] = y+1;
                } else if (board[y][x] == 'O') {
                    board[y][x] = '.';
                    board[minY[x]][x] = 'O';
                    minY[x]++;
                }
            }
        }
    }

    private static void tiltWest(final char[][] board) {
        int[] minX = new int[board.length];
        for (int x = 0; x < board[0].length; x++) {
            for (int y = 0; y < board.length; y++) {
                if (board[y][x] == '#') {
                    minX[y] = x+1;
                } else if (board[y][x] == 'O') {
                    board[y][x] = '.';
                    board[y][minX[y]] = 'O';
                    minX[y]++;
                }
            }
        }
    }

    private static void tiltSouth(final char[][] board) {
        int[] maxY = new int[board[0].length];
        Arrays.fill(maxY, board.length-1);
        for (int y = board.length-1; y >= 0; y--) {
            for (int x = board[y].length-1; x >= 0 ; x--) {
                if (board[y][x] == '#') {
                    maxY[x] = y-1;
                } else if (board[y][x] == 'O') {
                    board[y][x] = '.';
                    board[maxY[x]][x] = 'O';
                    maxY[x]--;
                }
            }
        }
    }

    private static void tiltEast(final char[][] board) {
        int[] maxX = new int[board.length];
        Arrays.fill(maxX, board[0].length-1);
        for (int x = board[0].length-1; x >=0; x--) {
            for (int y = board.length-1; y >= 0; y--) {
                if (board[y][x] == '#') {
                    maxX[y] = x-1;
                } else if (board[y][x] == 'O') {
                    board[y][x] = '.';
                    board[y][maxX[y]] = 'O';
                    maxX[y]--;
                }
            }
        }
    }

    private static long measureLoad(char[][] board) {
        long sum = 0;
        for (int y = 0; y < board.length; y++) {
            int loadLine = board.length - y;
            for (int x = 0; x < board[y].length; x++) {
                if (board[y][x] == 'O') {
                    sum += loadLine;
                }
            }
        }
        return sum;
    }
}