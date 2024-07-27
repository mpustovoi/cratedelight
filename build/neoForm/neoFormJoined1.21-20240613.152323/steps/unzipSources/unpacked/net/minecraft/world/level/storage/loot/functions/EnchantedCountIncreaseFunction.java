package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.List;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public class EnchantedCountIncreaseFunction extends LootItemConditionalFunction {
    public static final int NO_LIMIT = 0;
    public static final MapCodec<EnchantedCountIncreaseFunction> CODEC = RecordCodecBuilder.mapCodec(
        p_346073_ -> commonFields(p_346073_)
                .and(
                    p_346073_.group(
                        Enchantment.CODEC.fieldOf("enchantment").forGetter(p_346362_ -> p_346362_.enchantment),
                        NumberProviders.CODEC.fieldOf("count").forGetter(p_345798_ -> p_345798_.value),
                        Codec.INT.optionalFieldOf("limit", Integer.valueOf(0)).forGetter(p_344886_ -> p_344886_.limit)
                    )
                )
                .apply(p_346073_, EnchantedCountIncreaseFunction::new)
    );
    private final Holder<Enchantment> enchantment;
    private final NumberProvider value;
    private final int limit;

    EnchantedCountIncreaseFunction(List<LootItemCondition> p_344944_, Holder<Enchantment> p_345194_, NumberProvider p_344806_, int p_345432_) {
        super(p_344944_);
        this.enchantment = p_345194_;
        this.value = p_344806_;
        this.limit = p_345432_;
    }

    @Override
    public LootItemFunctionType<EnchantedCountIncreaseFunction> getType() {
        return LootItemFunctions.ENCHANTED_COUNT_INCREASE;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return Sets.union(ImmutableSet.of(LootContextParams.ATTACKING_ENTITY), this.value.getReferencedContextParams());
    }

    private boolean hasLimit() {
        return this.limit > 0;
    }

    /**
     * Called to perform the actual action of this function, after conditions have been checked.
     */
    @Override
    public ItemStack run(ItemStack pStack, LootContext pContext) {
        Entity entity = pContext.getParamOrNull(LootContextParams.ATTACKING_ENTITY);
        if (entity instanceof LivingEntity livingentity) {
            int i = EnchantmentHelper.getEnchantmentLevel(this.enchantment, livingentity);
            if (i == 0) {
                return pStack;
            }

            float f = (float)i * this.value.getFloat(pContext);
            pStack.grow(Math.round(f));
            if (this.hasLimit()) {
                pStack.limitSize(this.limit);
            }
        }

        return pStack;
    }

    public static EnchantedCountIncreaseFunction.Builder lootingMultiplier(HolderLookup.Provider pRegistries, NumberProvider pCount) {
        HolderLookup.RegistryLookup<Enchantment> registrylookup = pRegistries.lookupOrThrow(Registries.ENCHANTMENT);
        return new EnchantedCountIncreaseFunction.Builder(registrylookup.getOrThrow(Enchantments.LOOTING), pCount);
    }

    public static class Builder extends LootItemConditionalFunction.Builder<EnchantedCountIncreaseFunction.Builder> {
        private final Holder<Enchantment> enchantment;
        private final NumberProvider count;
        private int limit = 0;

        public Builder(Holder<Enchantment> pEnchantment, NumberProvider pCount) {
            this.enchantment = pEnchantment;
            this.count = pCount;
        }

        protected EnchantedCountIncreaseFunction.Builder getThis() {
            return this;
        }

        public EnchantedCountIncreaseFunction.Builder setLimit(int pLimit) {
            this.limit = pLimit;
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new EnchantedCountIncreaseFunction(this.getConditions(), this.enchantment, this.count, this.limit);
        }
    }
}
