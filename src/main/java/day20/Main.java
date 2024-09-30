package day20;

import com.google.common.io.Resources;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {
    private static final boolean part2 = true;
    public static void main(String[] args) throws Exception {
        final Map<String, Module> input = Resources.readLines(Resources.getResource("day20.txt"), StandardCharsets.UTF_8)
                .stream()
                .map(Module::parse)
                .collect(Collectors.toMap(Module::getName, m -> m));
        input.put("output", new OutputModule());
        input.put("rx", part2 ? new RxModule() : new OutputModule());
        for (final Module m : input.values()) {
            m.destinations = m.destinationNames.stream().map(input::get).toList();
        }

        final Broadcaster bcast = (Broadcaster) input.get("broadcaster");
        bcast.ping(null); // Updates the conjunctions so they know how many sources they have.

        final int max = part2 ? 10000 : 1000;
        IntStream.range(0, max).forEach(i -> bcast.rcv(null, new TickedPulse(i, Pulse.LO)));
        long totHis = input.values().stream().mapToInt(m -> m.hiSent).sum();
        long totLos = input.values().stream().mapToInt(m -> m.loSent).sum() + max; // add the button presses
        System.out.println(totHis + " :: " + totLos + " ::: " + totHis*totLos);

        if (part2) {
            // Part 2: Noticing that rx has one source: jq. Jq will only emit a low signal if its four sources all emit a
            // high signal at the same time. The four sources are vr, nl, gt and lr. Attaching probes:
            long vrPeriod = intervals(((ConjunctionModule) input.get("vr")).hiLog).get(0);
            long nlPeriod = intervals(((ConjunctionModule) input.get("nl")).hiLog).get(0);
            long gtPeriod = intervals(((ConjunctionModule) input.get("gt")).hiLog).get(0);
            long lrPeriod = intervals(((ConjunctionModule) input.get("lr")).hiLog).get(0);
            // This spits out 3907, 4003, 3911 and 3889 on my test data. The LCM of these is the first tick these occur simultaneously
            long product = vrPeriod * nlPeriod * gtPeriod * lrPeriod; // They are all prime. LCM is the product.
            System.out.println("Product is " + product);
        }
    }

    private static List<Integer> intervals(final List<Integer> integers) {
        final List<Integer> intervals = new ArrayList<>(integers.size()-1);
        for (int i = 0; i < integers.size()-1; i++) {
            intervals.add(integers.get(i+1)-integers.get(i));
        }
        return intervals;
    }

    private abstract static class Module {
        protected String name;
        protected List<String> destinationNames;
        protected List<Module> destinations;
        protected Set<Module> sources = new HashSet<>();
        private Queue<TickedPulse> sndQ = new LinkedList<>();
        public int loSent = 0;
        public int hiSent = 0;

        public Module(String name, List<String> destinationNames) {
            this.name = name;
            this.destinationNames = destinationNames;
        }

        public String getName() {
            return name;
        }

        public void ping(Module mod) {
            if (!sources.contains(mod)) {
                sources.add(mod);
                if (destinations.contains(null)) throw new IllegalStateException("Null in network: " + destinationNames + " :: " + destinations);
                destinations.forEach(m -> m.ping(this));
            }
        }

        public abstract void rcv(Module src, TickedPulse pulse);

        public void snd(final TickedPulse pulse) {
            sndQ.add(pulse);
        }

        public void done() {
            destinations.forEach(Module::resume);
        }

        public void resume() {
            boolean did = false;
            while(!sndQ.isEmpty()) {
                final TickedPulse p = sndQ.poll();
                if (p.pulse == Pulse.HI) hiSent += destinations.size();
                else loSent += destinations.size();
                destinations.forEach(d -> d.rcv(this, p));
                did = true;
            }
            if (did) {
                done();
            }
        }

        public static Module parse(final String input) {
            final String[] split = input.split(" -> ");
            final List<String> destNames = Arrays.asList(split[1].split(", "));
            if (split[0].startsWith("broadcaster")) {
                return new Broadcaster(destNames);
            } else if (split[0].startsWith("output")) {
                return new OutputModule();
            } else if (split[0].startsWith("%")) {
                return new FlipFlopModule(split[0].substring(1), destNames);
            } else if (split[0].startsWith("&")) {
                return new ConjunctionModule(split[0].substring(1), destNames);
            }
            throw new IllegalStateException();
        }
    }

    private static class Broadcaster extends Module {

        public Broadcaster(List<String> destinationNames) {
            super("broadcaster", destinationNames);
        }

        @Override
        public void rcv(final Module src, final TickedPulse pulse) {
            snd(pulse);
            resume();
        }
    }

    private static class ConjunctionModule extends Module {
        private Set<String> highMods = new HashSet<>();
        private List<Integer> hiLog = new ArrayList<>();

        public ConjunctionModule(final String name, List<String> destinationNames) {
            super(name, destinationNames);
        }

        @Override
        public void rcv(final Module src, final TickedPulse pulse) {
            //System.out.println("%s -%s-> %s".formatted(src.name, pulse, name));
            if (pulse.pulse == Pulse.LO) highMods.remove(src.name);
            else highMods.add(src.name);
            final boolean sendLo = highMods.size() == sources.size();
            if (!sendLo) hiLog.add(pulse.tick);
            snd(new TickedPulse(pulse.tick, sendLo ? Pulse.LO : Pulse.HI));
        }

        @Override
        public String toString() {
            return "ConjunctionModule[name=%s, mods=%d/%d]".formatted(name, highMods.size(), sources.size());
        }
    }

    private static class FlipFlopModule extends Module {
        private boolean isOn = false;

        public FlipFlopModule(final String name, List<String> destinationNames) {
            super(name, destinationNames);
        }

        @Override
        public void rcv(final Module src, final TickedPulse pulse) {
            //System.out.println("%s -%s-> %s".formatted(src.name, pulse, name));
            boolean doFlip = pulse.pulse == Pulse.LO;
            isOn ^= doFlip;
            if (doFlip) snd(new TickedPulse(pulse.tick, isOn ? Pulse.HI : Pulse.LO));
        }

        @Override
        public String toString() {
            return "FlipFlopModule[name=%s, on=%s]".formatted(name, isOn);
        }
    }

    private static class OutputModule extends Module {
        private long his = 0;
        private long los = 0;

        public OutputModule() {
            super("output", List.of());
        }

        @Override
        public void rcv(final Module src, final TickedPulse pulse) {
            //System.out.println("%s -%s-> %s".formatted(src.name, pulse, name));
            if (pulse.pulse == Pulse.HI) his++;
            else los++;
        }
    }

    private static class RxModule extends Module {

        public RxModule() {
            super("rx", List.of());
        }

        @Override
        public void rcv(final Module src, final TickedPulse pulse) {
            if (pulse.pulse == Pulse.LO) {
                throw new IllegalStateException("TURNING ON!");
            }
        }
    }

    private record TickedPulse(int tick, Pulse pulse) {};
    private enum Pulse { HI, LO}
}