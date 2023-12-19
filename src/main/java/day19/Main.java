package day19;

import com.google.common.io.Resources;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) throws Exception {
        final List<String> input = Resources.readLines(Resources.getResource("day19.txt"), StandardCharsets.UTF_8);
        boolean partMode = false;
        Map<String, Workflow> workflows = new HashMap<>();
        List<Part> parts = new ArrayList<>();
        for (final String line : input) {
            if (line.isBlank()) {
                partMode = true;
                continue;
            }
            if (!partMode) {
                final Workflow wf = Workflow.parse(line);
                workflows.put(wf.name, wf);
            }
            else parts.add(Part.parse(line));
        }
        final Workflow in = workflows.get("in");
        treeify(workflows, in.expression);
        System.out.println("Sum of all fields of accepted parts: " + parts
                .stream()
                .filter(p -> in.expression.getDestination(p).equals("A"))
                .mapToInt(p -> p.x+p.m+p.a+p.s)
                .sum());
        // Since we treeified the expression, computing the total number of possible combinations to get approved
        // should be a matter of traversing the tree
        System.out.println("Possible values: " + traverseValueSpace(
                in.expression,
                new ValueSpace(new Part(1, 1, 1, 1), new Part(4000, 4000, 4000, 4000))));
    }

    private static Expression treeify(final Map<String, Workflow> workflowMap, final Expression expression) {
        if (expression instanceof ConstExpression c) {
            if (Set.of("A", "R").contains(c.dest)) {
                return expression;
            }
            return treeify(workflowMap, workflowMap.get(c.dest).expression);
        }
        if (expression instanceof ConditionalExpression cond) {
            cond.ifTrue = treeify(workflowMap, cond.ifTrue);
            cond.ifFalse = treeify(workflowMap, cond.ifFalse);
            return cond;
        }
        throw new IllegalStateException("Unknown expression type..");
    }

    private static BigInteger traverseValueSpace(final Expression in, final ValueSpace toConstrain) {
        if (in instanceof ConstExpression c) {
            return c.dest.equals("R") ? BigInteger.ZERO : toConstrain.possibleValues();
        }
        if (in instanceof ConditionalExpression cond) {
            final ValueSpace vs1 = toConstrain.constrain(cond, false);
            final ValueSpace vs2 = toConstrain.constrain(cond, true);
            return traverseValueSpace(cond.ifTrue, vs1).add(traverseValueSpace(cond.ifFalse,  vs2));
        }
        throw new IllegalStateException();
    }

    private record ValueSpace(Part min, Part max) {
        public BigInteger possibleValues() {
            return BigInteger.valueOf(max.x - min.x + 1L)
                    .multiply(BigInteger.valueOf(max.m - min.m + 1L))
                    .multiply(BigInteger.valueOf(max.a - min.a + 1L))
                    .multiply(BigInteger.valueOf(max.s - min.s + 1L))
                    .max(BigInteger.ZERO);
        }

        public ValueSpace constrain(final ConditionalExpression conditionalExpression, boolean inverse) {
            final int add = inverse ? 0 : 1;

            if (conditionalExpression.isGt ^ inverse) {
                return switch (conditionalExpression.fieldName) {
                    case 'x' -> new ValueSpace(new Part(Math.max(min.x, conditionalExpression.threshold+add), min.m, min.a, min.s), max);
                    case 'm' -> new ValueSpace(new Part(min.x, Math.max(min.m, conditionalExpression.threshold+add), min.a, min.s), max);
                    case 'a' -> new ValueSpace(new Part(min.x, min.m, Math.max(min.a, conditionalExpression.threshold+add), min.s), max);
                    case 's' -> new ValueSpace(new Part(min.x, min.m, min.a, Math.max(min.s, conditionalExpression.threshold+add)), max);
                    default -> throw new IllegalStateException();
                };
            }
            return switch (conditionalExpression.fieldName) {
                case 'x' -> new ValueSpace(min, new Part(Math.min(max.x, conditionalExpression.threshold-add), max.m, max.a, max.s));
                case 'm' -> new ValueSpace(min, new Part(max.x,Math.min(max.m, conditionalExpression.threshold-add), max.a, max.s));
                case 'a' -> new ValueSpace(min, new Part(max.x, max.m, Math.min(max.a, conditionalExpression.threshold-add), max.s));
                case 's' -> new ValueSpace(min, new Part(max.x, max.m, max.a, Math.min(max.s, conditionalExpression.threshold-add)));
                default -> throw new IllegalStateException();
            };
        }
    }

    private interface Expression {

        static Expression parse(final String val) {
            if (!val.contains(":")) {
                return new ConstExpression(val);
            }
            return ConditionalExpression.parse(val);
        }

        String getDestination(Part part);
    }

    private static record Workflow(String name, Expression expression) {
        private static final Pattern wf = Pattern.compile("(\\w+)\\{(.*)\\}");

        static Workflow parse(final String in) {
            final Matcher matcher = wf.matcher(in);
            matcher.find();
            return new Workflow(matcher.group(1), Expression.parse(matcher.group(2)));
        }
    }

    private record ConstExpression(String dest) implements Expression {
        @Override
        public String getDestination(final Part part) {
            return dest;
        }
    }

    private static class ConditionalExpression implements Expression {

        private static final Pattern clausePat = Pattern.compile("(\\w)[<>](\\d+)");
        private final Predicate<Part> clause;
        private final int threshold;
        private final char fieldName;
        private final boolean isGt;
        private Expression ifTrue;
        private Expression ifFalse;

        private ConditionalExpression(Predicate<Part> clause,
                                      int threshold,
                                      char fieldName,
                                      boolean isGt,
                                      Expression ifTrue,
                                      Expression ifFalse) {
            this.clause = clause;
            this.threshold = threshold;
            this.fieldName = fieldName;
            this.isGt = isGt;
            this.ifTrue = ifTrue;
            this.ifFalse = ifFalse;
        }

        public static Expression parse(final String val) {
            final String[] split = val.split(":", 2);
            Matcher matcher = clausePat.matcher(split[0]);
            matcher.find();
            char fieldName = matcher.group(1).charAt(0);
            int threshold = Integer.parseInt(matcher.group(2));
            boolean isGt = split[0].contains(">");
            Predicate<Part> clause = switch (fieldName) {
                case 'x' -> isGt ? p -> p.x > threshold : p -> p.x < threshold;
                case 'm' -> isGt ? p -> p.m > threshold : p -> p.m < threshold;
                case 'a' -> isGt ? p -> p.a > threshold : p -> p.a < threshold;
                case 's' -> isGt ? p -> p.s > threshold : p -> p.s < threshold;
                default -> throw new IllegalStateException("Unexpected value: " + fieldName);
            };
            final String rest = split[1];
            final String[] restSplit = rest.split(",", 3);
            final Expression trueExp;
            if (restSplit[0].contains("<") || restSplit[0].contains(">")) {
                throw new IllegalStateException("True statement cannot be a conditional expression");
            } else {
                trueExp = Expression.parse(restSplit[0]);
            }
            final Expression falseExp;
            if (restSplit[1].contains("<") || restSplit[1].contains(">")) {
                falseExp = ConditionalExpression.parse(restSplit[1] + "," + restSplit[2]);
            } else {
                falseExp = new ConstExpression(restSplit[1]);
            }
            if (trueExp.equals(falseExp)) return trueExp;
            return new ConditionalExpression(clause, threshold, fieldName, isGt,trueExp, falseExp);
        }

        public String humanReadableClause() {
            return fieldName + (isGt ? ">" : "<") + threshold;
        }

        @Override
        public String toString() {
            return "ConditionalExpression[clause=" + humanReadableClause() + ", ifTrue=" + ifTrue + ", ifFalse=" + ifFalse + "]";
        }

        @Override
        public String getDestination(final Part part) {
            return clause.test(part) ? ifTrue.getDestination(part) : ifFalse.getDestination(part);
        }
    }

    private record Part(int x, int m, int a, int s) {
        private static final Pattern partPat = Pattern.compile("\\{x=(\\d+),m=(\\d+),a=(\\d+),s=(\\d+)}");

        public static Part parse(final String in) {
            final Matcher matcher = partPat.matcher(in);
            matcher.find();
            return new Part(
                    Integer.parseInt(matcher.group(1)),
                    Integer.parseInt(matcher.group(2)),
                    Integer.parseInt(matcher.group(3)),
                    Integer.parseInt(matcher.group(4)));
        }

    }

}