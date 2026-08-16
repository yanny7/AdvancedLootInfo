package com.yanny.aci.test.utils;

import com.yanny.aci.api.CoreListNode;
import com.yanny.aci.tooltip.TooltipNode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class TestListNode extends CoreListNode<TestServerUtils, TestDataNode, TestClientUtils> implements TestDataNode {
    public static final ResourceLocation ID = new ResourceLocation("aci_test", "list");

    public final String name;

    private final TooltipNode tooltip;

    public TestListNode(String name, TooltipNode tooltip) {
        this.name = name;
        this.tooltip = tooltip;
    }

    public TestListNode(TestClientUtils utils, FriendlyByteBuf buf) {
        super(utils, buf);
        name = buf.readUtf();
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
    public void encodeNode(TestServerUtils utils, FriendlyByteBuf buf) {
        buf.writeUtf(name);
        buf.writeVarInt(utils.getTooltipCache().getNodeId(tooltip));
    }
}
