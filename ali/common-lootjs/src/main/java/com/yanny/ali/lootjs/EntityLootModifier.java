package com.yanny.ali.lootjs;

import com.almostreliable.lootjs.core.LootModificationByEntity;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.lootjs.mixin.MixinLootModificationByEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public class EntityLootModifier extends LootModifier<Entity> {
    private final HashSet<EntityType<?>> entityTypes;

    public EntityLootModifier(IServerUtils utils, LootModificationByEntity byEntity) {
        super(utils, byEntity);
        entityTypes = ((MixinLootModificationByEntity) byEntity).getEntities();
    }

    @Override
    public boolean predicate(Entity value) {
        return entityTypes.contains(value.getType());
    }

    @NotNull
    @Override
    public IType<Entity> getType() {
        return IType.ENTITY;
    }
}
