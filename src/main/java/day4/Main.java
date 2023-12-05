package day4;

import com.google.common.collect.Sets;
import com.google.common.io.Resources;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws Exception {
        final List<Nums> board = Resources.readLines(Resources.getResource("day4.txt"), StandardCharsets.UTF_8)
                .stream()
                .map(Nums::fromString)
                .toList();
        System.out.println("Cards are worth " + board.stream().map(Nums::getPointValue).reduce(0, Integer::sum));
        final int[] cardNum = new int[board.size()];
        for (int i = 0; i < board.size(); i++) {
            cardNum[i] = 1;
        }

        for (int i = 0; i < board.size(); i++) {
            final int score = board.get(i).getScore();
            for (int j = 0; j < score; j++) {
                cardNum[i+j+1] += cardNum[i];
            }
        }
        System.out.println(Arrays.toString(cardNum));
        System.out.println("Total scratch cards " + Arrays.stream(cardNum).reduce(0, Integer::sum));
    }

    private record Nums(Set<Integer> winningNums, Set<Integer> myNums) {

        public int getScore() {
            return Sets.intersection(winningNums, myNums).size();
        }

        public int getPointValue() {
            return (int) Math.pow(2, getScore()-1);
        }

        public static Nums fromString(final String str) {
            final String[] split = str.split("\\|");
            System.out.println(str);
            final Set<Integer> winning = Arrays.stream(split[0].split(":")[1].split(" "))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .map(Integer::parseInt)
                    .collect(Collectors.toSet());
            final Set<Integer> mine = Arrays.stream(split[1].split(" "))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .map(Integer::parseInt)
                    .collect(Collectors.toSet());
            return new Nums(winning, mine);
        }
    }
}