package com.yanny.alicompat.compat.repurposedstructures;

import com.telepathicgrunt.repurposedstructures.configs.RSMainModdedLootConfig;
import com.telepathicgrunt.repurposedstructures.misc.lootmanager.StructureModdedLootImporter;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.glm.IPageLootModifier;
import com.yanny.ali.plugin.glm.LootPage;
import com.yanny.ali.plugin.glm.Match;
import com.yanny.ali.plugin.glm.PageMatch;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public record StructureModdedLootImportModifier(IServerUtils utils) implements IPageLootModifier {
    @NotNull
    @Override
    public PageMatch test(LootPage page) {
        if (isImported(page.tableId())) {
            return new PageMatch(Match.YES, List.of());
        }

        return PageMatch.NO;
    }

    @NotNull
    @Override
    public List<IOperation> getOperations(LootPage page, PageMatch ignoredMatch) {
        ResourceKey<LootTable> imported = StructureModdedLootImporter.TABLE_IMPORTS.get(tableKey(page.tableId()));

        if (imported == null) {
            return Collections.emptyList();
        }

        return Collections.singletonList(new IOperation.AddOperation((itemStack) -> true, getImportNode(imported.identifier())));
    }

    @NotNull
    private IDataNode getImportNode(Identifier imported) {
        TooltipNode tooltip = TooltipBuilder.array((b) -> {
            b.add(TooltipBuilder.keyOnly(Lang.Group.ALL));
            b.add(TooltipBuilder.keyOnly(RepurposedStructuresLang.Value.MODDED_ITEMS_ONLY));
        }).build();

        return NodeUtils.getReferenceNode(utils, imported, List.of(), tooltip);
    }

    private static boolean isImported(Identifier location) {
        ResourceKey<LootTable> key = tableKey(location);

        return RSMainModdedLootConfig.importModdedItems && StructureModdedLootImporter.TABLE_IMPORTS.containsKey(key) && !StructureModdedLootImporter.isInBlacklist(key);
    }

    @NotNull
    private static ResourceKey<LootTable> tableKey(Identifier location) {
        return ResourceKey.create(Registries.LOOT_TABLE, location);
    }
}
