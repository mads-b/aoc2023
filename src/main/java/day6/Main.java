package day6;

import com.google.common.io.Resources;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws Exception {
        final List<String> lines = Resources.readLines(Resources.getResource("day6.txt"), StandardCharsets.UTF_8);
        final List<Integer> times = parseList(lines.get(0));
        final List<Integer> distances = parseList(lines.get(1));

        int product = 1;
        for (int i = 0; i < times.size(); i++) {
            int time = times.get(i);
            int dist = distances.get(i);
            product *= options(time, dist);
        }
        System.out.println("Part 1 product: " + product);

        long time = Long.parseLong(times.stream().map(String::valueOf).collect(Collectors.joining()));
        long distance = Long.parseLong(distances.stream().map(String::valueOf).collect(Collectors.joining()));
        System.out.println("Part 2 options: " + options(time, distance));

    }

    private static long options(long time, long dist) {
        // s = t*B - B^2
        // Max dist is reached holding button half of the time
        double toBeat = 0.5*(time - Math.sqrt(time * time - 4.*dist));
        if (Math.ceil(toBeat) == Math.floor(toBeat)) toBeat +=1;
        long minTime = (long) Math.ceil(toBeat);
        long options = (time/2 - minTime)*2 + 1;
        if (time % 2 == 1) options++;
        return options;
    }

    private static List<Integer> parseList(final String str) {
        return Arrays.stream(str.split(":")[1].split(" "))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .toList();
    }
}