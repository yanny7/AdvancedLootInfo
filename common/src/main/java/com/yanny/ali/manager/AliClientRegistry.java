package com.yanny.ali.manager;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.yanny.ali.api.*;
import com.yanny.ali.platform.Services;
import com.yanny.ali.plugin.common.nodes.MissingNode;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;

import static com.yanny.ali.plugin.client.WidgetUtils.VERTICAL_OFFSET;

public class AliClientRegistry implements IClientRegistry, IClientUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Map<ResourceLocation, IWidgetFactory> widgetMap = new HashMap<>();
    private final Map<ResourceLocation, NodeFactory<?>> nodeFactoryMap = new HashMap<>();
    private final Map<ResourceLocation, WidgetDirection> widgetDirectionMap = new HashMap<>();
    private final Map<ResourceLocation, IBoundsGetter> widgetBoundsMap = new HashMap<>();
    private final Map<ResourceLocation, List<Item>> lootItemMap = new HashMap<>();
    private final Map<ResourceLocation, IDataNode> lootNodeMap = new HashMap<>();

    public void addLootData(ResourceLocation resourceLocation, IDataNode node, List<Item> items) {
        lootItemMap.put(resourceLocation, items);
        lootNodeMap.put(resourceLocation, node);
    }

    public Map<ResourceLocation, IDataNode> getLootData() {
        return lootNodeMap;
    }

    public void clearLootData() {
        lootNodeMap.clear();
        lootItemMap.clear();
    }

    @Override
    public void registerWidget(ResourceLocation id, WidgetDirection direction, IWidgetFactory factory, IBoundsGetter boundsGetter) {
        widgetMap.put(id, factory);
        widgetDirectionMap.put(id, direction);
        widgetBoundsMap.put(id, boundsGetter);
    }

    @Override
    public <T extends IDataNode> void registerNode(ResourceLocation id, NodeFactory<T> nodeFactory) {
        nodeFactoryMap.put(id, nodeFactory);
    }

    @Override
    public Pair<List<IWidget>, Rect> createWidgets(IWidgetUtils utils, List<IDataNode> entries, int x, int y, int maxWidth) {
        int posX = x, posY = y;
        int width = 0, height = 0;
        List<IWidget> widgets = new LinkedList<>();
        WidgetDirection lastDirection = null;

        for (IDataNode entry : entries) {
            WidgetDirection direction = widgetDirectionMap.getOrDefault(entry.getId(), widgetDirectionMap.get(MissingNode.ID));
            IWidgetFactory widgetFactory = widgetMap.getOrDefault(entry.getId(), widgetMap.get(MissingNode.ID));
            IBoundsGetter bounds = widgetBoundsMap.getOrDefault(entry.getId(), widgetBoundsMap.get(MissingNode.ID));

            if (lastDirection != null && direction != lastDirection && direction == WidgetDirection.VERTICAL) {
                posX = x;
                posY = y + height + VERTICAL_OFFSET;
            }

            Rect bound = bounds.apply(utils, entry, posX, posY, maxWidth);

            if (bound.right() > maxWidth) {
                posX = x;
                posY += bound.height();
                bound = bounds.apply(utils, entry, posX, posY, maxWidth);
            }

            IWidget widget = widgetFactory.create(utils, entry, posX, posY, maxWidth);
            width = Math.max(width, bound.right() - x);
            height = Math.max(height, bound.bottom() - y);

            if (lastDirection != null) {
                if (lastDirection != direction) {
                    posX = x;
                    posY += bound.height() + VERTICAL_OFFSET;
                } else if (direction == WidgetDirection.HORIZONTAL) {
                    posX += bound.width();
                } else if (direction == WidgetDirection.VERTICAL) {
                    posY += bound.height() + VERTICAL_OFFSET;
                }
            } else {
                switch (direction) {
                    case HORIZONTAL -> posX += bound.width();
                    case VERTICAL -> posY += bound.height() + VERTICAL_OFFSET;
                }
            }

            widgets.add(widget);
            lastDirection = direction;
        }

        return new Pair<>(widgets, new Rect(x, y, width, height));
    }

    @Override
    public <T extends IDataNode> NodeFactory<T> getNodeFactory(ResourceLocation id) {
        //noinspection unchecked
        NodeFactory<T> nodeFactory = (NodeFactory<T>) nodeFactoryMap.get(id);

        return Objects.requireNonNullElseGet(nodeFactory, () -> {
            throw new IllegalStateException(String.format("Failed to construct node - node {%s} was not registered!", id));
        });
    }

    @Override
    public Rect getBounds(IClientUtils utils, List<IDataNode> entries, int x, int y, int maxWidth) {
        int posX = x, posY = y;
        int width = 0, height = 0;
        WidgetDirection lastDirection = null;

        for (IDataNode entry : entries) {
            WidgetDirection direction = widgetDirectionMap.getOrDefault(entry.getId(), widgetDirectionMap.get(MissingNode.ID));
            IBoundsGetter bounds = widgetBoundsMap.getOrDefault(entry.getId(), widgetBoundsMap.get(MissingNode.ID));

            if (lastDirection != null && direction != lastDirection && direction == WidgetDirection.VERTICAL) {
                posX = x;
                posY = y + height + VERTICAL_OFFSET;
            }

            Rect bound = bounds.apply(utils, entry, posX, posY, maxWidth);

            if (bound.right() > maxWidth) {
                posX = x;
                posY += bound.height();
                bound = bounds.apply(utils, entry, posX, posY, maxWidth);
            }

            width = Math.max(width, bound.right() - x);
            height = Math.max(height, bound.bottom() - y);

            if (lastDirection != null) {
                if (lastDirection != direction) {
                    posX = x;
                    posY += bound.height() + VERTICAL_OFFSET;
                } else if (direction == WidgetDirection.HORIZONTAL) {
                    posX += bound.width();
                } else if (direction == WidgetDirection.VERTICAL) {
                    posY += bound.height() + VERTICAL_OFFSET;
                }
            } else {
                switch (direction) {
                    case HORIZONTAL -> posX += bound.width();
                    case VERTICAL -> posY += bound.height() + VERTICAL_OFFSET;
                }
            }

            lastDirection = direction;
        }

        return new Rect(x, y, width, height);
    }

    @Nullable
    @Override
    public WidgetDirection getWidgetDirection(ResourceLocation id) {
        return widgetDirectionMap.get(id);
    }

    @Override
    public List<Item> getItems(ResourceLocation location) {
        return lootItemMap.getOrDefault(location, List.of());
    }

    @Override
    public List<LootPool> getLootPools(LootTable lootTable) {
        return Services.PLATFORM.getLootPools(lootTable);
    }

    public void printClientInfo() {
        LOGGER.info("Registered {} widgets", widgetMap.size());
        LOGGER.info("Registered {} node factories", nodeFactoryMap.size());
        LOGGER.info("Registered {} widget directions", widgetDirectionMap.size());
        LOGGER.info("Registered {} widget bounds", widgetBoundsMap.size());
    }
}
