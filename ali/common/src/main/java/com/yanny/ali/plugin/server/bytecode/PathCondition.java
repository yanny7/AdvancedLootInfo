package com.yanny.ali.plugin.server.bytecode;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

sealed interface PathCondition {
    Always ALWAYS = new Always();

    static PathCondition of(List<List<Decision>> paths) {
        List<Set<Decision>> sets = new ArrayList<>();

        for (List<Decision> path : paths) {
            Set<Decision> set = withoutImplied(path);

            if (!sets.contains(set)) {
                sets.add(set);
            }
        }

        while (mergeOne(sets)) {
            sets = new ArrayList<>(new LinkedHashSet<>(sets));
        }

        if (sets.size() == 1) {
            return allOf(sets.get(0));
        }

        Set<Decision> common = new LinkedHashSet<>(sets.get(0));
        List<PathCondition> alternatives = new ArrayList<>();

        sets.forEach(common::retainAll);

        for (Set<Decision> set : sets) {
            Set<Decision> rest = new LinkedHashSet<>(set);

            rest.removeAll(common);

            PathCondition condition = allOf(rest);

            if (condition == ALWAYS) {
                return allOf(common);
            }
            if (!alternatives.contains(condition)) {
                alternatives.add(condition);
            }
        }

        PathCondition any = alternatives.size() == 1 ? alternatives.get(0) : new AnyOf(alternatives);

        return common.isEmpty() ? any : allOf(common).and(any);
    }

    default PathCondition and(PathCondition other) {
        List<PathCondition> terms = new ArrayList<>();

        for (PathCondition condition : List.of(this, other)) {
            if (condition instanceof AllOf all) {
                terms.addAll(all.terms());
            } else if (condition != ALWAYS) {
                terms.add(condition);
            }
        }

        if (terms.isEmpty()) {
            return ALWAYS;
        }

        return terms.size() == 1 ? terms.get(0) : new AllOf(terms);
    }

    TooltipNode tooltip(IServerUtils utils);

    String text();

    @NotNull
    private static Set<Decision> withoutImplied(List<Decision> path) {
        Set<Decision> set = new LinkedHashSet<>();

        for (Decision decision : path) {
            boolean implied = decision.kind() == Decision.Kind.EQUALS && !decision.holds() && path.stream().anyMatch((d) -> d.kind() == Decision.Kind.EQUALS
                    && d.holds() && Objects.equals(d.subject(), decision.subject()));

            if (!implied) {
                set.add(decision);
            }
        }

        return set;
    }

    private static boolean mergeOne(List<Set<Decision>> sets) {
        for (int i = 0; i < sets.size(); i++) {
            for (int j = i + 1; j < sets.size(); j++) {
                Set<Decision> a = sets.get(i);
                Set<Decision> b = sets.get(j);

                for (Decision decision : a) {
                    if (decision.kind() == Decision.Kind.UNKNOWN && decision.subject() == null) {
                        continue;
                    }

                    Set<Decision> rest = new LinkedHashSet<>(a);
                    Set<Decision> other = new LinkedHashSet<>(b);

                    rest.remove(decision);

                    if (other.remove(decision.negated()) && rest.equals(other)) {
                        sets.set(i, rest);
                        sets.remove(j);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private static PathCondition allOf(Set<Decision> set) {
        List<PathCondition> terms = new ArrayList<>();
        Map<Object, List<Object>> excluded = new LinkedHashMap<>();
        boolean random = false;
        boolean unknown = false;

        for (Decision decision : set) {
            if (decision.kind() == Decision.Kind.RANDOM) {
                random = true;
                continue;
            }
            if (decision.kind() == Decision.Kind.UNKNOWN) {
                unknown = true;
                continue;
            }

            Object value = Objects.requireNonNull(decision.value());

            if (decision.holds()) {
                terms.add(new Is(value));
            } else if (decision.kind() == Decision.Kind.EQUALS) {
                excluded.computeIfAbsent(decision.subject(), (s) -> new ArrayList<>()).add(value);
            } else {
                terms.add(new Not(new Is(value)));
            }
        }

        for (List<Object> values : excluded.values()) {
            List<PathCondition> options = values.stream().<PathCondition>map(Is::new).sorted(Comparator.comparing(PathCondition::text)).toList();

            terms.add(new Not(options.size() == 1 ? options.get(0) : new AnyOf(options)));
        }

        if (random) {
            terms.add(new RandomChance());
        }
        if (unknown) {
            terms.add(new UnknownPredicate());
        }
        if (terms.isEmpty()) {
            return ALWAYS;
        }

        return terms.size() == 1 ? terms.get(0) : new AllOf(terms);
    }

    record Always() implements PathCondition {
        @NotNull
        @Override
        public TooltipNode tooltip(IServerUtils utils) {
            return TooltipNode.empty();
        }

        @NotNull
        @Override
        public String text() {
            return "always";
        }
    }

    record Is(Object value) implements PathCondition {
        @Override
        public TooltipNode tooltip(IServerUtils utils) {
            return utils.getValueTooltip(utils, shown(value)).build(label(value));
        }

        @NotNull
        @Override
        public String text() {
            return label(value).getTranslation().sEng().replace("%s", String.valueOf(shown(value)));
        }

        @Nullable
        private static ResourceLocation registry(Object value) {
            if (value instanceof TagKey<?> tag) {
                return tag.registry().location();
            }
            if (value instanceof Holder<?> holder) {
                return holder.unwrapKey().map(ResourceKey::registry).orElse(null);
            }

            ResourceKey<?> key = value instanceof ResourceKey<?> resourceKey ? resourceKey : builtInKey(value);

            return key == null ? null : key.registry();
        }

        private static Object shown(Object value) {
            if (value instanceof ResourceKey<?> || value instanceof TagKey<?> || value instanceof Holder<?>) {
                return value;
            }

            ResourceKey<?> key = builtInKey(value);

            return key != null ? key : value;
        }

        @SuppressWarnings("unchecked")
        @Nullable
        private static ResourceKey<?> builtInKey(Object value) {
            for (Registry<?> registry : BuiltInRegistries.REGISTRY) {
                Optional<? extends ResourceKey<?>> key = ((Registry<Object>) registry).getResourceKey(value);

                if (key.isPresent()) {
                    return key.get();
                }
            }

            return null;
        }

        private static Lang.Value label(Object value) {
            ResourceLocation registry = registry(value);

            if (registry != null) {
                String path = registry.getPath();
                String key = "ali.property.value." + path.substring(path.lastIndexOf('/') + 1);

                for (Lang.Value label : Lang.Value.values()) {
                    if (label.getTranslation().sKey().equals(key)) {
                        return label;
                    }
                }
            }

            return Lang.Value.VALUE;
        }
    }

    record Not(PathCondition term) implements PathCondition {
        @Override
        public TooltipNode tooltip(IServerUtils utils) {
            return TooltipBuilder.array((b) -> b.add(term.tooltip(utils)), Lang.Conditions.INVERTED).build();
        }

        @NotNull
        @Override
        public String text() {
            return "not " + term.text();
        }
    }

    record AllOf(List<PathCondition> terms) implements PathCondition {
        @Override
        public TooltipNode tooltip(IServerUtils utils) {
            return TooltipBuilder.array((b) -> terms.forEach((t) -> b.add(t.tooltip(utils))), Lang.Conditions.ALL_OF).build();
        }

        @Override
        public String text() {
            return terms.stream().map(PathCondition::text).collect(Collectors.joining(" and ", "(", ")"));
        }
    }

    record AnyOf(List<PathCondition> terms) implements PathCondition {
        @Override
        public TooltipNode tooltip(IServerUtils utils) {
            return TooltipBuilder.array((b) -> terms.forEach((t) -> b.add(t.tooltip(utils))), Lang.Conditions.ANY_OF).build();
        }

        @Override
        public String text() {
            return terms.stream().map(PathCondition::text).collect(Collectors.joining(" or ", "(", ")"));
        }
    }

    record RandomChance() implements PathCondition {
        @Override
        public TooltipNode tooltip(IServerUtils utils) {
            return TooltipBuilder.keyOnly(Lang.Description.RANDOM_CHANCE).build();
        }

        @Override
        public String text() {
            return Lang.Description.RANDOM_CHANCE.getTranslation().sEng();
        }
    }

    record UnknownPredicate() implements PathCondition {
        @Override
        public TooltipNode tooltip(IServerUtils utils) {
            return TooltipBuilder.error(TooltipBuilder.translate(Lang.Description.UNKNOWN_PREDICATE.singular())).build();
        }

        @Override
        public String text() {
            return Lang.Description.UNKNOWN_PREDICATE.getTranslation().sEng();
        }
    }
}
