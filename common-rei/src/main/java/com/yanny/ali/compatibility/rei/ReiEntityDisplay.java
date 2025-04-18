package com.yanny.ali.compatibility.rei;

import com.yanny.ali.compatibility.common.EntityLootType;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import net.minecraft.world.entity.Entity;

import java.util.List;

public class ReiEntityDisplay extends ReiBaseDisplay {
    private final Entity entity;
    private final CategoryIdentifier<ReiEntityDisplay> identifier;

    public ReiEntityDisplay(EntityLootType entry, CategoryIdentifier<ReiEntityDisplay> identifier) {
        super(List.of(), entry);
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
}
