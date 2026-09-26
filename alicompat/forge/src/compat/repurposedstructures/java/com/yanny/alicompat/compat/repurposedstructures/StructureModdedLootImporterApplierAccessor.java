package com.yanny.alicompat.compat.repurposedstructures;

import com.telepathicgrunt.repurposedstructures.configs.forge.RSModdedLootConfig;
import com.telepathicgrunt.repurposedstructures.misc.forge.lootmanager.StructureModdedLootImporterApplier;
import com.telepathicgrunt.repurposedstructures.misc.lootmanager.StructureModdedLootImporter;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.ali.plugin.glm.IPageLootModifier;
import com.yanny.ali.plugin.glm.LootPage;
import com.yanny.ali.plugin.glm.Verdict;
import com.yanny.ali.plugin.server.GenericTooltipUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IGlobalLootModifierAccessor;
import com.yanny.alicompat.accessor.IPageResolverAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class StructureModdedLootImporterApplierAccessor extends BaseAccessor<StructureModdedLootImporterApplier> implements IGlobalLootModifierAccessor, IPageResolverAccessor {
    @FieldAccessor
    protected LootItemCondition[] conditions;

    public StructureModdedLootImporterApplierAccessor(StructureModdedLootImporterApplier parent) {
        super(parent);
    }

    @Override
    public Optional<IPageLootModifier> getLootModifier(IServerUtils utils) {
        return Optional.of(GlobalLootModifierUtils.getLootModifier(utils, parent, Arrays.asList(conditions), (page, c) -> getOperations(utils, page, c)));
    }

    @NotNull
    @Override
    public Verdict test(IServerUtils ignoredUtils, LootPage page) {
        return GlobalLootModifierUtils.testTable(page, StructureModdedLootImporterApplierAccessor::isImported, false);
    }

    @NotNull
    private static List<IOperation> getOperations(IServerUtils utils, LootPage page, List<LootItemCondition> conditions) {
        ResourceLocation imported = StructureModdedLootImporter.TABLE_IMPORTS.get(page.tableId());

        if (imported == null) {
            return Collections.emptyList();
        }

        return Collections.singletonList(new IOperation.AddOperation((itemStack) -> true, getImportNode(utils, conditions, imported)));
    }

    @NotNull
    private static IDataNode getImportNode(IServerUtils utils, List<LootItemCondition> conditions, ResourceLocation imported) {
        TooltipNode tooltip = TooltipBuilder.array((b) -> {
            b.add(TooltipBuilder.keyOnly(Lang.Group.ALL));
            b.add(TooltipBuilder.keyOnly(RepurposedStructuresLang.Value.MODDED_ITEMS_ONLY));
            b.add(GenericTooltipUtils.getConditionsSectionTooltip(utils, conditions));
        }).build();

        return NodeUtils.getReferenceNode(utils, imported, conditions, tooltip);
    }

    private static boolean isImported(ResourceLocation location) {
        return RSModdedLootConfig.importModdedItems.get() && StructureModdedLootImporter.TABLE_IMPORTS.containsKey(location) && !StructureModdedLootImporter.isInBlacklist(location);
    }
}
