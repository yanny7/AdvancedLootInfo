package com.yanny.ali.plugin.common.tooltip;

import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.ITooltipNode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ArrayTooltipNode extends ListTooltipNode {
    public static final ResourceLocation ID = new ResourceLocation(Utils.MOD_ID, "array");

    private ArrayTooltipNode(List<ITooltipNode> children) {
        super(children);
    }

    public ArrayTooltipNode add(ITooltipNode node) {
        super.addNode(node);
        return this;
    }

    @Override
    void encodeNode(FriendlyByteBuf buf) {
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public String toString() {
        return "ArrayTooltipNode{" +
                "children=" + getChildren() +
                '}';
    }

    @NotNull
    public static ArrayTooltipNode array() {
        return new ArrayTooltipNode(new ArrayList<>());
    }

    @NotNull
    public static ArrayTooltipNode decode(IClientUtils utils, FriendlyByteBuf buf) {
        List<ITooltipNode> children = ListTooltipNode.decodeChildren(utils, buf);
        return new ArrayTooltipNode(children);
    }
}
