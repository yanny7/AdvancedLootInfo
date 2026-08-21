package com.yanny.ali.plugin.common.nodes;

import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IServerUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.NotNull;

public class EntityLootTableNode extends LootTableNode {
    public static final Identifier ID = Utils.modLoc("entity_loot_table");

    private final EntityType<?> entityType;

    public EntityLootTableNode(LootTableNode node, EntityType<?> entityType) {
        super(node.nodes(), node.getTooltip());
        this.entityType = entityType;
    }

    public EntityLootTableNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        super(utils, buf);
        entityType = BuiltInRegistries.ENTITY_TYPE.getValue(buf.readIdentifier());
    }

    @Override
    public void encodeNode(IServerUtils utils, RegistryFriendlyByteBuf buf) {
        super.encodeNode(utils, buf);
        buf.writeIdentifier(BuiltInRegistries.ENTITY_TYPE.getKey(entityType));
    }

    @NotNull
    public EntityType<?> getEntityType() {
        return entityType;
    }

    @NotNull
    @Override
    public Identifier getId() {
        return ID;
    }
}
