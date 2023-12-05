package day5;

import com.google.common.collect.Range;
import com.google.common.io.Resources;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class Main {

    public static void main(String[] args) throws Exception {
        final List<String> board = Resources.readLines(Resources.getResource("day5.txt"), StandardCharsets.UTF_8);
        final List<Long> seeds = parseLine(board.get(0).substring(7));
        final Iterator<String> listIt = board.listIterator();
        listIt.next();

        final Map<String, Mapper> mappers = new HashMap<>();
        List<Mapping> mappingsForSubject = new ArrayList<>();
        String subject = null;
        while (listIt.hasNext()) {
            final String line = listIt.next();
            if (line.isEmpty()) {
                if (!mappingsForSubject.isEmpty()) {
                    mappers.put(subject.substring(0, subject.length() - 5), new Mapper(mappingsForSubject));
                }
                mappingsForSubject = new ArrayList<>();
                subject = listIt.hasNext() ? listIt.next() : null;
                continue;
            }
            mappingsForSubject.add(Mapping.parse(line));
        }

        long lowest = Long.MAX_VALUE;
        for (final long seed : seeds) {
            lowest = Math.min(lowest, getLocations(mappers, Range.closed(seed, seed))
                    .stream()
                    .map(Range::lowerEndpoint)
                    .min(Long::compare)
                    .orElse(0L));
        }
        System.out.println("Lowest location number is " + lowest);

        lowest = Long.MAX_VALUE;
        for (int i = 0; i < seeds.size(); i+=2) {
            long seed = seeds.get(i);
            long len = seeds.get(i+1);
            final Range<Long> input = Range.closed(seed, seed+len-1);
            final long output = getLocations(mappers, input).stream()
                    .map(Range::lowerEndpoint)
                    .min(Long::compare)
                    .orElse(0L);
            lowest = Math.min(lowest, output);
        }
        System.out.println("Lowest location number^2 is " + lowest);
    }

    private static List<Range<Long>> getLocations(final Map<String, Mapper> mappers, Range<Long> seed) {
        List<Range<Long>> soil = mappers.get("seed-to-soil").map(seed);
        List<Range<Long>> fertilizer = mappers.get("soil-to-fertilizer").map(soil);
        List<Range<Long>> water = mappers.get("fertilizer-to-water").map(fertilizer);
        List<Range<Long>> light = mappers.get("water-to-light").map(water);
        List<Range<Long>> temperature = mappers.get("light-to-temperature").map(light);
        List<Range<Long>> humidity = mappers.get("temperature-to-humidity").map(temperature);
        return mappers.get("humidity-to-location").map(humidity);
    }

    private static List<Long> parseLine(final String line) {
        return Arrays.stream(line.split(" ")).map(String::trim).map(Long::parseLong).toList();
    }

    private static class Mapper {
        private final SortedSet<Mapping> ms = new TreeSet<>((o1, o2) -> {
            long diff = o1.src - o2.src;
            int one = diff < 0 ? -1 : 1;
            return diff == 0 ? 0 : one;
        });

        public Mapper(final List<Mapping> mappings) {
            ms.addAll(mappings);
        }

        public List<Range<Long>> map(final List<Range<Long>> ranges) {
            final List<Range<Long>> output = new ArrayList<>(ranges.size());
            for (Range<Long> range : ranges) {
                output.addAll(map(range));
            }
            return output;
        }

        public List<Range<Long>> map(final Range<Long> range) {
            final List<Range<Long>> output = new ArrayList<>();
            long lower = range.lowerEndpoint();
            long upper = range.upperEndpoint();
            for (Mapping m : ms) {
                if (upper < m.src) {
                    return List.of(range);
                }
                if (lower < m.src) {
                    output.add(Range.closed(lower, m.src - 1));
                    lower = m.src;
                }
                if (lower > m.srcEndInc()) {
                    continue;
                }
                if (upper <= m.srcEndInc()) {
                    output.add(Range.closed(m.map(lower), m.map(upper)));
                    return output;
                } else {
                    output.add(Range.closed(m.map(lower), m.map(m.srcEndInc())));
                    lower = m.srcEndInc() + 1;
                }
            }
            if (lower <= upper) {
                output.add(Range.closed(lower, upper));
            }
            return output;
        }
    }

    private record Mapping(long dest, long src, long length) {

        public static Mapping parse(final String line) {
            final List<Long> nums = parseLine(line);
            return new Mapping(nums.get(0), nums.get(1), nums.get(2));
        }

        public long map(long num) {
            return dest + (num - src);
        }

        public long srcEndInc() {
            return src + length - 1;
        }
    }
}