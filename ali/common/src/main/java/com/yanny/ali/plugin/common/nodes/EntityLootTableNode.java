package com.yanny.ali.plugin.common.nodes;

import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IServerUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.NotNull;

public class EntityLootTableNode extends LootTableNode {
    public static final ResourceLocation ID = Utils.modLoc("entity_loot_table");

    private final EntityType<?> entityType;
    private final TooltipNode spawnTooltip;

    public EntityLootTableNode(LootTableNode node, EntityType<?> entityType, TooltipNode spawnTooltip) {
        super(node.nodes(), node.getTooltip());
        this.entityType = entityType;
        this.spawnTooltip = spawnTooltip;
    }

    public EntityLootTableNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        super(utils, buf);
        entityType = BuiltInRegistries.ENTITY_TYPE.get(buf.readResourceLocation());
        spawnTooltip = utils.getTooltipCache().getNodeById(buf.readVarInt());
    }

    @Override
    public void encodeNode(IServerUtils utils, RegistryFriendlyByteBuf buf) {
        super.encodeNode(utils, buf);
        buf.writeResourceLocation(BuiltInRegistries.ENTITY_TYPE.getKey(entityType));
        buf.writeVarInt(utils.getTooltipCache().getNodeId(spawnTooltip));
    }

    @NotNull
    public EntityType<?> getEntityType() {
        return entityType;
    }

    @NotNull
    public TooltipNode getSpawnTooltip() {
        return spawnTooltip;
    }

    @NotNull
    @Override
    public ResourceLocation getId() {
        return ID;
    }
}
