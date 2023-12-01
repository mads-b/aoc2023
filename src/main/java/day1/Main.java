package day1;

import com.google.common.io.Resources;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class Main {

    private static final Map<String, Integer> LITERALS = Map.of(
            "one", 1,
            "two", 2,
            "three", 3,
            "four", 4,
            "five", 5,
            "six", 6,
            "seven", 7,
            "eight", 8,
            "nine", 9);

    public static void main(String[] args) throws Exception {
        final List<String> lines = Resources.readLines(Resources.getResource("day1.txt"), StandardCharsets.UTF_8);

        System.out.println(lines.stream()
                .map(Main::getNum)
                .reduce(Integer::sum)
                .orElse(0));
    }

    private static int getNum(final String str) {
        return getFDig(str) * 10 + getLDig(str);
    }

    private static int getFDig(final String str) {
        for (int i = 0; i < str.length(); i++) {
            final Integer posDig = getPossibleDigitAt(i, str);
            if (posDig != null) {
                return posDig;
            }
        }
        throw new IllegalStateException("No nums in " + str);
    }

    private static int getLDig(final String str) {
        for (int i = str.length()- 1; i >= 0; i--) {
            final Integer posDig = getPossibleDigitAt(i, str);
            if (posDig != null) {
                return posDig;
            }
        }
        throw new IllegalStateException("No nums in " + str);
    }

    private static Integer getPossibleDigitAt(final int idx, final String str) {
        if (Character.isDigit(str.charAt(idx))) return Character.digit(str.charAt(idx), 10);
        for (Map.Entry<String, Integer> lit : LITERALS.entrySet()) {
            if (idx + lit.getKey().length() > str.length()) continue;
            final String test = str.substring(idx, idx + lit.getKey().length());
            if (test.equals(lit.getKey())) {
                return lit.getValue();
            }
        }
        return null;
    }
}