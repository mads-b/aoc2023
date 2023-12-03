package day3;

import com.google.common.io.Resources;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {
        final char[][] board = Resources.readLines(Resources.getResource("day3.txt"), StandardCharsets.UTF_8).stream()
                .map(String::toCharArray)
                .toArray(char[][]::new);
        int sum = 0;
        for (int y = 0; y < board.length; y++) {
            int numBuf = 0;
            boolean isAdjacent = false;
            for (int x = 0; x < board.length; x++) {
                char cur = board[y][x];
                if (Character.isDigit(cur)) {
                    numBuf *= 10;
                    numBuf += Character.digit(cur, 10);
                    isAdjacent |= isAdjacentToSymbol(board, x, y);
                } else {
                    if (isAdjacent) {
                        sum += numBuf;
                    }
                    isAdjacent = false;
                    numBuf = 0;
                }
            }
            if (isAdjacent) {
                sum += numBuf;
            }
        }

        final List<Point> stars = new ArrayList<>();
        for (int y = 0; y < board.length; y++) {
            for (int x = 0; x < board[y].length; x++) {
                if (board[y][x] == '*') {
                    stars.add(new Point(x, y));
                }
            }
        }

        // Try to find out how many combinations of numbers there can be around a star:
        // Top row | bottom row:
        // 1.1 || 1.. || ..1 || X1X
        // Mid row:
        // 1*1 || .*1 || 1*.

        int sum2 = 0;
        for (final Point p : stars) {
            int adjacentNums = 0;
            int gearProduct = 1;
            // Top row:
            if (p.y != 0) {
                if (Character.isDigit(board[p.y-1][p.x])) {
                    adjacentNums++;
                    gearProduct *= readNum(board, p.x, p.y-1);
                } else {
                    if (Character.isDigit(board[p.y-1][p.x-1])) {
                        adjacentNums++;
                        gearProduct *= readNum(board, p.x-1, p.y-1);
                    }
                    if (Character.isDigit(board[p.y-1][p.x+1])) {
                        adjacentNums++;
                        gearProduct *= readNum(board, p.x+1, p.y-1);
                    }
                }
            }

            // Mid row:
            if (p.x > 0) {
                if (Character.isDigit(board[p.y][p.x-1])) {
                    adjacentNums++;
                    gearProduct *= readNum(board, p.x-1, p.y);
                }
            }
            if (p.x < board[p.y].length-1) {
                if (Character.isDigit(board[p.y][p.x+1])) {
                    adjacentNums++;
                    gearProduct *= readNum(board, p.x+1, p.y);
                }
            }


            // Bottom row:
            if (p.y != board.length-1) {
                if (Character.isDigit(board[p.y+1][p.x])) {
                    adjacentNums++;
                    gearProduct *= readNum(board, p.x, p.y+1);
                } else {
                    if (Character.isDigit(board[p.y+1][p.x-1])) {
                        adjacentNums++;
                        gearProduct *= readNum(board, p.x-1, p.y+1);
                    }
                    if (Character.isDigit(board[p.y+1][p.x+1])) {
                        adjacentNums++;
                        gearProduct *= readNum(board, p.x+1, p.y+1);
                    }
                }
            }
            if (adjacentNums == 2) {
                sum2 += gearProduct;
            }
        }

        System.out.println("The sum: " + sum);
        System.out.println("the gear sums " + sum2);
    }

    private record Point(int x, int y) { }
    
    static boolean isAdjacentToSymbol(final char[][] board, final int x, final int y) {
        boolean adjacent = false;
        if (x > 0) {
            adjacent |= isSymbol(board[y][x-1]);
            if (y > 0) {
                adjacent |= isSymbol(board[y-1][x-1]);
                adjacent |= isSymbol(board[y-1][x]);
            }
            if (y < board.length-1) {
                adjacent |= isSymbol(board[y+1][x-1]);
                adjacent |= isSymbol(board[y+1][x]);
            }
        }

        if (x < board[0].length-1) {
            adjacent |= isSymbol(board[y][x+1]);
            if (y > 0) {
                adjacent |= isSymbol(board[y-1][x+1]);
                adjacent |= isSymbol(board[y-1][x]);
            }
            if (y < board.length-1) {
                adjacent |= isSymbol(board[y+1][x+1]);
                adjacent |= isSymbol(board[y+1][x]);
            }
        }
        return adjacent;
    }

    private static int readNum(final char[][] board, int x, final int y) {
        while(x != 0 && Character.isDigit(board[y][x-1])) {
            x--;
        }
        int sum = 0;
        while(x < board[y].length && Character.isDigit(board[y][x])) {
            sum *= 10;
            sum += Character.digit(board[y][x], 10);
            x++;
        }
        System.out.println("Read num @ " + x + " X " + y + " : " + sum);
        return sum;
    }

    private static boolean isSymbol(final char c) {
        return !Character.isDigit(c) && c != '.';
    }
}