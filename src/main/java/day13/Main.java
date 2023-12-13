package day13;

import com.google.common.io.Resources;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

    private static boolean part2 = true;

    public static void main(String[] args) throws Exception {
        final List<Board> input = Arrays.stream(Resources.toString(Resources.getResource("day13.txt"), StandardCharsets.UTF_8)
                        .split("\n\n"))
                .map(Board::new)
                .toList();
        System.out.println("Num boards: " + input.size());
        System.out.println("Sum: " + input.stream()
                .map(b -> {
                    int horizMirrorLine = b.findHorizontalMirrorLine() + 1;
                    int vertMirrorLine = b.transposed().findHorizontalMirrorLine() + 1;
                    if (horizMirrorLine < vertMirrorLine) {
                        // "there's many solutions but we only care about one in particular using arbitrary rules" :(
                        return horizMirrorLine * 100;
                    }
                    return vertMirrorLine;
                })
                .reduce(0, Integer::sum));
    }


    private static class Board {
        private final List<String> lines;

        public Board(final String input) {
            lines = Arrays.asList(input.split("\n"));
        }

        private Board(final List<String> lines) {
            this.lines = lines;
        }

        public Board transposed() {
            final List<String> newLines = new ArrayList<>(lines.get(0).length());
            for (int j = 0; j < lines.get(0).length(); j++) {
                final StringBuilder bldr = new StringBuilder(lines.size());
                for (int i = 0; i < lines.size(); i++) {
                    bldr.append(lines.get(i).charAt(j));
                }
                newLines.add(bldr.toString());
            }
            return new Board(newLines);
        }

        public int findHorizontalMirrorLine() {
            for (int i = 0; i < lines.size()-1; i++) {
                final SmudgeDetector smudgeDetector = new SmudgeDetector();
                if (smudgeDetector.matches(lines.get(i), lines.get(i+1))) { // Mirror candidate. Walk both ways to see if true mirror
                    int linesToMatch = Math.min(i, lines.size()-i-2);
                    boolean match = true;
                    for (int j = 1; j <= linesToMatch; j++) {
                        if (!smudgeDetector.matches(lines.get(i+1+j), lines.get(i-j))) {
                            match = false;
                            break;
                        }
                    }
                    if (match && smudgeDetector.smudgesToRemove == 0) {
                        return i;
                    }
                }
            }
            return Integer.MAX_VALUE-1000;
        }

        public String toString() {
            return String.join("\n", lines) + "\n";
        }
    }

    private static class SmudgeDetector {
        private int smudgesToRemove = part2 ? 1 : 0;

        public boolean matches(final String s1, final String s2) {
            if (smudgesToRemove == 0) return s1.equals(s2);
            int diffCount = 0;
            for (int i = 0; i < s1.length(); i++) {
                diffCount += s1.charAt(i) == s2.charAt(i) ? 0 : 1;
            }
            if (diffCount == 1) {
                smudgesToRemove--;
                return true;
            } else {
                return diffCount == 0;
            }
        }
    }
}