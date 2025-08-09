package com.yanny.ali.compatibility.rei;

import com.yanny.ali.compatibility.common.EntityLootType;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.SpawnEggItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public class ReiEntityDisplay extends ReiBaseDisplay {
    private final Entity entity;
    private final CategoryIdentifier<ReiEntityDisplay> identifier;

    public ReiEntityDisplay(EntityLootType entry, CategoryIdentifier<ReiEntityDisplay> identifier) {
        super(getSpawnEgg(entry), entry);
        entity = entry.entity();
        this.identifier = identifier;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return identifier;
    }

    public Entity getEntity() {
        return entity;
    }

    @Unmodifiable
    @NotNull
    private static List<EntryIngredient> getSpawnEgg(EntityLootType entry) {
        SpawnEggItem item = SpawnEggItem.byId(entry.entity().getType());

        if (item != null) {
            return List.of(EntryIngredients.of(item));
        } else {
            return List.of();
        }
    }
}
