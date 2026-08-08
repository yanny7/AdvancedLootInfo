package com.yanny.awi.plugin.common.nodes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.SurfaceRules;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;

/**
 * Rewrites a dimension's surface rule into the subset that can fire for one specific biome. One instance per dimension
 * (the encode and the {@link RegistryOps} are done once and reused for every biome of that dimension).
 * <p>
 * The scan pins the biome for the whole of {@link NodeUtils#getBaseBlocksForBiome}, so every {@code minecraft:biome}
 * condition in the rule is a constant during that scan — yet the compiled rule still evaluates each of them. They are
 * {@code LazyYCondition}s, invalidated on every cell, so a heavily-modded overworld rule (one biome-gated branch per
 * biome per mod) pays a set lookup per branch per cell. Dropping the branches that can never match removes that.
 * <p>
 * The rewrite goes through {@link SurfaceRules.RuleSource#CODEC} rather than over the object graph: the node classes
 * ({@code SequenceRuleSource}, {@code TestRuleSource}, {@code BiomeConditionSource}) are package-private records, so
 * reaching them means reflection over names that remap in production. Serialized form is keyed by registry ids, which
 * do not.
 * <p>
 * <b>Unknown node types are descended into but never restructured.</b> Mods wrap the vanilla rule in their own rule
 * type (Terrablender being the common one), and the biome-gated branches worth pruning sit *inside* that wrapper — so
 * stopping at the first unrecognised node prunes nothing on exactly the packs that need it most. Inside an unknown
 * node, a dead branch is therefore replaced <i>in place</i> with an empty {@code minecraft:sequence} (a valid rule that
 * never fires) instead of being removed: arity and element order stay intact, so it does not matter whether the wrapper
 * indexes its children or pairs them with data of its own. Elements are only really dropped inside a
 * {@code minecraft:sequence}, whose semantics are known. An object carrying {@code "type": "minecraft:condition"} is
 * always a {@code RuleSource} (that id belongs to the rule registry), so descending cannot misread a condition as one.
 * <p>
 * Any failure falls back to the original rule, and a dimension whose first biome prunes nothing turns specialization
 * off for itself, so the encode cost is not paid per biome for nothing.
 */
public class SurfaceRuleSpecializer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Set<SurfaceRules.RuleSource> LOGGED_RULES = Collections.newSetFromMap(new IdentityHashMap<>());

    private static final String TYPE = "type";
    private static final String SEQUENCE = "minecraft:sequence";
    private static final String CONDITION = "minecraft:condition";
    private static final String BIOME = "minecraft:biome";
    private static final String NOT = "minecraft:not";
    private static final String SEQUENCE_FIELD = "sequence";
    private static final String IF_TRUE_FIELD = "if_true";
    private static final String THEN_RUN_FIELD = "then_run";
    private static final String BIOME_IS_FIELD = "biome_is";
    private static final String INVERT_FIELD = "invert";

    private final SurfaceRules.RuleSource original;
    private final DynamicOps<JsonElement> ops;
    @Nullable
    private final JsonElement encoded;
    private final boolean logStatistics;

    private boolean effective = true;

    /**
     * @param codecLookup the provider that <i>owns</i> the holders the rule references — a different provider holding
     *                    equal values still fails the codec's ownership check and turns specialization off. In game
     *                    that is simply the level's {@code RegistryAccess}.
     */
    public SurfaceRuleSpecializer(SurfaceRules.RuleSource original, HolderLookup.Provider codecLookup, boolean logStatistics) {
        JsonElement json = null;
        DynamicOps<JsonElement> dynamicOps = RegistryOps.create(JsonOps.INSTANCE, codecLookup);

        try {
            json = SurfaceRules.RuleSource.CODEC.encodeStart(dynamicOps, original).getOrThrow();
        } catch (Throwable t) {
            LOGGER.warn("Could not encode the surface rule, per-biome specialization is off for this dimension", t);
        }

        this.original = original;
        this.ops = dynamicOps;
        this.encoded = json;
        this.logStatistics = logStatistics;
    }

    /** The rule with every branch that cannot fire for {@code biome} removed, or the original rule if none can be. */
    @NotNull
    public SurfaceRules.RuleSource specialize(Holder<Biome> biome) {
        Optional<ResourceKey<Biome>> key = biome.unwrapKey();

        if (!effective || encoded == null || key.isEmpty()) {
            return original;
        }

        try {
            JsonElement pruned = prune(encoded, key.get().location().toString(), false);

            if (pruned == null || pruned.equals(encoded)) {
                // Nothing in this rule is decidable per biome — stop paying the encode for the rest of the dimension.
                effective = false;
                log("not specializable, no biome-gated branch could be pruned", encoded, null);

                return original;
            }

            SurfaceRules.RuleSource result = SurfaceRules.RuleSource.CODEC.parse(ops, pruned).getOrThrow();

            log("specialized", encoded, pruned);

            return result;
        } catch (Throwable t) {
            effective = false;
            LOGGER.warn("Could not specialize the surface rule per biome, using it unchanged for this dimension", t);

            return original;
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
        } else {
            int nodesAfter = nodeCount(after);

            LOGGER.info("Surface rule {}: {} -> {} nodes ({}% removed)", what, nodesBefore, nodesAfter,
                    100 - (nodesAfter * 100 / Math.max(1, nodesBefore)));
        }
    }

    /**
     * Returns the rule with branches that cannot fire for {@code biomeId} pruned. {@code canDrop} says whether the
     * caller may cope with the node disappearing entirely (only a {@code minecraft:sequence} can); everywhere else a
     * dead branch is replaced in place by an empty sequence, which never fires but keeps the structure intact.
     */
    @Nullable
    private static JsonElement prune(JsonElement element, String biomeId, boolean canDrop) {
        if (element.isJsonArray()) {
            JsonArray pruned = new JsonArray();

            for (JsonElement child : element.getAsJsonArray()) {
                pruned.add(prune(child, biomeId, false));
            }

            return pruned;
        }

        if (!element.isJsonObject()) {
            return element;
        }

        JsonObject object = element.getAsJsonObject();
        String type = typeOf(object);

        if (SEQUENCE.equals(type) && object.has(SEQUENCE_FIELD) && object.get(SEQUENCE_FIELD).isJsonArray()) {
            JsonArray kept = new JsonArray();

            for (JsonElement child : object.getAsJsonArray(SEQUENCE_FIELD)) {
                JsonElement prunedChild = prune(child, biomeId, true);

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
            Boolean matches = biomeVerdict(condition, biomeId);

            if (Boolean.FALSE.equals(matches)) {
                return canDrop ? null : emptySequence();
            }

            JsonElement thenRun = prune(object.get(THEN_RUN_FIELD), biomeId, false);

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
            copy.add(entry.getKey(), prune(entry.getValue(), biomeId, false));
        }

        return copy;
    }

    /** {@code TRUE}/{@code FALSE} when the condition is a biome test that always/never matches, {@code null} otherwise. */
    @Nullable
    private static Boolean biomeVerdict(@Nullable JsonElement condition, String biomeId) {
        if (condition == null || !condition.isJsonObject()) {
            return null;
        }

        JsonObject object = condition.getAsJsonObject();
        String type = typeOf(object);

        if (BIOME.equals(type) && object.has(BIOME_IS_FIELD) && object.get(BIOME_IS_FIELD).isJsonArray()) {
            for (JsonElement entry : object.getAsJsonArray(BIOME_IS_FIELD)) {
                if (entry.isJsonPrimitive() && biomeId.equals(entry.getAsString())) {
                    return Boolean.TRUE;
                }
            }

            return Boolean.FALSE;
        }

        if (NOT.equals(type)) {
            Boolean inner = biomeVerdict(object.get(INVERT_FIELD), biomeId);

            return inner == null ? null : !inner;
        }

        return null;
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