package day8;

import com.google.common.io.Resources;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) throws Exception {
        final List<String> lns = Resources.readLines(Resources.getResource("day8.txt"), StandardCharsets.UTF_8);
        final Map<String, Node> nodes = new HashMap<>();

        for (int i = 2; i < lns.size(); i++) {
            final Node n = new Node(lns.get(i));
            nodes.put(n.label, n);
        }
        for (Node n : nodes.values()) {
            n.left = nodes.get(n.leftLabel);
            n.right = nodes.get(n.rightLabel);
        }

        //System.out.println("Reached the end in " + numSteps(nodes.get("AAA"), "ZZZ", new Instruction(lns.get(0))));

        final List<Node> starts = nodes.values().stream().filter(n -> n.label.endsWith("A")).toList();
        final long minSteps = starts.stream()
                .map(n -> numSteps(n, "Z", new Instruction(lns.get(0))))
                .reduce(1L, (lcm, num) -> lcm * num / lcm(lcm, num));

        System.out.println("Reached the end in " + minSteps);
    }

    public static long lcm(long a, long b) {
        if (b == 0) {
            return a;
        }
        return lcm(b, a % b);
    }

    private static long numSteps(Node cur, final String endSuffix, final Instruction instruction) {
        int steps = 0;
        while(!cur.label.endsWith(endSuffix)) {
            cur = instruction.next(cur);
            steps++;
        }
        return steps;
    }

   private static class Node {
       private static final Pattern P = Pattern.compile("([A-Z0-9]+) = \\(([A-Z0-9]+), ([A-Z0-9]+)\\)");

       private String label;
        private Node left;
        private final String leftLabel;
        private Node right;
        private final String rightLabel;

        public Node(final String line) {
            final Matcher matcher = P.matcher(line);
            matcher.find();
            this.label = matcher.group(1);
            this.leftLabel = matcher.group(2);
            this.rightLabel = matcher.group(3);
        }
    }

    private static class Instruction {
        private final char[] arr;
        private int cur = 0;
        public Instruction(final String input) {
            this.arr = input.toCharArray();
        }

        public Node next(Node current) {
            final Node next = arr[cur++] == 'L' ? current.left : current.right;
            if (cur == arr.length) cur = 0;
            return next;
        }
    }
}