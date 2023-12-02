package day2;

import com.google.common.io.Resources;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {
        final List<Line> lines = Resources.readLines(Resources.getResource("day2.txt"), StandardCharsets.UTF_8).stream().map(Line::parse).toList();
        // Part 1
        System.out.println("Sum of lines: " + lines
                .stream()
                .filter(l -> {
                    final Round r = l.max();
                    return r.red <= 12 && r.green <= 13 && r.blue <= 14;
                })
                .map(Line::gameNum)
                .reduce(0, Integer::sum));

        // Part 2
        System.out.println("Power of lines: " + lines.stream()
                .map(Line::max)
                .map(r -> r.red * r.green * r.blue)
                .reduce(0, Integer::sum));
    }

    private record Line(int gameNum, List<Round> rounds) {

        public static Line parse(final String s) {
            final String[] split = s.split(":");
            final int gameNum = Integer.parseInt(split[0].split(" ")[1]);
            final List<Round> rounds = Arrays.stream(split[1].split(";")).map(Round::parse).toList();
            return new Line(gameNum, rounds);
        }

        public Round max() {
            int r = 0, g = 0, b = 0;
            for (Round round : rounds) {
                r = Math.max(r, round.red);
                g = Math.max(g, round.green);
                b = Math.max(b, round.blue);
            }
            return new Round(r, g, b);
        }
    }

    private record Round(int red, int green, int blue) {

        public static Round parse(String str) {
            int r = 0, g = 0, b = 0;
            final String[] split = str.split(",");
            for (final String item : split) {
                final String[] ar = item.trim().split(" ");
                if (ar[1].equals("red")) r = Integer.parseInt(ar[0]);
                if (ar[1].equals("green")) g = Integer.parseInt(ar[0]);
                if (ar[1].equals("blue")) b = Integer.parseInt(ar[0]);
            }
            return new Round(r, g, b);
        }
    }
}