package net.minecraft.world.item.enchantment;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.effects.EnchantmentAttributeEffect;
import net.minecraft.world.item.enchantment.providers.EnchantmentProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableObject;

public class EnchantmentHelper {
    /**
     * @deprecated Neo: Use {@link #getTagEnchantmentLevel(Holder, ItemStack)} for NBT
     *             enchantments, or {@link ItemStack#getEnchantmentLevel(Holder)} for
     *             gameplay.
     */
    @Deprecated
    public static int getItemEnchantmentLevel(Holder<Enchantment> pEnchantment, ItemStack pStack) {
        // Neo: To reduce patch size, update this method to always check gameplay enchantments, and add getTagEnchantmentLevel as a helper for mods.
        return pStack.getEnchantmentLevel(pEnchantment);
    }

    /**
     * Gets the level of an enchantment from NBT. Use {@link ItemStack#getEnchantmentLevel(Holder)} for gameplay logic.
     */
    public static int getTagEnchantmentLevel(Holder<Enchantment> p_346179_, ItemStack p_44845_) {
        ItemEnchantments itemenchantments = p_44845_.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        return itemenchantments.getLevel(p_346179_);
    }

    public static ItemEnchantments updateEnchantments(ItemStack pStack, Consumer<ItemEnchantments.Mutable> pUpdater) {
        DataComponentType<ItemEnchantments> datacomponenttype = getComponentType(pStack);
        ItemEnchantments itemenchantments = pStack.get(datacomponenttype);
        if (itemenchantments == null) {
            return ItemEnchantments.EMPTY;
        } else {
            ItemEnchantments.Mutable itemenchantments$mutable = new ItemEnchantments.Mutable(itemenchantments);
            pUpdater.accept(itemenchantments$mutable);
            ItemEnchantments itemenchantments1 = itemenchantments$mutable.toImmutable();
            pStack.set(datacomponenttype, itemenchantments1);
            return itemenchantments1;
        }
    }

    public static boolean canStoreEnchantments(ItemStack pStack) {
        return pStack.has(getComponentType(pStack));
    }

    public static void setEnchantments(ItemStack pStack, ItemEnchantments pEnchantments) {
        pStack.set(getComponentType(pStack), pEnchantments);
    }

    public static ItemEnchantments getEnchantmentsForCrafting(ItemStack pStack) {
        return pStack.getOrDefault(getComponentType(pStack), ItemEnchantments.EMPTY);
    }

    private static DataComponentType<ItemEnchantments> getComponentType(ItemStack pStack) {
        return pStack.is(Items.ENCHANTED_BOOK) ? DataComponents.STORED_ENCHANTMENTS : DataComponents.ENCHANTMENTS;
    }

    public static boolean hasAnyEnchantments(ItemStack pStack) {
        return !pStack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).isEmpty()
            || !pStack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY).isEmpty();
    }

    public static int processDurabilityChange(ServerLevel pLevel, ItemStack pStack, int pDamage) {
        MutableFloat mutablefloat = new MutableFloat((float)pDamage);
        runIterationOnItem(pStack, (p_344593_, p_344594_) -> p_344593_.value().modifyDurabilityChange(pLevel, p_344594_, pStack, mutablefloat));
        return mutablefloat.intValue();
    }

    public static int processAmmoUse(ServerLevel pLevel, ItemStack pWeapon, ItemStack pAmmo, int pCount) {
        MutableFloat mutablefloat = new MutableFloat((float)pCount);
        runIterationOnItem(pWeapon, (p_344545_, p_344546_) -> p_344545_.value().modifyAmmoCount(pLevel, p_344546_, pAmmo, mutablefloat));
        return mutablefloat.intValue();
    }

    public static int processBlockExperience(ServerLevel pLevel, ItemStack pStack, int pExperience) {
        MutableFloat mutablefloat = new MutableFloat((float)pExperience);
        runIterationOnItem(pStack, (p_344491_, p_344492_) -> p_344491_.value().modifyBlockExperience(pLevel, p_344492_, pStack, mutablefloat));
        return mutablefloat.intValue();
    }

    public static int processMobExperience(ServerLevel pLevel, @Nullable Entity pKiller, Entity pMob, int pExperience) {
        if (pKiller instanceof LivingEntity livingentity) {
            MutableFloat mutablefloat = new MutableFloat((float)pExperience);
            runIterationOnEquipment(
                livingentity,
                (p_344574_, p_344575_, p_344576_) -> p_344574_.value()
                        .modifyMobExperience(pLevel, p_344575_, p_344576_.itemStack(), pMob, mutablefloat)
            );
            return mutablefloat.intValue();
        } else {
            return pExperience;
        }
    }

    private static void runIterationOnItem(ItemStack pStack, EnchantmentHelper.EnchantmentVisitor pVisitor) {
        ItemEnchantments itemenchantments = pStack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);

        // Neo: Respect gameplay-only enchantments when doing iterations
        var lookup = net.neoforged.neoforge.common.CommonHooks.resolveLookup(net.minecraft.core.registries.Registries.ENCHANTMENT);
        if (lookup != null) {
            itemenchantments = pStack.getAllEnchantments(lookup);
        }

        for (Entry<Holder<Enchantment>> entry : itemenchantments.entrySet()) {
            pVisitor.accept(entry.getKey(), entry.getIntValue());
        }
    }

    private static void runIterationOnItem(
        ItemStack pStack, EquipmentSlot pSlot, LivingEntity pEntity, EnchantmentHelper.EnchantmentInSlotVisitor pVisitor
    ) {
        if (!pStack.isEmpty()) {
            ItemEnchantments itemenchantments = pStack.get(DataComponents.ENCHANTMENTS);

            // Neo: Respect gameplay-only enchantments when doing iterations
            itemenchantments = pStack.getAllEnchantments(pEntity.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT));

            if (itemenchantments != null && !itemenchantments.isEmpty()) {
                EnchantedItemInUse enchantediteminuse = new EnchantedItemInUse(pStack, pSlot, pEntity);

                for (Entry<Holder<Enchantment>> entry : itemenchantments.entrySet()) {
                    Holder<Enchantment> holder = entry.getKey();
                    if (holder.value().matchingSlot(pSlot)) {
                        pVisitor.accept(holder, entry.getIntValue(), enchantediteminuse);
                    }
                }
            }
        }
    }

    private static void runIterationOnEquipment(LivingEntity pEntity, EnchantmentHelper.EnchantmentInSlotVisitor pVisitor) {
        for (EquipmentSlot equipmentslot : EquipmentSlot.values()) {
            runIterationOnItem(pEntity.getItemBySlot(equipmentslot), equipmentslot, pEntity, pVisitor);
        }
    }

    public static boolean isImmuneToDamage(ServerLevel pLevel, LivingEntity pEntity, DamageSource pDamageSource) {
        MutableBoolean mutableboolean = new MutableBoolean();
        runIterationOnEquipment(
            pEntity,
            (p_344534_, p_344535_, p_344536_) -> mutableboolean.setValue(
                    mutableboolean.isTrue() || p_344534_.value().isImmuneToDamage(pLevel, p_344535_, pEntity, pDamageSource)
                )
        );
        return mutableboolean.isTrue();
    }

    public static float getDamageProtection(ServerLevel pLevel, LivingEntity pEntity, DamageSource pDamageSource) {
        MutableFloat mutablefloat = new MutableFloat(0.0F);
        runIterationOnEquipment(
            pEntity,
            (p_344604_, p_344605_, p_344606_) -> p_344604_.value()
                    .modifyDamageProtection(pLevel, p_344605_, p_344606_.itemStack(), pEntity, pDamageSource, mutablefloat)
        );
        return mutablefloat.floatValue();
    }

    public static float modifyDamage(ServerLevel pLevel, ItemStack pTool, Entity pEntity, DamageSource pDamageSource, float pDamage) {
        MutableFloat mutablefloat = new MutableFloat(pDamage);
        runIterationOnItem(
            pTool, (p_344525_, p_344526_) -> p_344525_.value().modifyDamage(pLevel, p_344526_, pTool, pEntity, pDamageSource, mutablefloat)
        );
        return mutablefloat.floatValue();
    }

    public static float modifyFallBasedDamage(ServerLevel pLevel, ItemStack pTool, Entity pEnity, DamageSource pDamageSource, float pFallBasedDamage) {
        MutableFloat mutablefloat = new MutableFloat(pFallBasedDamage);
        runIterationOnItem(
            pTool, (p_344552_, p_344553_) -> p_344552_.value().modifyFallBasedDamage(pLevel, p_344553_, pTool, pEnity, pDamageSource, mutablefloat)
        );
        return mutablefloat.floatValue();
    }

    public static float modifyArmorEffectiveness(ServerLevel pLevel, ItemStack pTool, Entity pEntity, DamageSource pDamageSource, float pArmorEffectiveness) {
        MutableFloat mutablefloat = new MutableFloat(pArmorEffectiveness);
        runIterationOnItem(
            pTool, (p_344468_, p_344469_) -> p_344468_.value().modifyArmorEffectivness(pLevel, p_344469_, pTool, pEntity, pDamageSource, mutablefloat)
        );
        return mutablefloat.floatValue();
    }

    public static float modifyKnockback(ServerLevel pLevel, ItemStack pTool, Entity pEntity, DamageSource pDamageSource, float pKnockback) {
        MutableFloat mutablefloat = new MutableFloat(pKnockback);
        runIterationOnItem(
            pTool, (p_344446_, p_344447_) -> p_344446_.value().modifyKnockback(pLevel, p_344447_, pTool, pEntity, pDamageSource, mutablefloat)
        );
        return mutablefloat.floatValue();
    }

    public static void doPostAttackEffects(ServerLevel pLevel, Entity pEntity, DamageSource pDamageSource) {
        if (pDamageSource.getEntity() instanceof LivingEntity livingentity) {
            doPostAttackEffectsWithItemSource(pLevel, pEntity, pDamageSource, livingentity.getWeaponItem());
        } else {
            doPostAttackEffectsWithItemSource(pLevel, pEntity, pDamageSource, null);
        }
    }

    public static void doPostAttackEffectsWithItemSource(ServerLevel pLevel, Entity pEntity, DamageSource pDamageSource, @Nullable ItemStack pItemSource) {
        if (pEntity instanceof LivingEntity livingentity) {
            runIterationOnEquipment(
                livingentity,
                (p_344427_, p_344428_, p_344429_) -> p_344427_.value()
                        .doPostAttack(pLevel, p_344428_, p_344429_, EnchantmentTarget.VICTIM, pEntity, pDamageSource)
            );
        }

        if (pItemSource != null && pDamageSource.getEntity() instanceof LivingEntity livingentity1) {
            runIterationOnItem(
                pItemSource,
                EquipmentSlot.MAINHAND,
                livingentity1,
                (p_344557_, p_344558_, p_344559_) -> p_344557_.value()
                        .doPostAttack(pLevel, p_344558_, p_344559_, EnchantmentTarget.ATTACKER, pEntity, pDamageSource)
            );
        }
    }

    public static void runLocationChangedEffects(ServerLevel pLevel, LivingEntity pEntity) {
        runIterationOnEquipment(
            pEntity, (p_344496_, p_344497_, p_344498_) -> p_344496_.value().runLocationChangedEffects(pLevel, p_344497_, p_344498_, pEntity)
        );
    }

    public static void runLocationChangedEffects(ServerLevel pLevel, ItemStack pStack, LivingEntity pEntity, EquipmentSlot pSlot) {
        runIterationOnItem(
            pStack,
            pSlot,
            pEntity,
            (p_344615_, p_344616_, p_344617_) -> p_344615_.value().runLocationChangedEffects(pLevel, p_344616_, p_344617_, pEntity)
        );
    }

    public static void stopLocationBasedEffects(LivingEntity pEntity) {
        runIterationOnEquipment(pEntity, (p_344643_, p_344644_, p_344645_) -> p_344643_.value().stopLocationBasedEffects(p_344644_, p_344645_, pEntity));
    }

    public static void stopLocationBasedEffects(ItemStack pStack, LivingEntity pEntity, EquipmentSlot pSlot) {
        runIterationOnItem(
            pStack, pSlot, pEntity, (p_344480_, p_344481_, p_344482_) -> p_344480_.value().stopLocationBasedEffects(p_344481_, p_344482_, pEntity)
        );
    }

    public static void tickEffects(ServerLevel pLevel, LivingEntity pEntity) {
        runIterationOnEquipment(pEntity, (p_344432_, p_344433_, p_344434_) -> p_344432_.value().tick(pLevel, p_344433_, p_344434_, pEntity));
    }

    public static int getEnchantmentLevel(Holder<Enchantment> pEnchantment, LivingEntity pEntity) {
        Iterable<ItemStack> iterable = pEnchantment.value().getSlotItems(pEntity).values();
        int i = 0;

        for (ItemStack itemstack : iterable) {
            int j = getItemEnchantmentLevel(pEnchantment, itemstack);
            if (j > i) {
                i = j;
            }
        }

        return i;
    }

    public static int processProjectileCount(ServerLevel pLevel, ItemStack pTool, Entity pEntity, int pProjectileCount) {
        MutableFloat mutablefloat = new MutableFloat((float)pProjectileCount);
        runIterationOnItem(
            pTool, (p_344634_, p_344635_) -> p_344634_.value().modifyProjectileCount(pLevel, p_344635_, pTool, pEntity, mutablefloat)
        );
        return Math.max(0, mutablefloat.intValue());
    }

    public static float processProjectileSpread(ServerLevel pLevel, ItemStack pTool, Entity pEntity, float pProjectileSpread) {
        MutableFloat mutablefloat = new MutableFloat(pProjectileSpread);
        runIterationOnItem(
            pTool, (p_344474_, p_344475_) -> p_344474_.value().modifyProjectileSpread(pLevel, p_344475_, pTool, pEntity, mutablefloat)
        );
        return Math.max(0.0F, mutablefloat.floatValue());
    }

    public static int getPiercingCount(ServerLevel pLevel, ItemStack pFiredFromWeapon, ItemStack pPickupItemStack) {
        MutableFloat mutablefloat = new MutableFloat(0.0F);
        runIterationOnItem(pFiredFromWeapon, (p_344598_, p_344599_) -> p_344598_.value().modifyPiercingCount(pLevel, p_344599_, pPickupItemStack, mutablefloat));
        return Math.max(0, mutablefloat.intValue());
    }

    public static void onProjectileSpawned(ServerLevel pLevel, ItemStack pFiredFromWeapon, AbstractArrow pArrow, Consumer<Item> pOnBreak) {
        LivingEntity livingentity = pArrow.getOwner() instanceof LivingEntity livingentity1 ? livingentity1 : null;
        EnchantedItemInUse enchantediteminuse = new EnchantedItemInUse(pFiredFromWeapon, null, livingentity, pOnBreak);
        runIterationOnItem(pFiredFromWeapon, (p_344580_, p_344581_) -> p_344580_.value().onProjectileSpawned(pLevel, p_344581_, enchantediteminuse, pArrow));
    }

    public static void onHitBlock(
        ServerLevel pLevel,
        ItemStack pStack,
        @Nullable LivingEntity pOwner,
        Entity pEntity,
        @Nullable EquipmentSlot pSlot,
        Vec3 pPos,
        BlockState pState,
        Consumer<Item> pOnBreak
    ) {
        EnchantedItemInUse enchantediteminuse = new EnchantedItemInUse(pStack, pSlot, pOwner, pOnBreak);
        runIterationOnItem(
            pStack, (p_350196_, p_350197_) -> p_350196_.value().onHitBlock(pLevel, p_350197_, enchantediteminuse, pEntity, pPos, pState)
        );
    }

    public static int modifyDurabilityToRepairFromXp(ServerLevel pLevel, ItemStack pStack, int pDuabilityToRepairFromXp) {
        MutableFloat mutablefloat = new MutableFloat((float)pDuabilityToRepairFromXp);
        runIterationOnItem(pStack, (p_344540_, p_344541_) -> p_344540_.value().modifyDurabilityToRepairFromXp(pLevel, p_344541_, pStack, mutablefloat));
        return Math.max(0, mutablefloat.intValue());
    }

    public static float processEquipmentDropChance(ServerLevel pLevel, LivingEntity pEntity, DamageSource pDamageSource, float pEquipmentDropChance) {
        MutableFloat mutablefloat = new MutableFloat(pEquipmentDropChance);
        RandomSource randomsource = pEntity.getRandom();
        runIterationOnEquipment(pEntity, (p_347320_, p_347321_, p_347322_) -> {
            LootContext lootcontext = Enchantment.damageContext(pLevel, p_347321_, pEntity, pDamageSource);
            p_347320_.value().getEffects(EnchantmentEffectComponents.EQUIPMENT_DROPS).forEach(p_347345_ -> {
                if (p_347345_.enchanted() == EnchantmentTarget.VICTIM && p_347345_.affected() == EnchantmentTarget.VICTIM && p_347345_.matches(lootcontext)) {
                    mutablefloat.setValue(p_347345_.effect().process(p_347321_, randomsource, mutablefloat.floatValue()));
                }
            });
        });
        if (pDamageSource.getEntity() instanceof LivingEntity livingentity) {
            runIterationOnEquipment(
                livingentity,
                (p_347338_, p_347339_, p_347340_) -> {
                    LootContext lootcontext = Enchantment.damageContext(pLevel, p_347339_, pEntity, pDamageSource);
                    p_347338_.value()
                        .getEffects(EnchantmentEffectComponents.EQUIPMENT_DROPS)
                        .forEach(
                            p_347327_ -> {
                                if (p_347327_.enchanted() == EnchantmentTarget.ATTACKER
                                    && p_347327_.affected() == EnchantmentTarget.VICTIM
                                    && p_347327_.matches(lootcontext)) {
                                    mutablefloat.setValue(p_347327_.effect().process(p_347339_, randomsource, mutablefloat.floatValue()));
                                }
                            }
                        );
                }
            );
        }

        return mutablefloat.floatValue();
    }

    public static void forEachModifier(ItemStack pStack, EquipmentSlotGroup pSlotGroup, BiConsumer<Holder<Attribute>, AttributeModifier> pAction) {
        runIterationOnItem(pStack, (p_344461_, p_344462_) -> p_344461_.value().getEffects(EnchantmentEffectComponents.ATTRIBUTES).forEach(p_350185_ -> {
                if (((Enchantment)p_344461_.value()).definition().slots().contains(pSlotGroup)) {
                    pAction.accept(p_350185_.attribute(), p_350185_.getModifier(p_344462_, pSlotGroup));
                }
            }));
    }

    public static void forEachModifier(ItemStack pStack, EquipmentSlot pSlot, BiConsumer<Holder<Attribute>, AttributeModifier> pAction) {
        runIterationOnItem(pStack, (p_348409_, p_348410_) -> p_348409_.value().getEffects(EnchantmentEffectComponents.ATTRIBUTES).forEach(p_350180_ -> {
                if (((Enchantment)p_348409_.value()).matchingSlot(pSlot)) {
                    pAction.accept(p_350180_.attribute(), p_350180_.getModifier(p_348410_, pSlot));
                }
            }));
    }

    public static int getFishingLuckBonus(ServerLevel pLevel, ItemStack pStack, Entity pEntity) {
        MutableFloat mutablefloat = new MutableFloat(0.0F);
        runIterationOnItem(
            pStack, (p_344564_, p_344565_) -> p_344564_.value().modifyFishingLuckBonus(pLevel, p_344565_, pStack, pEntity, mutablefloat)
        );
        return Math.max(0, mutablefloat.intValue());
    }

    public static float getFishingTimeReduction(ServerLevel pLevel, ItemStack pStack, Entity pEntity) {
        MutableFloat mutablefloat = new MutableFloat(0.0F);
        runIterationOnItem(
            pStack, (p_344611_, p_344612_) -> p_344611_.value().modifyFishingTimeReduction(pLevel, p_344612_, pStack, pEntity, mutablefloat)
        );
        return Math.max(0.0F, mutablefloat.floatValue());
    }

    public static int getTridentReturnToOwnerAcceleration(ServerLevel pLevel, ItemStack pStack, Entity pEntity) {
        MutableFloat mutablefloat = new MutableFloat(0.0F);
        runIterationOnItem(
            pStack,
            (p_344516_, p_344517_) -> p_344516_.value().modifyTridentReturnToOwnerAcceleration(pLevel, p_344517_, pStack, pEntity, mutablefloat)
        );
        return Math.max(0, mutablefloat.intValue());
    }

    public static float modifyCrossbowChargingTime(ItemStack pStack, LivingEntity pEntity, float pCrossbowChargingTime) {
        MutableFloat mutablefloat = new MutableFloat(pCrossbowChargingTime);
        runIterationOnItem(pStack, (p_352869_, p_352870_) -> p_352869_.value().modifyCrossbowChargeTime(pEntity.getRandom(), p_352870_, mutablefloat));
        return Math.max(0.0F, mutablefloat.floatValue());
    }

    public static float getTridentSpinAttackStrength(ItemStack pStack, LivingEntity pEntity) {
        MutableFloat mutablefloat = new MutableFloat(0.0F);
        runIterationOnItem(
            pStack, (p_352865_, p_352866_) -> p_352865_.value().modifyTridentSpinAttackStrength(pEntity.getRandom(), p_352866_, mutablefloat)
        );
        return mutablefloat.floatValue();
    }

    public static boolean hasTag(ItemStack pStack, TagKey<Enchantment> pTag) {
        ItemEnchantments itemenchantments = pStack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);

        // Neo: Respect gameplay-only enchantments when enchantment effect tag checks
        var lookup = net.neoforged.neoforge.common.CommonHooks.resolveLookup(net.minecraft.core.registries.Registries.ENCHANTMENT);
        if (lookup != null) {
            itemenchantments = pStack.getAllEnchantments(lookup);
        }

        for (Entry<Holder<Enchantment>> entry : itemenchantments.entrySet()) {
            Holder<Enchantment> holder = entry.getKey();
            if (holder.is(pTag)) {
                return true;
            }
        }

        return false;
    }

    public static boolean has(ItemStack pStack, DataComponentType<?> pComponentType) {
        MutableBoolean mutableboolean = new MutableBoolean(false);
        runIterationOnItem(pStack, (p_344620_, p_344621_) -> {
            if (p_344620_.value().effects().has(pComponentType)) {
                mutableboolean.setTrue();
            }
        });
        return mutableboolean.booleanValue();
    }

    public static <T> Optional<T> pickHighestLevel(ItemStack pStack, DataComponentType<List<T>> pComponentType) {
        Pair<List<T>, Integer> pair = getHighestLevel(pStack, pComponentType);
        if (pair != null) {
            List<T> list = pair.getFirst();
            int i = pair.getSecond();
            return Optional.of(list.get(Math.min(i, list.size()) - 1));
        } else {
            return Optional.empty();
        }
    }

    @Nullable
    public static <T> Pair<T, Integer> getHighestLevel(ItemStack pStack, DataComponentType<T> pComponentType) {
        MutableObject<Pair<T, Integer>> mutableobject = new MutableObject<>();
        runIterationOnItem(pStack, (p_344457_, p_344458_) -> {
            if (mutableobject.getValue() == null || mutableobject.getValue().getSecond() < p_344458_) {
                T t = p_344457_.value().effects().get(pComponentType);
                if (t != null) {
                    mutableobject.setValue(Pair.of(t, p_344458_));
                }
            }
        });
        return mutableobject.getValue();
    }

    public static Optional<EnchantedItemInUse> getRandomItemWith(DataComponentType<?> pComponentType, LivingEntity pEntity, Predicate<ItemStack> pFilter) {
        List<EnchantedItemInUse> list = new ArrayList<>();

        for (EquipmentSlot equipmentslot : EquipmentSlot.values()) {
            ItemStack itemstack = pEntity.getItemBySlot(equipmentslot);
            if (pFilter.test(itemstack)) {
                ItemEnchantments itemenchantments = itemstack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);

                for (Entry<Holder<Enchantment>> entry : itemenchantments.entrySet()) {
                    Holder<Enchantment> holder = entry.getKey();
                    if (holder.value().effects().has(pComponentType) && holder.value().matchingSlot(equipmentslot)) {
                        list.add(new EnchantedItemInUse(itemstack, equipmentslot, pEntity));
                    }
                }
            }
        }

        return Util.getRandomSafe(list, pEntity.getRandom());
    }

    /**
     * Returns the enchantability of itemstack, using a separate calculation for each enchantNum (0, 1 or 2), cutting to the max enchantability power of the table, which is locked to a max of 15.
     */
    public static int getEnchantmentCost(RandomSource pRandom, int pEnchantNum, int pPower, ItemStack pStack) {
        Item item = pStack.getItem();
        int i = pStack.getEnchantmentValue();
        if (i <= 0) {
            return 0;
        } else {
            if (pPower > 15) {
                pPower = 15;
            }

            int j = pRandom.nextInt(8) + 1 + (pPower >> 1) + pRandom.nextInt(pPower + 1);
            if (pEnchantNum == 0) {
                return Math.max(j / 3, 1);
            } else {
                return pEnchantNum == 1 ? j * 2 / 3 + 1 : Math.max(j, pPower * 2);
            }
        }
    }

    public static ItemStack enchantItem(
        RandomSource pRandom, ItemStack pStack, int pLevel, RegistryAccess pRegistryAccess, Optional<? extends HolderSet<Enchantment>> pPossibleEnchantments
    ) {
        return enchantItem(
            pRandom,
            pStack,
            pLevel,
            pPossibleEnchantments.map(HolderSet::stream)
                .orElseGet(() -> pRegistryAccess.registryOrThrow(Registries.ENCHANTMENT).holders().map(p_344499_ -> (Holder<Enchantment>)p_344499_))
        );
    }

    public static ItemStack enchantItem(RandomSource pRandom, ItemStack pStack, int pLevel, Stream<Holder<Enchantment>> pPossibleEnchantments) {
        List<EnchantmentInstance> list = selectEnchantment(pRandom, pStack, pLevel, pPossibleEnchantments);
        if (pStack.is(Items.BOOK)) {
            pStack = new ItemStack(Items.ENCHANTED_BOOK);
        }

        for (EnchantmentInstance enchantmentinstance : list) {
            pStack.enchant(enchantmentinstance.enchantment, enchantmentinstance.level);
        }

        return pStack;
    }

    public static List<EnchantmentInstance> selectEnchantment(RandomSource pRandom, ItemStack pStack, int pLevel, Stream<Holder<Enchantment>> pPossibleEnchantments) {
        List<EnchantmentInstance> list = Lists.newArrayList();
        Item item = pStack.getItem();
        int i = pStack.getEnchantmentValue();
        if (i <= 0) {
            return list;
        } else {
            pLevel += 1 + pRandom.nextInt(i / 4 + 1) + pRandom.nextInt(i / 4 + 1);
            float f = (pRandom.nextFloat() + pRandom.nextFloat() - 1.0F) * 0.15F;
            pLevel = Mth.clamp(Math.round((float)pLevel + (float)pLevel * f), 1, Integer.MAX_VALUE);
            List<EnchantmentInstance> list1 = getAvailableEnchantmentResults(pLevel, pStack, pPossibleEnchantments);
            if (!list1.isEmpty()) {
                WeightedRandom.getRandomItem(pRandom, list1).ifPresent(list::add);

                while (pRandom.nextInt(50) <= pLevel) {
                    if (!list.isEmpty()) {
                        filterCompatibleEnchantments(list1, Util.lastOf(list));
                    }

                    if (list1.isEmpty()) {
                        break;
                    }

                    WeightedRandom.getRandomItem(pRandom, list1).ifPresent(list::add);
                    pLevel /= 2;
                }
            }

            return list;
        }
    }

    public static void filterCompatibleEnchantments(List<EnchantmentInstance> pDataList, EnchantmentInstance pData) {
        pDataList.removeIf(p_344519_ -> !Enchantment.areCompatible(pData.enchantment, p_344519_.enchantment));
    }

    public static boolean isEnchantmentCompatible(Collection<Holder<Enchantment>> pCurrentEnchantments, Holder<Enchantment> pNewEnchantment) {
        for (Holder<Enchantment> holder : pCurrentEnchantments) {
            if (!Enchantment.areCompatible(holder, pNewEnchantment)) {
                return false;
            }
        }

        return true;
    }

    public static List<EnchantmentInstance> getAvailableEnchantmentResults(int pLevel, ItemStack pStack, Stream<Holder<Enchantment>> pPossibleEnchantments) {
        List<EnchantmentInstance> list = Lists.newArrayList();
        boolean flag = pStack.is(Items.BOOK);
        // Neo: Rewrite filter logic to call isPrimaryItemFor instead of hardcoded vanilla logic.
        // The original logic is recorded in the default implementation of IItemExtension#isPrimaryItemFor.
        pPossibleEnchantments.filter(pStack::isPrimaryItemFor).forEach(p_344478_ -> {
            Enchantment enchantment = p_344478_.value();

            for (int i = enchantment.getMaxLevel(); i >= enchantment.getMinLevel(); i--) {
                if (pLevel >= enchantment.getMinCost(i) && pLevel <= enchantment.getMaxCost(i)) {
                    list.add(new EnchantmentInstance((Holder<Enchantment>)p_344478_, i));
                    break;
                }
            }
        });
        return list;
    }

    public static void enchantItemFromProvider(
        ItemStack pStack, RegistryAccess pRegistries, ResourceKey<EnchantmentProvider> pKey, DifficultyInstance pDifficulty, RandomSource pRandom
    ) {
        EnchantmentProvider enchantmentprovider = pRegistries.registryOrThrow(Registries.ENCHANTMENT_PROVIDER).get(pKey);
        if (enchantmentprovider != null) {
            updateEnchantments(pStack, p_348401_ -> enchantmentprovider.enchant(pStack, p_348401_, pRandom, pDifficulty));
        }
    }

    @FunctionalInterface
    interface EnchantmentInSlotVisitor {
        void accept(Holder<Enchantment> pEnchantment, int pLevel, EnchantedItemInUse pItem);
    }

    @FunctionalInterface
    interface EnchantmentVisitor {
        void accept(Holder<Enchantment> pEnchantment, int pLevel);
    }
}
