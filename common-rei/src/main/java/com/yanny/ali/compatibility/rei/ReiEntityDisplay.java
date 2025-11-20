package com.yanny.ali.compatibility.rei;

import com.yanny.ali.compatibility.common.EntityLootType;
import com.yanny.ali.platform.Services;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.SpawnEggItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public class ReiEntityDisplay extends ReiBaseDisplay {
    private final EntityType<?> entityType;
    private final ResourceLocation variant;
    private final CategoryIdentifier<ReiEntityDisplay> identifier;

    public ReiEntityDisplay(EntityLootType entry, CategoryIdentifier<ReiEntityDisplay> identifier) {
        super(getSpawnEgg(entry), entry);
        entityType = entry.entityType();
        variant = entry.variant();
        this.identifier = identifier;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return identifier;
    }

    public EntityType<?> getEntityType() {
        return entityType;
    }

    public ResourceLocation getVariant() {
        return variant;
    }

    @Unmodifiable
    @NotNull
    private static List<EntryIngredient> getSpawnEgg(EntityLootType entry) {
        SpawnEggItem item = Services.getPlatform().getSpawnEggItem(entry.entityType());

        if (item != null) {
            return List.of(EntryIngredients.of(item));
        } else {
            return List.of();
        }
    }
}
