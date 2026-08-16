package com.yanny.aci.test.utils;

import com.yanny.aci.tooltip.TooltipNode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class TestLeafNode implements TestDataNode {
    public static final ResourceLocation ID = new ResourceLocation("aci_test", "leaf");

    public final String value;
    public final int count;
    public final float chance;

    private final TooltipNode tooltip;

    public TestLeafNode(String value, int count, float chance, TooltipNode tooltip) {
        this.value = value;
        this.count = count;
        this.chance = chance;
        this.tooltip = tooltip;
    }

    public TestLeafNode(TestClientUtils utils, FriendlyByteBuf buf) {
        value = buf.readUtf();
        count = buf.readVarInt();
        chance = buf.readFloat();
        tooltip = utils.getTooltipCache().getNodeById(buf.readVarInt());
    }

    @NotNull
    @Override
    public TooltipNode getTooltip() {
        return tooltip;
    }

    @NotNull
    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void encode(TestServerUtils utils, FriendlyByteBuf buf) {
        buf.writeUtf(value);
        buf.writeVarInt(count);
        buf.writeFloat(chance);
        buf.writeVarInt(utils.getTooltipCache().getNodeId(tooltip));
    }

    @Override
    public float getChance() {
        return chance;
    }
}
