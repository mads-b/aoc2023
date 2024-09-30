package day25;

import com.google.common.io.Resources;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) throws Exception {
        final Map<String, Component> comps = new HashMap<>();
        final List<String> input = Resources.readLines(Resources.getResource("day25.txt"), StandardCharsets.UTF_8);
        for (final String line : input) {
            var split = line.split(": ");
            var node = comps.computeIfAbsent(split[0], (key) -> new Component(split[0]));
            Arrays.stream(split[1].split(" ")).forEach(comp -> {
                var neigh = comps.computeIfAbsent(comp, (key) -> new Component(comp));
                neigh.connections.add(node);
                node.connections.add(neigh);
            });
        }

        final Random rand = new Random();

        while (true) {
            final List<Component> nodes = new ArrayList<>(clone(comps.values()));
            // Strategy is: merge the node with the most connections with the neighbor that has the most connections.
            while (nodes.size() != 2) {
                var randomNode = nodes.remove(rand.nextInt(nodes.size()));
                var randomNeighbor = randomNode.getConnections().get(rand.nextInt(randomNode.getConnections().size()));

                nodes.remove(randomNode);
                nodes.remove(randomNeighbor);

                final UnionComponent union = new UnionComponent(randomNode, randomNeighbor);
                nodes.add(union);

                for (final Component unionNeighs : union.getConnections()) {
                    unionNeighs.replace(randomNode, union);
                    unionNeighs.replace(randomNeighbor, union);
                }
            }
            if (nodes.get(0).getConnections().size() == 3) {
                final UnionComponent one = (UnionComponent) nodes.get(0);
                final UnionComponent two = (UnionComponent) nodes.get(1);
                System.out.println("Group sizes multiplied: " + (one.subComponents.size() * two.subComponents.size()));
                break;
            }
        }
    }

    public static Collection<Component> clone(final Collection<Component> list) {
        final Map<String, Component> fresh = HashMap.newHashMap(list.size());
        for (final Component old : list) {
            fresh.put(old.name, new Component(old.name));
        }
        for (final Component old : list) {
            old.connections.forEach(conn -> fresh.get(old.name).connections.add(fresh.get(conn.name)));
        }
        return fresh.values();
    }


    private static class UnionComponent extends Component {

        private final Set<Component> subComponents;

        public UnionComponent(final Component union1, final Component union2) {
            super(union1.name + "|" + union2.name);
            this.subComponents = Stream.concat(union1 instanceof UnionComponent u1 ? u1.subComponents.stream() : Stream.of(union1), union2 instanceof UnionComponent u2 ? u2.subComponents.stream() : Stream.of(union2)).collect(Collectors.toSet());
            this.connections = new ArrayList<>(Stream.concat(union1.getConnections().stream().filter(conn -> conn != union2), union2.getConnections().stream().filter(conn -> conn != union1)) // No self-connections
                    .toList());
        }

        @Override
        public String toString() {
            return "[UnionComp name=%s neighs=%s]".formatted(name, connections.stream().map(c -> c.name).toList());
        }
    }

    private static class Component {
        protected final String name;
        protected List<Component> connections = new ArrayList<>();

        public Component(final String name) {
            this.name = name;
        }

        public List<Component> getConnections() {
            return connections;
        }

        public void replace(final Component rm, final Component add) {
            for (int i = 0; i < connections.size(); i++) {
                var conn = connections.get(i);
                if (conn.name.equals(rm.name)) {
                    connections.set(i, add);
                }
            }
        }

        @Override
        public String toString() {
            return "[Comp name=%s neighs=%s]".formatted(name, connections.stream().map(c -> c.name).toList());
        }
    }
}