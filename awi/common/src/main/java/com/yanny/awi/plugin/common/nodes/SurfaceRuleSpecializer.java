package com.yanny.awi.plugin.common.nodes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.yanny.aci.CommonLogUtils;
import com.yanny.awi.Utils;
import com.yanny.awi.api.ISurfaceRuleHandler;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.material.condition.MaterialCondition;
import net.minecraft.world.level.levelgen.material.rule.MaterialRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;

/**
 * Rewrites a dimension's surface rule into the subset that can fire for one specific biome. One instance per dimension
 * (the encode and the {@link RegistryOps} are done once and reused for every biome of that dimension).
 * <p>
 * The rewrite goes through {@link MaterialRule#CODEC} rather than over the object graph: the node classes
 * ({@code SequenceRule}, {@code ConditionRule}, {@code BiomeCondition}) are records whose field and method names remap
 * in production. Serialized form is keyed by registry ids, which do not.
 * <p>
 * A rule or condition held in the {@code worldgen/material_rule} / {@code worldgen/material_condition} registry
 * serializes as a bare id, so a branch worth pruning can sit behind a reference. References in a known rule or
 * condition position are therefore resolved, pruned, and inlined - but only when pruning actually changed them, so an
 * untouched reference stays a reference.
 * <p>
 * <b>Unknown node types are descended into but never restructured.</b> Inside an unknown node a dead branch is
 * replaced <i>in place</i> with an empty {@code minecraft:sequence} (a valid rule that never fires) instead of being
 * removed, so arity and element order stay intact whether the wrapper indexes its children or pairs them with data of
 * its own. Elements are only really dropped inside a {@code minecraft:sequence}, whose semantics are known.
 * <p>
 * Before any pruning, every node of a rule type AWI has a {@link ISurfaceRuleHandler} for is replaced by a ghost block, once
 * per dimension; the walk measures where the ghost lands and the handler expands that into what the rule really places.
 * A rule that does not always place would shadow every rule after it as a ghost, so it becomes a marker that never fires
 * instead; pruning alone tells whether it applies to a biome, and its handler is given the whole height of the dimension.
 * Every {@code minecraft:noise_threshold} and {@code minecraft:hole} gate is replaced by a coin that is true for about
 * half of all blocks.
 * <p>
 * Any failure falls back to the rule with its ghosts (or to the original rule, if replacing failed), and a dimension
 * whose first biome prunes nothing turns pruning off for itself.
 */
public class SurfaceRuleSpecializer {
    private static final Logger LOGGER = CommonLogUtils.getLogger(Utils.MOD_ID);
    private static final Set<MaterialRule> LOGGED_RULES = Collections.newSetFromMap(new IdentityHashMap<>());

    private static final String TYPE = "type";
    private static final String SEQUENCE = "minecraft:sequence";
    private static final String CONDITION = "minecraft:condition";
    private static final String BIOME = "minecraft:biome";
    private static final String NOT = "minecraft:not";
    private static final String SEQUENCE_FIELD = "sequence";
    private static final String IF_TRUE_FIELD = "if_true";
    private static final String THEN_RUN_FIELD = "then_run";
    private static final String BIOME_IS_FIELD = "biome_is";
    private static final String TAG_PREFIX = "#";
    private static final String INVERT_FIELD = "invert";

    private static final String BLOCK = "minecraft:block";
    private static final String RESULT_STATE_FIELD = "result_state";
    private static final String NAME_FIELD = "Name";
    private static final String GHOST_BLOCK = "minecraft:light";
    private static final int GHOST_COUNT = LightBlock.MAX_LEVEL + 1;
    private static final String NOISE_THRESHOLD = "minecraft:noise_threshold";
    private static final String HOLE = "minecraft:hole";
    private static final String VERTICAL_GRADIENT = "minecraft:vertical_gradient";
    private static final int COIN_ANCHOR = 2000;
    private static final String Y_ABOVE = "minecraft:y_above";

    private final MaterialRule original;
    private final DynamicOps<JsonElement> ops;
    @Nullable
    private final JsonElement base;
    private final MaterialRule baseRule;
    private final List<Ghost> ghosts;
    private final List<Ghost> markers;
    private final Map<Holder<Biome>, List<Ghost>> markersByBiome = new HashMap<>();
    private final boolean logStatistics;

    private boolean pruningEffective = true;

    public record Ghost(BlockState state, ISurfaceRuleHandler handler, JsonObject definition) {}

    /**
     * @param codecLookup the provider that <i>owns</i> the holders the rule references — a different provider holding
     *                    equal values still fails the codec's ownership check and turns specialization off. In game
     *                    that is the level's {@code RegistryAccess}.
     */
    public SurfaceRuleSpecializer(MaterialRule original, HolderLookup.Provider codecLookup,
                                  Map<String, ISurfaceRuleHandler> handlers, boolean logStatistics) {
        JsonElement json = null;
        // Not RegistryOps.create(ops, provider): that adapter derives each registry's HolderOwner from the lookup it
        // hands out, while a provider may serialize under a different owner than it looks up under (a datapack-registry
        // set built by RegistrySetBuilder does exactly that). Asking the provider keeps the two consistent.
        DynamicOps<JsonElement> dynamicOps = codecLookup.createSerializationContext(JsonOps.INSTANCE);
        List<Ghost> replaced = new ArrayList<>();
        MaterialRule rule = original;

        try {
            json = MaterialRule.CODEC.encodeStart(dynamicOps, original).getOrThrow();
        } catch (Throwable t) {
            LOGGER.warn("Could not encode the surface rule, per-biome specialization is off for this dimension", t);
        }

        if (json != null) {
            boolean ghostsAllowed = !handlers.isEmpty() && !placesBlock(json, GHOST_BLOCK);

            if (!handlers.isEmpty() && !ghostsAllowed) {
                LOGGER.warn("The surface rule places {} itself, rules AWI knows are measured instead", GHOST_BLOCK);
            }

            try {
                Rewrite rewrite = new Rewrite(dynamicOps, ghostsAllowed ? handlers : Map.of(), replaced);
                JsonElement substituted = rewrite.apply(json, Position.RULE);

                if (!replaced.isEmpty() || rewrite.coins > 0) {
                    rule = MaterialRule.CODEC.parse(dynamicOps, substituted).getOrThrow();
                    json = substituted;
                }
            } catch (Throwable t) {
                LOGGER.warn("Could not rewrite the surface rule for scanning, it is scanned unchanged", t);
                replaced.clear();
            }
        }

        this.original = original;
        this.ops = dynamicOps;
        this.base = json;
        this.baseRule = rule;
        this.ghosts = List.copyOf(replaced);
        this.markers = replaced.stream().filter((ghost) -> !ghost.handler().alwaysPlaces()).toList();
        this.logStatistics = logStatistics;
    }

    @NotNull
    public MaterialRule baseRule() {
        return baseRule;
    }

    @NotNull
    public List<Ghost> ghosts() {
        return ghosts;
    }

    @NotNull
    public List<Ghost> markers() {
        return markers;
    }

    @NotNull
    public List<Ghost> markers(Holder<Biome> biome) {
        return markersByBiome.getOrDefault(biome, markers);
    }

    /** The base rule with every branch that cannot fire for {@code biome} removed, or the base rule if none can be. */
    @NotNull
    public MaterialRule specialize(Holder<Biome> biome) {
        Optional<ResourceKey<Biome>> key = biome.unwrapKey();

        if (!pruningEffective || base == null || key.isEmpty()) {
            return baseRule;
        }

        try {
            JsonElement pruned = prune(base, key.get().identifier().toString(), false, true, new HashMap<>());

            if (pruned == null || pruned.equals(base)) {
                // Nothing in this rule is decidable per biome.
                pruningEffective = false;
                log("not specializable, no biome-gated branch could be pruned", base, null);

                return baseRule;
            }

            MaterialRule result = MaterialRule.CODEC.parse(ops, pruned).getOrThrow();

            markersByBiome.put(biome, markers.stream().filter((marker) -> contains(pruned, encode(marker.state()))).toList());
            log("specialized", base, pruned);

            return result;
        } catch (Throwable t) {
            pruningEffective = false;
            LOGGER.warn("Could not specialize the surface rule per biome, using it unchanged for this dimension", t);

            return baseRule;
        }
    }

    public static void clearLoggedRules() {
        synchronized (LOGGED_RULES) {
            LOGGED_RULES.clear();
        }
    }

    /** Takes the trees rather than their sizes: counting them walks the whole JSON, which must not happen when off. */
    private void log(String what, JsonElement before, @Nullable JsonElement after) {
        if (!logStatistics) {
            return;
        }

        synchronized (LOGGED_RULES) {
            if (!LOGGED_RULES.add(original)) {
                return;
            }
        }

        int nodesBefore = nodeCount(before);

        if (after == null) {
            LOGGER.info("Surface rule {} ({} nodes)", what, nodesBefore);
            return;
        }

        int nodesAfter = nodeCount(after);

        // A resolved reference is one node before and its whole body after, so the serialized form can grow while the
        // compiled rule still shrinks - reporting a "% removed" for that would read as a regression it is not.
        if (nodesAfter > nodesBefore) {
            LOGGER.info("Surface rule {}: {} -> {} nodes (references inlined)", what, nodesBefore, nodesAfter);
        } else {
            LOGGER.info("Surface rule {}: {} -> {} nodes ({}% removed)", what, nodesBefore, nodesAfter,
                    100 - (nodesAfter * 100 / Math.max(1, nodesBefore)));
        }
    }

    /**
     * Returns the rule with branches that cannot fire for {@code biomeId} pruned. {@code canDrop} says whether the
     * caller may cope with the node disappearing entirely (only a {@code minecraft:sequence} can); everywhere else a
     * dead branch is replaced in place by an empty sequence, which never fires but keeps the structure intact.
     * {@code isRule} marks the positions known to hold a {@link MaterialRule}, the only ones where a bare id may be
     * resolved into the rule it references.
     */
    @Nullable
    private JsonElement prune(JsonElement element, String biomeId, boolean canDrop, boolean isRule, Map<String, JsonElement> resolved) {
        if (element.isJsonArray()) {
            JsonArray pruned = new JsonArray();

            for (JsonElement child : element.getAsJsonArray()) {
                pruned.add(prune(child, biomeId, false, false, resolved));
            }

            return pruned;
        }

        if (isRule && element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            return pruneReferencedRule(element, biomeId, resolved);
        }

        if (!element.isJsonObject()) {
            return element;
        }

        JsonObject object = element.getAsJsonObject();
        String type = typeOf(object);

        if (SEQUENCE.equals(type) && object.has(SEQUENCE_FIELD) && object.get(SEQUENCE_FIELD).isJsonArray()) {
            JsonArray kept = new JsonArray();

            for (JsonElement child : object.getAsJsonArray(SEQUENCE_FIELD)) {
                JsonElement prunedChild = prune(child, biomeId, true, true, resolved);

                if (prunedChild != null) {
                    kept.add(prunedChild);
                }
            }

            if (kept.isEmpty()) {
                return canDrop ? null : emptySequence();
            }

            if (kept.size() == 1) {
                return kept.get(0);
            }

            return sequence(kept);
        }

        if (CONDITION.equals(type) && object.has(IF_TRUE_FIELD) && object.has(THEN_RUN_FIELD)) {
            JsonElement condition = object.get(IF_TRUE_FIELD);
            Boolean matches = biomeVerdict(resolveCondition(condition), biomeId);

            if (Boolean.FALSE.equals(matches)) {
                return canDrop ? null : emptySequence();
            }

            JsonElement thenRun = prune(object.get(THEN_RUN_FIELD), biomeId, false, true, resolved);

            if (isEmptySequence(thenRun)) {
                return canDrop ? null : emptySequence();
            }

            if (Boolean.TRUE.equals(matches)) {
                return thenRun;
            }

            JsonObject test = new JsonObject();

            test.addProperty(TYPE, CONDITION);
            test.add(IF_TRUE_FIELD, condition);
            test.add(THEN_RUN_FIELD, thenRun);

            return test;
        }

        // Unknown node (a mod's own rule type, or a condition): descend into it, but keep its shape exactly.
        JsonObject copy = new JsonObject();

        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            copy.add(entry.getKey(), prune(entry.getValue(), biomeId, false, false, resolved));
        }

        return copy;
    }

    /**
     * Resolves a condition given as a bare registry id into its serialized form; anything else is handed back as is.
     * Only shapes {@link #biomeVerdict} can decide matter, so a failure to resolve is simply "undecidable".
     */
    @Nullable
    private JsonElement resolveCondition(@Nullable JsonElement condition) {
        if (condition == null || !condition.isJsonPrimitive() || !condition.getAsJsonPrimitive().isString()) {
            return condition;
        }

        try {
            MaterialCondition parsed = MaterialCondition.CODEC.parse(ops, condition).getOrThrow();

            if (parsed instanceof MaterialCondition.HolderHolder reference) {
                return MaterialCondition.CODEC.encodeStart(ops, reference.holder().value()).getOrThrow();
            }
        } catch (Throwable t) {
            return null;
        }

        return null;
    }

    /**
     * Prunes the rule a bare registry id refers to, inlining the result only when pruning changed it - an untouched
     * reference stays a reference, which keeps the rule from being expanded for nothing.
     */
    @NotNull
    private JsonElement pruneReferencedRule(JsonElement reference, String biomeId, Map<String, JsonElement> resolved) {
        String id = reference.getAsString();
        JsonElement cached = resolved.get(id);

        if (cached != null) {
            return cached;
        }

        JsonElement inlined;

        try {
            MaterialRule parsed = MaterialRule.CODEC.parse(ops, reference).getOrThrow();

            if (!(parsed instanceof MaterialRule.HolderHolder holder)) {
                return reference;
            }

            inlined = MaterialRule.CODEC.encodeStart(ops, holder.holder().value()).getOrThrow();
        } catch (Throwable t) {
            return reference;
        }

        JsonElement pruned = prune(inlined, biomeId, false, false, resolved);
        JsonElement result = pruned == null || pruned.equals(inlined) ? reference : pruned;

        resolved.put(id, result);

        return result;
    }

    /** {@code TRUE}/{@code FALSE} when the condition is a biome test that always/never matches, {@code null} otherwise. */
    @Nullable
    private static Boolean biomeVerdict(@Nullable JsonElement condition, String biomeId) {
        if (condition == null || !condition.isJsonObject()) {
            return null;
        }

        JsonObject object = condition.getAsJsonObject();
        String type = typeOf(object);

        if (BIOME.equals(type) && object.has(BIOME_IS_FIELD)) {
            return biomeSetVerdict(object.get(BIOME_IS_FIELD), biomeId);
        }

        if (NOT.equals(type)) {
            Boolean inner = biomeVerdict(object.get(INVERT_FIELD), biomeId);

            return inner == null ? null : !inner;
        }

        return null;
    }

    /**
     * The biome set of a {@code minecraft:biome} condition serializes in three shapes: a {@code "#tag"} reference, a
     * bare id when it holds a single biome, or an array of ids. Only the last two name their biomes; a tag does not
     * carry its contents in the rule, so it stays undecidable.
     */
    @Nullable
    private static Boolean biomeSetVerdict(JsonElement biomeIs, String biomeId) {
        if (biomeIs.isJsonArray()) {
            boolean matches = false;

            for (JsonElement entry : biomeIs.getAsJsonArray()) {
                Boolean verdict = biomeSetVerdict(entry, biomeId);

                if (verdict == null) {
                    return null;
                }

                matches |= verdict;
            }

            return matches;
        }

        if (biomeIs.isJsonPrimitive()) {
            String id = biomeIs.getAsString();

            return id.startsWith(TAG_PREFIX) ? null : biomeId.equals(id);
        }

        return null;
    }

    private enum Position { RULE, CONDITION, OTHER }

    private static class Rewrite {
        private final DynamicOps<JsonElement> ops;
        private final Map<String, ISurfaceRuleHandler> handlers;
        private final List<Ghost> ghosts;
        private final Map<List<Object>, BlockState> ghostStates = new HashMap<>();
        private final Map<String, JsonElement> resolved = new HashMap<>();
        private int coins;

        Rewrite(DynamicOps<JsonElement> ops, Map<String, ISurfaceRuleHandler> handlers, List<Ghost> ghosts) {
            this.ops = ops;
            this.handlers = handlers;
            this.ghosts = ghosts;
        }

        @NotNull
        JsonElement apply(JsonElement element, Position position) {
            if (element.isJsonArray()) {
                JsonArray copy = new JsonArray();

                for (JsonElement child : element.getAsJsonArray()) {
                    copy.add(apply(child, position));
                }

                return copy;
            }

            if (position != Position.OTHER && element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                return applyReferenced(element, position);
            }

            if (!element.isJsonObject()) {
                return element;
            }

            JsonObject object = element.getAsJsonObject();
            String type = typeOf(object);
            ISurfaceRuleHandler handler = type != null ? handlers.get(type) : null;

            if (handler != null) {
                BlockState state = ghostState(handler, object);

                if (state != null) {
                    return handler.alwaysPlaces() ? ghost(state) : marker(ghost(state));
                }
            }

            if (NOISE_THRESHOLD.equals(type) || HOLE.equals(type)) {
                return coin();
            }

            JsonObject copy = new JsonObject();

            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                copy.add(entry.getKey(), apply(entry.getValue(), childPosition(type, entry.getKey())));
            }

            return copy;
        }

        @NotNull
        private static Position childPosition(@Nullable String type, String field) {
            if (SEQUENCE.equals(type) && SEQUENCE_FIELD.equals(field)) {
                return Position.RULE;
            }

            if (CONDITION.equals(type)) {
                return switch (field) {
                    case IF_TRUE_FIELD -> Position.CONDITION;
                    case THEN_RUN_FIELD -> Position.RULE;
                    default -> Position.OTHER;
                };
            }

            if (NOT.equals(type) && INVERT_FIELD.equals(field)) {
                return Position.CONDITION;
            }

            return Position.OTHER;
        }

        @NotNull
        private JsonElement applyReferenced(JsonElement reference, Position position) {
            String key = position + reference.getAsString();
            JsonElement cached = resolved.get(key);

            if (cached != null) {
                return cached;
            }

            JsonElement inlined;

            try {
                if (position == Position.RULE) {
                    if (!(MaterialRule.CODEC.parse(ops, reference).getOrThrow() instanceof MaterialRule.HolderHolder holder)) {
                        return reference;
                    }

                    inlined = MaterialRule.CODEC.encodeStart(ops, holder.holder().value()).getOrThrow();
                } else {
                    if (!(MaterialCondition.CODEC.parse(ops, reference).getOrThrow() instanceof MaterialCondition.HolderHolder holder)) {
                        return reference;
                    }

                    inlined = MaterialCondition.CODEC.encodeStart(ops, holder.holder().value()).getOrThrow();
                }
            } catch (Throwable t) {
                return reference;
            }

            JsonElement rewritten = apply(inlined, position);
            JsonElement result = rewritten.equals(inlined) ? reference : rewritten;

            resolved.put(key, result);

            return result;
        }

        @Nullable
        private BlockState ghostState(ISurfaceRuleHandler handler, JsonObject definition) {
            List<Object> rule = List.of(handler, definition);
            BlockState state = ghostStates.get(rule);

            if (state == null && ghosts.size() < GHOST_COUNT) {
                state = Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, ghosts.size());
                ghosts.add(new Ghost(state, handler, definition.deepCopy()));
                ghostStates.put(rule, state);
            }

            return state;
        }

        @NotNull
        private static JsonObject ghost(BlockState state) {
            JsonObject ghost = new JsonObject();

            ghost.addProperty(TYPE, BLOCK);
            ghost.add(RESULT_STATE_FIELD, encode(state));

            return ghost;
        }

        @NotNull
        private static JsonObject marker(JsonObject ghost) {
            JsonObject never = new JsonObject();
            JsonObject marker = new JsonObject();

            never.addProperty(TYPE, Y_ABOVE);
            never.add("anchor", anchor(COIN_ANCHOR));
            never.addProperty("surface_depth_multiplier", 0);
            never.addProperty("add_stone_depth", false);
            marker.addProperty(TYPE, CONDITION);
            marker.add(IF_TRUE_FIELD, never);
            marker.add(THEN_RUN_FIELD, ghost);

            return marker;
        }

        // Never a constant true: an always-open gate shadows its sequence siblings and every not(gate) branch.
        @NotNull
        private JsonObject coin() {
            JsonObject coin = new JsonObject();

            coin.addProperty(TYPE, VERTICAL_GRADIENT);
            coin.addProperty("random_name", Utils.modLoc("coin_" + coins++).toString());
            coin.add("true_at_and_below", anchor(-COIN_ANCHOR));
            coin.add("false_at_and_above", anchor(COIN_ANCHOR));

            return coin;
        }

        @NotNull
        private static JsonObject anchor(int y) {
            JsonObject anchor = new JsonObject();

            anchor.addProperty("absolute", y);

            return anchor;
        }
    }

    @NotNull
    private static JsonElement encode(BlockState state) {
        return BlockState.CODEC.encodeStart(JsonOps.INSTANCE, state).getOrThrow();
    }

    private static boolean contains(JsonElement element, JsonElement needle) {
        if (element.equals(needle)) {
            return true;
        }

        if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) {
                if (contains(child, needle)) {
                    return true;
                }
            }
        } else if (element.isJsonObject()) {
            for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
                if (contains(entry.getValue(), needle)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean placesBlock(JsonElement element, String blockId) {
        if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) {
                if (placesBlock(child, blockId)) {
                    return true;
                }
            }

            return false;
        }

        if (element.isJsonPrimitive()) {
            String value = element.getAsString();

            return value.equals(blockId) || value.startsWith(blockId + "[");
        }

        if (!element.isJsonObject()) {
            return false;
        }

        JsonObject object = element.getAsJsonObject();

        if (object.has(NAME_FIELD) && object.get(NAME_FIELD).isJsonPrimitive() && blockId.equals(object.get(NAME_FIELD).getAsString())) {
            return true;
        }

        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            if (placesBlock(entry.getValue(), blockId)) {
                return true;
            }
        }

        return false;
    }

    @NotNull
    private static JsonObject sequence(JsonArray children) {
        JsonObject sequence = new JsonObject();

        sequence.addProperty(TYPE, SEQUENCE);
        sequence.add(SEQUENCE_FIELD, children);

        return sequence;
    }

    @NotNull
    private static JsonObject emptySequence() {
        return sequence(new JsonArray());
    }

    private static boolean isEmptySequence(@Nullable JsonElement element) {
        return element != null && element.isJsonObject()
                && SEQUENCE.equals(typeOf(element.getAsJsonObject()))
                && element.getAsJsonObject().has(SEQUENCE_FIELD)
                && element.getAsJsonObject().get(SEQUENCE_FIELD).isJsonArray()
                && element.getAsJsonObject().getAsJsonArray(SEQUENCE_FIELD).isEmpty();
    }

    private static int nodeCount(JsonElement element) {
        if (element.isJsonArray()) {
            int total = 0;

            for (JsonElement child : element.getAsJsonArray()) {
                total += nodeCount(child);
            }

            return total;
        }

        if (!element.isJsonObject()) {
            return 0;
        }

        int total = 1;

        for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
            total += nodeCount(entry.getValue());
        }

        return total;
    }

    @Nullable
    private static String typeOf(JsonObject object) {
        return object.has(TYPE) && object.get(TYPE).isJsonPrimitive() ? object.get(TYPE).getAsString() : null;
    }
}