package com.yanny.ali.plugin.common.nodes;

import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IServerUtils;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.NotNull;

public class EntityLootTableNode extends LootTableNode {
    public static final ResourceLocation ID = Utils.modLoc("entity_loot_table");

    private final EntityType<?> entityType;
    private final TooltipNode infoTooltip;

    public EntityLootTableNode(LootTableNode node, EntityType<?> entityType, TooltipNode infoTooltip) {
        super(node.nodes(), node.getTooltip());
        this.entityType = entityType;
        this.infoTooltip = infoTooltip;
    }

    public EntityLootTableNode(IClientUtils utils, FriendlyByteBuf buf) {
        super(utils, buf);
        entityType = Registry.ENTITY_TYPE.get(buf.readResourceLocation());
        infoTooltip = utils.getTooltipCache().getNodeById(buf.readVarInt());
    }

    @Override
    public void encodeNode(IServerUtils utils, FriendlyByteBuf buf) {
        super.encodeNode(utils, buf);
        buf.writeResourceLocation(Registry.ENTITY_TYPE.getKey(entityType));
        buf.writeVarInt(utils.getTooltipCache().getNodeId(infoTooltip));
    }

    @NotNull
    public EntityType<?> getEntityType() {
        return entityType;
    }

    @NotNull
    public TooltipNode getInfoTooltip() {
        return infoTooltip;
    }

    @NotNull
    @Override
    public ResourceLocation getId() {
        return ID;
    }
}
