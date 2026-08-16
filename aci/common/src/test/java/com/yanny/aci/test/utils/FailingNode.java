package com.yanny.aci.test.utils;

import com.yanny.aci.tooltip.TooltipNode;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class FailingNode implements TestDataNode {
    public static final Identifier ID = Identifier.fromNamespaceAndPath("aci_test", "failing");

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
    public Identifier getId() {
        return ID;
    }

    @Override
    public void encode(TestServerUtils utils, RegistryFriendlyByteBuf buf) {
        buf.writeUtf("partially written");
        throw new IllegalStateException("node cannot be encoded");
    }

    @Override
    public float getChance() {
        return chance;
    }
}
