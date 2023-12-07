package day7;

import com.google.common.io.Resources;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

public class Main {

    public static void main(String[] args) throws Exception {
        final List<String> lns = Resources.readLines(Resources.getResource("day7.txt"), StandardCharsets.UTF_8);
        //System.out.println("Final score: " + getScore(lns, false));
        System.out.println("Final score (jokers): " + getScore(lns, true));
    }

    private static long getScore(final List<String> lines, final boolean joker) {
        final List<HandBet> part1 = new ArrayList<>(lines.stream().map(s -> HandBet.parse(s, joker)).toList());
        Collections.sort(part1);
        long finalScore = 0;
        for (int rank = 0; rank < part1.size(); rank++) {
            finalScore += (rank+1) * part1.get(rank).bet;
        }
        return finalScore;
    }

    public record HandBet(Hand hand, int bet) implements Comparable<HandBet> {

        public static HandBet parse(final String str, final boolean joker) {
            final String[] split = str.split(" ");
            return new HandBet(Hand.parse(split[0], joker), Integer.parseInt(split[1]));
        }

        @Override
        public int compareTo(final HandBet o) {
            return hand.compareTo(o.hand);
        }
    }

    public record Hand(char[] cards, boolean joker) implements Comparable<Hand> {
        private static final Map<Character, Integer> CARD_STRENGTH = new HashMap<>();

        static {
            final char[] cardsOrdered = "23456789TJQKA".toCharArray();
            for (int i = 0; i < cardsOrdered.length; i++) {
                CARD_STRENGTH.put(cardsOrdered[i], i+1);
            }
        }

        int typeScore() {
            final SortedMap<Character, Integer> count = new TreeMap<>();
            for (char c : cards) {
                count.compute(c, (ch, i) -> Objects.requireNonNullElse(i, 0)+1);
            }
            int jokers = joker ? count.getOrDefault('J', 0) : 0;
            int pairs = 0;
            int threes = 0;
            int fours = 0;
            int fives = 0;
            for(Map.Entry<Character, Integer> num : count.entrySet()) {
                if (num.getKey() == 'J' && joker) { // Factor them in later.
                    continue;
                }
                switch (num.getValue()) {
                    case 2: pairs++; break;
                    case 3: threes++; break;
                    case 4: fours++; break;
                    case 5: fives++; break;
                }
            }

            // Please forgive me; I couldn't be arsed to find a better way
            if (threes == 1 && jokers == 2) {
                fives++;
                threes--;
                jokers = 0;
            } if (fours == 1 && jokers == 1) {
                fives++;
                fours--;
                jokers = 0;
            } if (threes == 1 && jokers == 1) {
                fours++;
                threes--;
                jokers = 0;
            } if (pairs != 0 && jokers == 3) {
                pairs--;
                fives++;
                jokers = 0;
            } if (pairs != 0 && jokers == 2) {
                pairs--;
                fours++;
                jokers = 0;
            } if (pairs != 0 && jokers == 1) {
                pairs--;
                threes++;
                jokers = 0;
            } if (jokers == 1) {
                pairs++;
                jokers = 0;
            } if (jokers == 2) {
                threes++;
                jokers = 0;
            } if (jokers == 3) {
                fours++;
                jokers = 0;
            } if (jokers >= 4) {
                fives++;
                jokers = 0;
            }

            int score = 0;
            score += fives * 10;
            score += fours * 9;
            score += threes * pairs * 8;
            if (pairs == 0) score += threes * 7;
            if (threes == 0) score += 3 * pairs;
            return score;
        }

        int cardScore() {
            int score = 0;
            for (int i = 0; i < cards.length; i++) {
                int invI = (cards.length - i)+1;
                final char card = cards[i];
                final int cardStrength = joker && card == 'J' ? 0 :  CARD_STRENGTH.get(card);
                score += Math.pow(15, invI)*cardStrength;
            }
            return score;
        }

        public static Hand parse(final String hand, boolean weakJoker) {
            return new Hand(hand.toCharArray(), weakJoker);
        }

        @Override
        public int compareTo(final Hand o) {
            int typeScore = typeScore() - o.typeScore();
            if (typeScore == 0) {
                return cardScore() - o.cardScore();
            }
            return typeScore;
        }

        @Override
        public String toString() {
            return new String(cards) + " ;; " + typeScore();
        }
    }
}