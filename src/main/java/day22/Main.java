package day22;

import com.google.common.collect.Sets;
import com.google.common.io.Resources;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) throws Exception {
        final List<Block> input = new ArrayList<>(Resources.readLines(Resources.getResource("day22-test.txt"), StandardCharsets.UTF_8)
                .stream().map(Block::parse).toList());
        Collections.sort(input, (b1, b2) -> Math.min(b1.start.z, b1.end.z) - Math.min(b2.start.z, b2.start.z));
        System.out.println(input);

        int[][] zLevels = new int[10][10];
        Arrays.stream(zLevels).forEach(arr -> Arrays.fill(arr, 1));
        Block[][][] blockPositions = new Block[10][10][150];
        final Map<Block, Set<Block>> blocksAndTheirSupports = new HashMap<>();

        for (final Block b : input) {
            int minZ = IntStream.range(b.start.x, b.end.x+1).map(x -> IntStream.range(b.start.y, b.end.y+1).map(y -> zLevels[x][y]).max().orElse(0)).max().orElse(0);
            int height = b.end.z - b.start.z;
            final Block restingBlock = new Block(new Point(b.start.x, b.start.y, minZ), new Point(b.end.x, b.end.y, minZ+height));
            final Set<Block> supports = new HashSet<>();
            IntStream.range(b.start.x, b.end.x+1).forEach(x -> IntStream.range(b.start.y, b.end.y+1).forEach(y -> {
                zLevels[x][y] = minZ + height + 1;
                final Block blockUnderHere = blockPositions[x][y][minZ-1];
                if (blockUnderHere != null) {
                    supports.add(blockUnderHere);
                }
                IntStream.range(restingBlock.start.z, restingBlock.end.z+1).forEach(z -> blockPositions[x][y][z] = restingBlock);
            }));
            blocksAndTheirSupports.put(restingBlock, supports);
        }
        final Map<Block, Set<Block>> blocksAndWhatTheySupport = new HashMap<>();
        for (Block b : blocksAndTheirSupports.keySet()) {
            final Set<Block> blocksSupportedByBlock = IntStream.range(b.start.x, b.end.x + 1)
                    .mapToObj(x -> IntStream.range(b.start.y, b.end.y + 1)
                            .mapToObj(y -> blockPositions[x][y][b.end.z + 1])
                            .filter(Objects::nonNull)).reduce(Stream.of(), Stream::concat)
                    .collect(Collectors.toSet());
            blocksAndWhatTheySupport.put(b, blocksSupportedByBlock);
        }


        System.out.println("Supports of blocks: " + blocksAndTheirSupports);
        System.out.println("What the blocks support " + blocksAndWhatTheySupport);
        final long unZappable = blocksAndTheirSupports.values()
                .stream()
                .filter(blocks -> blocks.size() == 1)
                .map(blocks -> blocks.iterator().next()).distinct().count();
        System.out.println("Can be zapped: " + (blocksAndTheirSupports.size() - unZappable));

        long chainReactionCount = 0;
        for (Block b : blocksAndTheirSupports.keySet()) {
            chainReactionCount += countChainReaction(Set.of(b), blocksAndTheirSupports, blocksAndWhatTheySupport)-1;
            System.out.println("Zapping " + b + " gives a chain reaction of " + countChainReaction(Set.of(b), blocksAndTheirSupports, blocksAndWhatTheySupport));
        }
        System.out.println("Total chain reaction sum: " + chainReactionCount);
    }

    // This is ultra-slow, but it really didn't have to be fast..
    private static int countChainReaction(
            final Set<Block> blocksToDisintegrate,
            final Map<Block, Set<Block>> blocksAndTheirSupports,
            final Map<Block, Set<Block>> blocksAndWhatTheySupport) {
        final Set<Block> unsupported = blocksToDisintegrate.stream()
                .flatMap(d -> blocksAndWhatTheySupport.get(d).stream().filter(i -> !blocksToDisintegrate.contains(i)))
                .filter(u -> Sets.difference(blocksAndTheirSupports.get(u), blocksToDisintegrate).isEmpty())
                .collect(Collectors.toSet());
        if (unsupported.isEmpty()) {
            return blocksToDisintegrate.size();
        }
        return countChainReaction(Sets.union(unsupported, blocksToDisintegrate), blocksAndTheirSupports, blocksAndWhatTheySupport);
    }


    private record Block(Point start, Point end) {

        public boolean contains(final Point p) {
            return start.x <= p.x && end.x >= p.x
                    && start.y <= p.y && end.y >= p.y
                    && start.z <= p.z && end.z >= p.z;
        }

        public static Block parse(final String str) {
            final String[] split = str.split("~");
            return new Block(Point.parse(split[0]), Point.parse(split[1]));
        }

        public String toString() {
            return "%d,%d,%d~%d,%d,%d".formatted(start.x, start.y, start.z, end.x, end.y, end.z);
        }
    }

    private record Point(int x, int y, int z) {

        public static Point parse(final String str) {
            List<Integer> xyz = Arrays.stream(str.split(",")).map(Integer::parseInt).toList();
            return new Point(xyz.get(0), xyz.get(1), xyz.get(2));
        }
    }

    private static Stream<Point> loopAll(int[][] input) {
        return IntStream.range(0, input.length).mapToObj(y -> IntStream.range(0, input[y].length).mapToObj(x -> new Point(x, y, input[y][x]))).flatMap(s -> s);
    }
}