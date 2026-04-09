package com.yanny.ali.plugin.common.nodes;

import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.ListNode;
import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.List;

public abstract class CompositeNode extends ListNode {
    public CompositeNode(List<IDataNode> children) {
        children.forEach(this::addChildren);
    }

    public CompositeNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        super(utils, buf);
    }
}
