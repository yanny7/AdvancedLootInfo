package com.yanny.ali.plugin.server;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Unit;
import net.minecraft.world.LockCode;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.AdventureModePredicate;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.JukeboxPlayable;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.*;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.Repairable;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.PotDecorations;
import net.minecraft.world.level.saveddata.maps.MapId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.getFilterableTooltip;
import static com.yanny.ali.plugin.server.GenericTooltipUtils.getMapTooltip;

public class DataComponentTooltipUtils {
    @Unmodifiable
    @NotNull
    public static TooltipBuilder getCustomDataTooltip(IServerUtils utils, CustomData value) {
        return utils.getValueTooltip(utils, value.copyTag().toString()).key(Lang.Value.TAG);
    }

    @Unmodifiable
    @NotNull
    public static TooltipBuilder getIntTooltip(IServerUtils utils, int value) {
        return utils.getValueTooltip(utils, value).key(Lang.Value.VALUE);
    }

    @Unmodifiable
    @NotNull
    public static TooltipBuilder getCustomNameTooltip(IServerUtils utils, Component value) {
        return utils.getValueTooltip(utils, value).key(Lang.Value.CUSTOM_NAME);
    }

    @Unmodifiable
    @NotNull
    public static TooltipBuilder getItemNameTooltip(IServerUtils utils, Component value) {
        return utils.getValueTooltip(utils, value).key(Lang.Value.ITEM_NAME);
    }

    @NotNull
    public static TooltipBuilder getItemLoreTooltip(IServerUtils utils, ItemLore value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.lines()).build(Lang.Branch.LINES));
            b.add(utils.getValueTooltip(utils, value.styledLines()).build(Lang.Branch.STYLED_LINES));
        });
    }

    @Unmodifiable
    @NotNull
    public static TooltipBuilder getRarityTooltip(IServerUtils utils, Rarity value) {
        return utils.getValueTooltip(utils, value).key(Lang.Value.RARITY);
    }

    @NotNull
    public static TooltipBuilder getItemEnchantmentsTooltip(IServerUtils utils, ItemEnchantments value) {
        return getMapTooltip(utils, value.enchantments, GenericTooltipUtils::getEnchantmentLevelEntryTooltip).key(Lang.Branch.ENCHANTMENTS);
    }

    @NotNull
    public static TooltipBuilder getAdventureModePredicateTooltip(IServerUtils utils, AdventureModePredicate value) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, value.predicates).build(Lang.Branch.BLOCKS)));
    }

    @NotNull
    public static TooltipBuilder getAttributeModifiersTooltip(IServerUtils utils, ItemAttributeModifiers value) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, value.modifiers()).build(Lang.Branch.MODIFIERS)));
    }

    @Unmodifiable
    @NotNull
    public static TooltipBuilder getCustomModelDataTooltip(IServerUtils utils, CustomModelData value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.floats().toString()).build(Lang.Value.FLOATS));
            b.add(utils.getValueTooltip(utils, value.flags().toString()).build(Lang.Value.FLAGS));
            b.add(utils.getValueTooltip(utils, value.strings().toString()).build(Lang.Value.STRINGS));
            b.add(utils.getValueTooltip(utils, value.colors().toString()).build(Lang.Value.COLORS));
        });
    }

    @Unmodifiable
    @NotNull
    public static TooltipBuilder getTooltipDisplayTooltip(IServerUtils utils, TooltipDisplay value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.hideTooltip()).build(Lang.Value.HIDE_TOOLTIP));
            b.add(utils.getValueTooltip(utils, value.hiddenComponents()).build(Lang.Branch.HIDDEN_COMPONENTS));
        });
    }

    @Unmodifiable
    @NotNull
    public static TooltipBuilder getEmptyTooltip(IServerUtils ignoredUtils, Unit ignoredValue) {
        return TooltipBuilder.empty();
    }

    @Unmodifiable
    @NotNull
    public static TooltipBuilder getBoolTooltip(IServerUtils utils, boolean value) {
        return utils.getValueTooltip(utils, value).key(Lang.Value.VALUE);
    }

    @NotNull
    public static TooltipBuilder getFoodTooltip(IServerUtils utils, FoodProperties food) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, food.nutrition()).build(Lang.Value.NUTRITION));
            b.add(utils.getValueTooltip(utils, food.saturation()).build(Lang.Value.SATURATION));
            b.add(utils.getValueTooltip(utils, food.canAlwaysEat()).build(Lang.Value.CAN_ALWAYS_EAT));
        });
    }

    @NotNull
    public static TooltipBuilder getConsumableTooltip(IServerUtils utils, Consumable consumable) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, consumable.consumeSeconds()).build(Lang.Value.CONSUME_SECONDS));
            b.add(utils.getValueTooltip(utils, consumable.animation()).build(Lang.Value.ANIMATION));
            b.add(utils.getValueTooltip(utils, consumable.sound()).build(Lang.Value.SOUND));
            b.add(utils.getValueTooltip(utils, consumable.hasConsumeParticles()).build(Lang.Value.HAS_CUSTOM_PARTICLES));
            b.add(utils.getValueTooltip(utils, consumable.onConsumeEffects()).build(Lang.Branch.ON_CONSUME_EFFECTS));
        });
    }

    @NotNull
    public static TooltipBuilder getUseRemainderTooltip(IServerUtils utils, UseRemainder remainder) {
        return utils.getValueTooltip(utils, remainder.convertInto()).key(Lang.Branch.CONVERT_INTO);
    }

    @NotNull
    public static TooltipBuilder getUseCooldownTooltip(IServerUtils utils, UseCooldown cooldown) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, cooldown.seconds()).build(Lang.Value.SECONDS));
            b.add(utils.getValueTooltip(utils, cooldown.cooldownGroup()).build(Lang.Value.COOLDOWN_GROUP));
        });
    }

    @Unmodifiable
    @NotNull
    public static TooltipBuilder getDamageResistantTooltip(IServerUtils utils, DamageResistant resistant) {
        return utils.getValueTooltip(utils, resistant.types()).key(Lang.Branch.TYPES);
    }

    @NotNull
    public static TooltipBuilder getToolTooltip(IServerUtils utils, Tool tool) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, tool.rules()).build(Lang.Branch.RULES));
            b.add(utils.getValueTooltip(utils, tool.defaultMiningSpeed()).build(Lang.Value.DEFAULT_MINING_SPEED));
            b.add(utils.getValueTooltip(utils, tool.damagePerBlock()).build(Lang.Value.DAMAGE_PER_BLOCK));
            b.add(utils.getValueTooltip(utils, tool.canDestroyBlocksInCreative()).build(Lang.Value.CAN_DESTROY_BLOCKS_IN_CREATIVE));
        });
    }

    @NotNull
    public static TooltipBuilder getWeaponTooltip(IServerUtils utils, Weapon weapon) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, weapon.itemDamagePerAttack()).build(Lang.Value.ITEM_DAMAGE_PER_ATTACK));
            b.add(utils.getValueTooltip(utils, weapon.disableBlockingForSeconds()).build(Lang.Value.DISABLE_BLOCKING_FOR_SECONDS));
        });
    }

    @Unmodifiable
    @NotNull
    public static TooltipBuilder getEnchantableTooltip(IServerUtils utils, Enchantable enchantable) {
        return utils.getValueTooltip(utils, enchantable.value()).key(Lang.Value.VALUE);
    }

    @Unmodifiable
    @NotNull
    public static TooltipBuilder getEquipableTooltip(IServerUtils utils, Equippable equippable) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, equippable.slot()).build(Lang.Value.SLOT));
            b.add(utils.getValueTooltip(utils, equippable.equipSound()).build(Lang.Value.EQUIP_SOUND));
            b.add(utils.getValueTooltip(utils, equippable.assetId()).build(Lang.Value.ASSET_ID));
            b.add(utils.getValueTooltip(utils, equippable.cameraOverlay()).build(Lang.Value.CAMERA_OVERLAY));
            b.add(utils.getValueTooltip(utils, equippable.allowedEntities()).build(Lang.Branch.ALLOWED_ENTITIES));
            b.add(utils.getValueTooltip(utils, equippable.dispensable()).build(Lang.Value.DISPENSABLE));
            b.add(utils.getValueTooltip(utils, equippable.swappable()).build(Lang.Value.SWAPPABLE));
            b.add(utils.getValueTooltip(utils, equippable.damageOnHurt()).build(Lang.Value.DAMAGE_ON_HURT));
            b.add(utils.getValueTooltip(utils, equippable.equipOnInteract()).build(Lang.Value.EQUIP_ON_INTERACT));
        });
    }

    @Unmodifiable
    @NotNull
    public static TooltipBuilder getRepairableTooltip(IServerUtils utils, Repairable repairable) {
        return utils.getValueTooltip(utils, repairable.items()).key(Lang.Branch.ITEMS);
    }

    @Unmodifiable
    @NotNull
    public static TooltipBuilder getDeathProtectionTooltip(IServerUtils utils, DeathProtection protection) {
        return utils.getValueTooltip(utils, protection.deathEffects()).key(Lang.Branch.DEATH_EFFECTS);
    }

    @Unmodifiable
    @NotNull
    public static TooltipBuilder getBlockAttacksTooltip(IServerUtils utils, BlocksAttacks value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.blockDelaySeconds()).build(Lang.Value.BLOCK_DELAY_SECONDS));
            b.add(utils.getValueTooltip(utils, value.disableCooldownScale()).build(Lang.Value.DISABLE_COOLDOWN_SCALE));
            b.add(utils.getValueTooltip(utils, value.damageReductions()).build(Lang.Branch.DAMAGE_REDUCTIONS));
            b.add(utils.getValueTooltip(utils, value.itemDamage()).build(Lang.Branch.ITEM_DAMAGE));
            b.add(utils.getValueTooltip(utils, value.bypassedBy()).build(Lang.Branch.BYPASSED_BY));
            b.add(utils.getValueTooltip(utils, value.blockSound()).build(Lang.Value.BLOCK_SOUND));
            b.add(utils.getValueTooltip(utils, value.disableSound()).build(Lang.Value.DISABLE_SOUND));
        });
    }

    @NotNull
    public static TooltipBuilder getDyedColorTooltip(IServerUtils utils, DyedItemColor value) {
        return utils.getValueTooltip(utils, value.rgb()).key(Lang.Value.RGB);
    }

    @Unmodifiable
    @NotNull
    public static TooltipBuilder getMapColorTooltip(IServerUtils utils, MapItemColor value) {
        return utils.getValueTooltip(utils, value.rgb()).key(Lang.Value.RGB);
    }

    @Unmodifiable
    @NotNull
    public static TooltipBuilder getMapIdTooltip(IServerUtils utils, MapId value) {
        return utils.getValueTooltip(utils, value.id()).key(Lang.Value.VALUE);
    }

    @NotNull
    public static TooltipBuilder getMapDecorationsTooltip(IServerUtils utils, MapDecorations value) {
        return getMapTooltip(utils, value.decorations(), GenericTooltipUtils::getMapDecorationEntryTooltip).key(Lang.Branch.DECORATIONS);
    }

    @Unmodifiable
    @NotNull
    public static TooltipBuilder getMapPostProcessingTooltip(IServerUtils utils, MapPostProcessing value) {
        return utils.getValueTooltip(utils, value).key(Lang.Value.VALUE);
    }

    @NotNull
    public static TooltipBuilder getChargedProjectilesTooltip(IServerUtils utils, ChargedProjectiles value) {
        return utils.getValueTooltip(utils, value.items()).key(Lang.Branch.ITEMS);
    }

    @NotNull
    public static TooltipBuilder getBundleContentsTooltip(IServerUtils utils, BundleContents value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.items).build(Lang.Branch.ITEMS));
            b.add(utils.getValueTooltip(utils, value.weight().toString()).build(Lang.Value.FRACTION));
        });
    }

    @NotNull
    public static TooltipBuilder getPotionContentsTooltip(IServerUtils utils, PotionContents value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.potion()).build(Lang.Value.POTION));
            b.add(utils.getValueTooltip(utils, value.customColor()).build(Lang.Value.CUSTOM_COLOR));
            b.add(utils.getValueTooltip(utils, value.customEffects()).build(Lang.Branch.CUSTOM_EFFECTS));
            b.add(utils.getValueTooltip(utils, value.customName()).build(Lang.Value.CUSTOM_NAME));
        });
    }

    @Unmodifiable
    @NotNull
    public static TooltipBuilder getFloatValueTooltip(IServerUtils utils, float value) {
        return utils.getValueTooltip(utils, value).key(Lang.Value.VALUE);
    }

    @NotNull
    public static TooltipBuilder getSuspiciousStewEffectsTooltip(IServerUtils utils, SuspiciousStewEffects value) {
        return utils.getValueTooltip(utils, value.effects()).key(Lang.Branch.EFFECTS);
    }

    @NotNull
    public static TooltipBuilder getWritableBookContentTooltip(IServerUtils utils, WritableBookContent value) {
        return getFilterableTooltip(utils, Lang.Branch.PAGE, value.pages()).key(Lang.Branch.PAGES);
    }

    @NotNull
    public static TooltipBuilder getWrittenBookContentTooltip(IServerUtils utils, WrittenBookContent value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.title()).build(Lang.Branch.TITLE));
            b.add(utils.getValueTooltip(utils, value.author()).build(Lang.Value.AUTHOR));
            b.add(utils.getValueTooltip(utils, value.generation()).build(Lang.Value.GENERATION));
            b.add(getFilterableTooltip(utils, Lang.Branch.PAGE, value.pages()).build(Lang.Branch.PAGES));
            b.add(utils.getValueTooltip(utils, value.resolved()).build(Lang.Value.RESOLVED));
        });
    }

    @NotNull
    public static TooltipBuilder getTrimTooltip(IServerUtils utils, ArmorTrim value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.material().value()).build(Lang.Value.MATERIAL));
            b.add(utils.getValueTooltip(utils, value.pattern().value()).build(Lang.Value.PATTERN));
        });
    }

    @NotNull
    public static TooltipBuilder getDebugStickStateTooltip(IServerUtils utils, DebugStickState value) {
        return getMapTooltip(utils, value.properties(), GenericTooltipUtils::getBlockPropertyEntryTooltip).key(Lang.Branch.PROPERTIES);
    }

    @Unmodifiable
    @NotNull
    public static TooltipBuilder getInstrumentTooltip(IServerUtils utils, InstrumentComponent value) {
        return utils.getValueTooltip(utils, value.instrument()).key(Lang.Value.VALUE);
    }

    @Unmodifiable
    @NotNull
    public static TooltipBuilder getOminousBottleAmplifierTooltip(IServerUtils utils, OminousBottleAmplifier value) {
        return utils.getValueTooltip(utils, value.value()).key(Lang.Value.VALUE);
    }

    @NotNull
    public static TooltipBuilder getJukeboxPlayableTooltip(IServerUtils utils, JukeboxPlayable value) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, value.song()).build(Lang.Value.SONG)));
    }

    @Unmodifiable
    @NotNull
    public static TooltipBuilder getProvidesBannerPatternsTooltip(IServerUtils utils, HolderSet<BannerPattern> value) {
        return utils.getValueTooltip(utils, value).key(Lang.Branch.BANNER_PATTERNS);
    }

    @Unmodifiable
    @NotNull
    public static TooltipBuilder getRecipesTooltip(IServerUtils utils, List<ResourceKey<Recipe<?>>> value) {
        return utils.getValueTooltip(utils, value).key(Lang.Branch.RECIPES);
    }

    @NotNull
    public static TooltipBuilder getLodestoneTrackerTooltip(IServerUtils utils, LodestoneTracker value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.target()).build(Lang.Branch.GLOBAL_POS));
            b.add(utils.getValueTooltip(utils, value.tracked()).build(Lang.Value.TRACKED));
        });
    }

    @NotNull
    public static TooltipBuilder getFireworkExplosionTooltip(IServerUtils utils, FireworkExplosion value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.shape()).build(Lang.Value.SHAPE));
            b.add(utils.getValueTooltip(utils, value.colors().toString()).build(Lang.Value.COLORS));
            b.add(utils.getValueTooltip(utils, value.fadeColors().toString()).build(Lang.Value.FADE_COLORS));
            b.add(utils.getValueTooltip(utils, value.hasTrail()).build(Lang.Value.HAS_TRAIL));
            b.add(utils.getValueTooltip(utils, value.hasTwinkle()).build(Lang.Value.HAS_TWINKLE));
        });
    }

    @NotNull
    public static TooltipBuilder getFireworksTooltip(IServerUtils utils, Fireworks value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.flightDuration()).build(Lang.Value.FLIGHT_DURATION));
            b.add(utils.getValueTooltip(utils, value.explosions()).build(Lang.Branch.EXPLOSIONS));
        });
    }

    @NotNull
    public static TooltipBuilder getProfileTooltip(IServerUtils utils, ResolvableProfile value) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, value.name()).build(Lang.Value.NAME)));
    }

    @NotNull
    public static TooltipBuilder getIdentifierTooltip(IServerUtils utils, Identifier value) {
        return utils.getValueTooltip(utils, value).key(Lang.Value.VALUE);
    }

    @Unmodifiable
    @NotNull
    public static TooltipBuilder getBannerPatternsTooltip(IServerUtils utils, BannerPatternLayers value) {
        return utils.getValueTooltip(utils, value).key(Lang.Branch.BANNER_PATTERNS);
    }

    @Unmodifiable
    @NotNull
    public static TooltipBuilder getDyeColorTooltip(IServerUtils utils, DyeColor value) {
        return utils.getValueTooltip(utils, value).key(Lang.Value.COLOR);
    }

    @NotNull
    public static TooltipBuilder getPotDecorationsTooltip(IServerUtils utils, PotDecorations value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.back()).build(Lang.Value.BACK));
            b.add(utils.getValueTooltip(utils, value.left()).build(Lang.Value.LEFT));
            b.add(utils.getValueTooltip(utils, value.right()).build(Lang.Value.RIGHT));
            b.add(utils.getValueTooltip(utils, value.front()).build(Lang.Value.FRONT));
        });
    }

    @NotNull
    public static TooltipBuilder getContainerTooltip(IServerUtils utils, ItemContainerContents value) {
        return utils.getValueTooltip(utils, value.items).key(Lang.Branch.ITEMS);
    }

    @NotNull
    public static TooltipBuilder getBlockStateTooltip(IServerUtils utils, BlockItemStateProperties properties) {
        return getMapTooltip(utils, properties.properties(), GenericTooltipUtils::getStringEntryTooltip).key(Lang.Branch.PROPERTIES);
    }

    @NotNull
    public static TooltipBuilder getBeesTooltip(IServerUtils utils, Bees bees) {
        return utils.getValueTooltip(utils, bees.bees()).key(Lang.Branch.BEES);
    }

    @Unmodifiable
    @NotNull
    public static TooltipBuilder getLockTooltip(IServerUtils utils, LockCode lockCode) {
        return utils.getValueTooltip(utils, lockCode.predicate()).key(Lang.Branch.PREDICATE);
    }

    @NotNull
    public static TooltipBuilder getContainerLootTooltip(IServerUtils utils, SeededContainerLoot value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.lootTable()).build(Lang.Value.LOOT_TABLE));
            b.add(utils.getValueTooltip(utils, value.seed()).build(Lang.Value.SEED));
        });
    }

    @Unmodifiable
    @NotNull
    public static TooltipBuilder getHolderTooltip(IServerUtils utils, Holder<?> value) {
        return utils.getValueTooltip(utils, value).key(Lang.Value.VALUE);
    }

    @Unmodifiable
    @NotNull
    public static TooltipBuilder getEnumTypeTooltip(IServerUtils utils, Enum<?> type) {
        return utils.getValueTooltip(utils, type).key(Lang.Value.TYPE);
    }

    @Unmodifiable
    @NotNull
    public static TooltipBuilder getTypedEntityDataTooltip(IServerUtils utils, TypedEntityData<?> value) {
        return utils.getValueTooltip(utils, value.copyTagWithoutId().toString()).key(Lang.Value.TAG);
    }

    @Unmodifiable
    @NotNull
    public static TooltipBuilder getSwingAnimationTooltip(IServerUtils utils, SwingAnimation value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.type()).build(Lang.Value.TYPE));
            b.add(utils.getValueTooltip(utils, value.duration()).build(Lang.Value.DURATION));
        });
    }

    @Unmodifiable
    @NotNull
    public static TooltipBuilder getUseEffectsTooltip(IServerUtils utils, UseEffects value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.canSprint()).build(Lang.Value.CAN_SPRINT));
            b.add(utils.getValueTooltip(utils, value.interactVibrations()).build(Lang.Value.INTERACT_VIBRATIONS));
            b.add(utils.getValueTooltip(utils, value.speedMultiplier()).build(Lang.Value.SPEED_MULTIPLIER));
        });
    }

    @NotNull
    public static TooltipBuilder getAttackRangeTooltip(IServerUtils utils, AttackRange value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.minReach()).build(Lang.Value.MIN_REACH));
            b.add(utils.getValueTooltip(utils, value.maxReach()).build(Lang.Value.MAX_REACH));
            b.add(utils.getValueTooltip(utils, value.minCreativeReach()).build(Lang.Value.MIN_CREATIVE_REACH));
            b.add(utils.getValueTooltip(utils, value.maxCreativeReach()).build(Lang.Value.MAX_CREATIVE_REACH));
            b.add(utils.getValueTooltip(utils, value.hitboxMargin()).build(Lang.Value.HITBOX_MARGIN));
            b.add(utils.getValueTooltip(utils, value.mobFactor()).build(Lang.Value.MOB_FACTOR));
        });
    }

    @NotNull
    public static TooltipBuilder getPiercingWeaponTooltip(IServerUtils utils, PiercingWeapon value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.dealsKnockback()).build(Lang.Value.DEALS_KNOCKBACK));
            b.add(utils.getValueTooltip(utils, value.dismounts()).build(Lang.Value.DISMOUNTS));
            b.add(utils.getValueTooltip(utils, value.sound()).build(Lang.Value.SOUND));
            b.add(utils.getValueTooltip(utils, value.hitSound()).build(Lang.Value.HIT_SOUND));
        });
    }

    @NotNull
    public static TooltipBuilder getKineticWeaponTooltip(IServerUtils utils, KineticWeapon value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.contactCooldownTicks()).build(Lang.Value.CONTACT_COOLDOWN_TICKS));
            b.add(utils.getValueTooltip(utils, value.delayTicks()).build(Lang.Value.DELAY_TICKS));
            b.add(utils.getValueTooltip(utils, value.dismountConditions()).build(Lang.Branch.DISMOUNT_CONDITION));
            b.add(utils.getValueTooltip(utils, value.knockbackConditions()).build(Lang.Branch.KNOCKBACK_CONDITION));
            b.add(utils.getValueTooltip(utils, value.damageConditions()).build(Lang.Branch.DAMAGE_CONDITION));
            b.add(utils.getValueTooltip(utils, value.forwardMovement()).build(Lang.Value.FORWARD_MOVEMENT));
            b.add(utils.getValueTooltip(utils, value.damageMultiplier()).build(Lang.Value.DAMAGE_MULTIPLIER));
            b.add(utils.getValueTooltip(utils, value.sound()).build(Lang.Value.SOUND));
            b.add(utils.getValueTooltip(utils, value.hitSound()).build(Lang.Value.HIT_SOUND));
        });
    }
}
