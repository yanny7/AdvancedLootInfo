package com.yanny.ali.api;

import com.yanny.aci.api.CoreListNode;
import net.minecraft.network.FriendlyByteBuf;

public abstract class ListNode extends CoreListNode<IServerUtils, IDataNode, IClientUtils> implements IDataNode {
    public ListNode() {
        super();
    }

    public ListNode(IClientUtils utils, FriendlyByteBuf buf) {
        super(utils, buf);
    }
}
