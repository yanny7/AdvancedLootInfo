package com.yanny.ali.plugin.common.nodes;

import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IServerUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class EntityLootTableNode extends LootTableNode {
    public static final Identifier ID = Utils.modLoc("entity_loot_table");

    private final EntityType<?> entityType;
    private final TooltipNode infoTooltip;
    @Nullable
    private final List<SynchedEntityData.DataValue<?>> variantData;

    public EntityLootTableNode(LootTableNode node, EntityType<?> entityType, TooltipNode infoTooltip, @Nullable List<SynchedEntityData.DataValue<?>> variantData) {
        super(node.nodes(), node.getTooltip());
        this.entityType = entityType;
        this.infoTooltip = infoTooltip;
        this.variantData = variantData;
    }

    public EntityLootTableNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        super(utils, buf);
        entityType = BuiltInRegistries.ENTITY_TYPE.getValue(buf.readIdentifier());
        infoTooltip = utils.getTooltipCache().getNodeById(buf.readVarInt());
        variantData = buf.readBoolean() ? readVariantData(buf) : null;
    }

    @Override
    public void encodeNode(IServerUtils utils, RegistryFriendlyByteBuf buf) {
        super.encodeNode(utils, buf);
        buf.writeIdentifier(BuiltInRegistries.ENTITY_TYPE.getKey(entityType));
        buf.writeVarInt(utils.getTooltipCache().getNodeId(infoTooltip));
        buf.writeBoolean(variantData != null);

        if (variantData != null) {
            buf.writeVarInt(variantData.size());
            variantData.forEach((value) -> value.write(buf));
        }
    }

    @NotNull
    public EntityType<?> getEntityType() {
        return entityType;
    }

    @NotNull
    public TooltipNode getInfoTooltip() {
        return infoTooltip;
    }

    @Nullable
    public List<SynchedEntityData.DataValue<?>> getVariantData() {
        return variantData;
    }

    @NotNull
    @Override
    public Identifier getId() {
        return ID;
    }

    @NotNull
    private static List<SynchedEntityData.DataValue<?>> readVariantData(RegistryFriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<SynchedEntityData.DataValue<?>> data = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            data.add(SynchedEntityData.DataValue.read(buf, buf.readUnsignedByte()));
        }

        return data;
    }
}
