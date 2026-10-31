package com.yanny.ali.plugin.common.nodes;

import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IServerUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EntityLootTableNode extends LootTableNode {
    public static final ResourceLocation ID = Utils.modLoc("entity_loot_table");

    private final EntityType<?> entityType;
    private final TooltipNode spawnTooltip;
    @Nullable
    private final List<SynchedEntityData.DataValue<?>> variantData;

    public EntityLootTableNode(LootTableNode node, EntityType<?> entityType, TooltipNode spawnTooltip, @Nullable List<SynchedEntityData.DataValue<?>> variantData) {
        super(node.nodes(), node.getTooltip());
        this.entityType = entityType;
        this.spawnTooltip = spawnTooltip;
        this.variantData = variantData;
    }

    public EntityLootTableNode(IClientUtils utils, FriendlyByteBuf buf) {
        super(utils, buf);
        entityType = BuiltInRegistries.ENTITY_TYPE.get(buf.readResourceLocation());
        spawnTooltip = utils.getTooltipCache().getNodeById(buf.readVarInt());
        variantData = buf.readNullable((b) -> b.readList((v) -> SynchedEntityData.DataValue.read(v, v.readUnsignedByte())));
    }

    @Override
    public void encodeNode(IServerUtils utils, FriendlyByteBuf buf) {
        super.encodeNode(utils, buf);
        buf.writeResourceLocation(BuiltInRegistries.ENTITY_TYPE.getKey(entityType));
        buf.writeVarInt(utils.getTooltipCache().getNodeId(spawnTooltip));
        buf.writeNullable(variantData, (b, data) -> b.writeCollection(data, (v, value) -> value.write(v)));
    }

    @NotNull
    public EntityType<?> getEntityType() {
        return entityType;
    }

    @NotNull
    public TooltipNode getSpawnTooltip() {
        return spawnTooltip;
    }

    @Nullable
    public List<SynchedEntityData.DataValue<?>> getVariantData() {
        return variantData;
    }

    @NotNull
    @Override
    public ResourceLocation getId() {
        return ID;
    }
}
