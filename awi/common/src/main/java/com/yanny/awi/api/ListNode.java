package com.yanny.awi.api;

import com.yanny.aci.api.CoreListNode;
import net.minecraft.network.RegistryFriendlyByteBuf;

public abstract class ListNode extends CoreListNode<IServerUtils, IDataNode, IClientUtils> implements IDataNode {
    public ListNode() {
        super();
    }

    public ListNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        super(utils, buf);
    }
}
