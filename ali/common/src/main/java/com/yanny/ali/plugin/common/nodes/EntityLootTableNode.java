package com.yanny.ali.plugin.common.nodes;

import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IServerUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.NotNull;

public class EntityLootTableNode extends LootTableNode {
    public static final ResourceLocation ID = Utils.modLoc("entity_loot_table");

    private final EntityType<?> entityType;

    public EntityLootTableNode(LootTableNode node, EntityType<?> entityType) {
        super(node.nodes(), node.getTooltip());
        this.entityType = entityType;
    }

    public EntityLootTableNode(IClientUtils utils, FriendlyByteBuf buf) {
        super(utils, buf);
        entityType = BuiltInRegistries.ENTITY_TYPE.get(buf.readResourceLocation());
    }

    @Override
    public void encodeNode(IServerUtils utils, FriendlyByteBuf buf) {
        super.encodeNode(utils, buf);
        buf.writeResourceLocation(BuiltInRegistries.ENTITY_TYPE.getKey(entityType));
    }

    @NotNull
    public EntityType<?> getEntityType() {
        return entityType;
    }

    @NotNull
    @Override
    public ResourceLocation getId() {
        return ID;
    }
}
