package com.yanny.aci.test.utils;

import com.yanny.aci.tooltip.TooltipNode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class FailingNode implements TestDataNode {
    public static final ResourceLocation ID = new ResourceLocation("aci_test", "failing");

    private final float chance;

    public FailingNode(float chance) {
        this.chance = chance;
    }

    @NotNull
    @Override
    public TooltipNode getTooltip() {
        return TooltipNode.empty();
    }

    @NotNull
    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void encode(TestServerUtils utils, FriendlyByteBuf buf) {
        buf.writeUtf("partially written");
        throw new IllegalStateException("node cannot be encoded");
    }

    @Override
    public float getChance() {
        return chance;
    }
}
