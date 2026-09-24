package com.yanny.awi.configuration;

import com.yanny.aci.CommonLogUtils;
import com.yanny.awi.Utils;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.dimension.LevelStem;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Predicate;

public class DimensionFilter {
    private static final Logger LOGGER = CommonLogUtils.getLogger(Utils.MOD_ID);

    private final Set<ResourceLocation> shown = new HashSet<>();
    private final Set<ResourceLocation> hidden = new HashSet<>();
    private final List<TagKey<LevelStem>> shownTags = new ArrayList<>();
    private final List<TagKey<LevelStem>> hiddenTags = new ArrayList<>();

    public DimensionFilter(List<String> entries) {
        for (String entry : entries) {
            boolean negated = entry.startsWith("!");
            String target = negated ? entry.substring(1) : entry;
            boolean tag = target.startsWith("#");
            ResourceLocation id = ResourceLocation.tryParse(tag ? target.substring(1) : target);

            if (id == null) {
                LOGGER.warn("Ignoring invalid dimension filter entry '{}'", entry);
            } else if (tag) {
                (negated ? hiddenTags : shownTags).add(TagKey.create(Registries.LEVEL_STEM, id));
            } else {
                (negated ? hidden : shown).add(id);
            }
        }
    }

    public boolean isVisible(Registry<LevelStem> registry, ResourceLocation dimension) {
        Optional<Holder.Reference<LevelStem>> holder = registry.getHolder(ResourceKey.create(Registries.LEVEL_STEM, dimension));
        return isVisible(dimension, (tag) -> holder.map((h) -> h.is(tag)).orElse(false));
    }

    public boolean isVisible(ResourceLocation dimension) {
        return isVisible(dimension, null);
    }

    private boolean isVisible(ResourceLocation dimension, @Nullable Predicate<TagKey<LevelStem>> isInTag) {
        if (hidden.contains(dimension) || (isInTag != null && hiddenTags.stream().anyMatch(isInTag))) {
            return false;
        }

        if ((shown.isEmpty() && shownTags.isEmpty()) || shown.contains(dimension)) {
            return true;
        }

        // Dimension tags are not synced to the client; the server already dropped what a tag entry excludes.
        if (isInTag == null) {
            return !shownTags.isEmpty();
        }

        return shownTags.stream().anyMatch(isInTag);
    }
}
