package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.enchantment.LevelBasedValue;

public record AddValue(LevelBasedValue value) implements EnchantmentValueEffect {
    public static final MapCodec<AddValue> CODEC = RecordCodecBuilder.mapCodec(
        p_345952_ -> p_345952_.group(LevelBasedValue.CODEC.fieldOf("value").forGetter(AddValue::value)).apply(p_345952_, AddValue::new)
    );

    @Override
    public float process(int pEnchantmentLevel, RandomSource pRandom, float pValue) {
        return pValue + this.value.calculate(pEnchantmentLevel);
    }

    @Override
    public MapCodec<AddValue> codec() {
        return CODEC;
    }
}
