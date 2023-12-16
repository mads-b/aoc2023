package day15;

import com.google.common.io.Resources;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class Main {
    public static void main(String[] args) throws Exception {
        final String input = Resources.readLines(Resources.getResource("day15.txt"), StandardCharsets.UTF_8).get(0);
        String[] split = input.split(",");

        System.out.println("P1 sum " + Arrays.stream(split).map(Main::hash).mapToInt(i -> i).sum());
        List<List<LabeledLens>> boxes = new ArrayList<>(256);
        IntStream.range(0, 256).forEach(i -> boxes.add(new ArrayList<>()));

        for (String s : split) {
            if (s.endsWith("-")) {
                final String label = s.substring(0, s.length() - 1);
                boxes.get(hash(label)).remove(new LabeledLens(label, 0));
            } else {
                final String[] eqOp = s.split("=");
                int focal = Integer.parseInt(eqOp[1]);
                int hashKey = hash(eqOp[0]);
                final List<LabeledLens> relevant = boxes.get(hashKey);
                final LabeledLens n = new LabeledLens(eqOp[0], focal);
                boolean replaced = false;
                for (int i = 0; i < relevant.size(); i++) {
                    if (relevant.get(i).equals(n)) {
                        relevant.set(i, n);
                        replaced = true;
                    }
                }
                if (!replaced) relevant.add(n);
            }
        }

        long sum = 0;
        for (int i = 0; i < 256; i++) {
            final List<LabeledLens> box = boxes.get(i);
            for (int j = 0; j < box.size(); j++) {
                sum += (i + 1) * (j+1) * box.get(j).focalLength;
            }
        }
        System.out.println("Sum is " + sum);
    }

    private record LabeledLens(String label, int focalLength) {
        @Override
        public boolean equals(final Object o) {
            if (!(o instanceof LabeledLens)) return false;
            return label.equals(((LabeledLens) o).label);
        }

        @Override
        public int hashCode() {
            return hash(label);
        }
    }

    private static int hash(final String str) {
        int cur = 0;
        for (char c : str.toCharArray()) {
            cur += c;
            cur *= 17;
            cur %= 256;
        }
        return cur;
    }
}