package com.yanny.ali.api;

import com.mojang.logging.LogUtils;
import com.yanny.aci.api.CoreListNode;
import com.yanny.ali.manager.PluginManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.slf4j.Logger;

import java.util.List;

public abstract class ListNode extends CoreListNode<IServerUtils, ITooltipNode, IDataNode, IClientUtils> implements IDataNode {
    private static final Logger LOGGER = LogUtils.getLogger();

    public ListNode() {
        super();
    }

    public ListNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        super(utils, buf);
    }

    @Override
    public final void encode(IServerUtils utils, RegistryFriendlyByteBuf buf) {
        List<IDataNode> nodes = nodes();
        int countIndex = buf.writerIndex();
        int successfulNodes = 0;

        buf.writeInt(nodes.size());

        for (IDataNode node : nodes) {
            int startOfNode = buf.writerIndex();

            try {
                buf.writeIdentifier(node.getId());
                node.encode(utils, buf);
                successfulNodes++;
            } catch (Throwable e) {
                buf.writerIndex(startOfNode);
                LOGGER.warn("Failed to write node in {}", PluginManager.getInstance().serverRegistry.getCurrentLootTable(), e);
            }
        }

        if (successfulNodes != nodes.size()) {
            int endIndex = buf.writerIndex();

            buf.writerIndex(countIndex);
            buf.writeInt(successfulNodes);
            buf.writerIndex(endIndex);
        }

        encodeNode(utils, buf);
    }
}
