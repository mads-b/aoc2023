package day9;

import com.google.common.io.Resources;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {
        final List<int[]> sequences = Resources.readLines(Resources.getResource("day9.txt"), StandardCharsets.UTF_8)
                .stream().map(str -> str.split(" ")).map(Arrays::stream).map(str -> str.map(Integer::parseInt).mapToInt(i -> i).toArray())
                .toList();

        long sum = 0;
        long reverseSum = 0;
        for (final int[] sequence : sequences) {
            List<int[]> stack = new ArrayList<>();
            stack.add(sequence);
            while(!isZeros(stack.get(stack.size()-1))) {
                stack.add(diffSeq(stack.get(stack.size()-1)));
            }
            sum += extrapolate(stack);
            reverseSum += extrapolateReverse(stack);
        }
        System.out.println("Extrapolated: " + sum);
        System.out.println("Extrapolated in reverse: " + reverseSum);
    }
    
    private static int[] diffSeq(final int[] seq) {
        final int[] diffSeq = new int[seq.length - 1];
        for (int i = 0; i < seq.length - 1; i++) {
            diffSeq[i] = seq[i+1] - seq[i];
        }
        return diffSeq;
    }
    
    private static boolean isZeros(final int[] seq) {
        for (int n : seq) {
            if (n != 0) {
                return false;
            }
        }
        return true;
    }

    private static int extrapolate(final List<int[]> stack) {
        int next = 0;
        for (int i = stack.size()-1; i >= 0; i--) {
            int[] seq = stack.get(i);
            seq[seq.length-1] += next;
            next = seq[seq.length-1];
        }
        return next;
    }

    private static int extrapolateReverse(final List<int[]> stack) {
        int next = 0;
        for (int i = stack.size()-1; i >= 0; i--) {
            int[] seq = stack.get(i);
            seq[0] -= next;
            next = seq[0];
        }
        return next;
    }
}