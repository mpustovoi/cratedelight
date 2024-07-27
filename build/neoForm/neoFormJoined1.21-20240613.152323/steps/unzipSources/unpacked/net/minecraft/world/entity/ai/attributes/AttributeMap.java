package net.minecraft.world.entity.ai.attributes;

import com.google.common.collect.Multimap;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public class AttributeMap {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Map<Holder<Attribute>, AttributeInstance> attributes = new Object2ObjectOpenHashMap<>();
    private final Set<AttributeInstance> attributesToSync = new ObjectOpenHashSet<>();
    private final Set<AttributeInstance> attributesToUpdate = new ObjectOpenHashSet<>();
    private final AttributeSupplier supplier;

    public AttributeMap(AttributeSupplier pSupplier) {
        this.supplier = pSupplier;
    }

    private void onAttributeModified(AttributeInstance p_22158_) {
        this.attributesToUpdate.add(p_22158_);
        if (p_22158_.getAttribute().value().isClientSyncable()) {
            this.attributesToSync.add(p_22158_);
        }
    }

    public Set<AttributeInstance> getAttributesToSync() {
        return this.attributesToSync;
    }

    public Set<AttributeInstance> getAttributesToUpdate() {
        return this.attributesToUpdate;
    }

    public Collection<AttributeInstance> getSyncableAttributes() {
        return this.attributes.values().stream().filter(p_315935_ -> p_315935_.getAttribute().value().isClientSyncable()).collect(Collectors.toList());
    }

    @Nullable
    public AttributeInstance getInstance(Holder<Attribute> pAttribute) {
        return this.attributes.computeIfAbsent(pAttribute, p_315936_ -> this.supplier.createInstance(this::onAttributeModified, (Holder<Attribute>)p_315936_));
    }

    public boolean hasAttribute(Holder<Attribute> pAttribute) {
        return this.attributes.get(pAttribute) != null || this.supplier.hasAttribute(pAttribute);
    }

    public boolean hasModifier(Holder<Attribute> pAttribute, ResourceLocation pId) {
        AttributeInstance attributeinstance = this.attributes.get(pAttribute);
        return attributeinstance != null ? attributeinstance.getModifier(pId) != null : this.supplier.hasModifier(pAttribute, pId);
    }

    public double getValue(Holder<Attribute> pAttribute) {
        AttributeInstance attributeinstance = this.attributes.get(pAttribute);
        return attributeinstance != null ? attributeinstance.getValue() : this.supplier.getValue(pAttribute);
    }

    public double getBaseValue(Holder<Attribute> pAttribute) {
        AttributeInstance attributeinstance = this.attributes.get(pAttribute);
        return attributeinstance != null ? attributeinstance.getBaseValue() : this.supplier.getBaseValue(pAttribute);
    }

    public double getModifierValue(Holder<Attribute> pAttribute, ResourceLocation pId) {
        AttributeInstance attributeinstance = this.attributes.get(pAttribute);
        return attributeinstance != null ? attributeinstance.getModifier(pId).amount() : this.supplier.getModifierValue(pAttribute, pId);
    }

    public void addTransientAttributeModifiers(Multimap<Holder<Attribute>, AttributeModifier> pModifiers) {
        pModifiers.forEach((p_351795_, p_351796_) -> {
            AttributeInstance attributeinstance = this.getInstance((Holder<Attribute>)p_351795_);
            if (attributeinstance != null) {
                attributeinstance.removeModifier(p_351796_.id());
                attributeinstance.addTransientModifier(p_351796_);
            }
        });
    }

    public void removeAttributeModifiers(Multimap<Holder<Attribute>, AttributeModifier> pModifiers) {
        pModifiers.asMap().forEach((p_344297_, p_344298_) -> {
            AttributeInstance attributeinstance = this.attributes.get(p_344297_);
            if (attributeinstance != null) {
                p_344298_.forEach(p_351794_ -> attributeinstance.removeModifier(p_351794_.id()));
            }
        });
    }

    public void assignAllValues(AttributeMap pMap) {
        pMap.attributes.values().forEach(p_315934_ -> {
            AttributeInstance attributeinstance = this.getInstance(p_315934_.getAttribute());
            if (attributeinstance != null) {
                attributeinstance.replaceFrom(p_315934_);
            }
        });
    }

    public void assignBaseValues(AttributeMap pMap) {
        pMap.attributes.values().forEach(p_348165_ -> {
            AttributeInstance attributeinstance = this.getInstance(p_348165_.getAttribute());
            if (attributeinstance != null) {
                attributeinstance.setBaseValue(p_348165_.getBaseValue());
            }
        });
    }

    public ListTag save() {
        ListTag listtag = new ListTag();

        for (AttributeInstance attributeinstance : this.attributes.values()) {
            listtag.add(attributeinstance.save());
        }

        return listtag;
    }

    public void load(ListTag pNbt) {
        for (int i = 0; i < pNbt.size(); i++) {
            CompoundTag compoundtag = pNbt.getCompound(i);
            String s = compoundtag.getString("id");
            ResourceLocation resourcelocation = ResourceLocation.tryParse(s);
            if (resourcelocation != null) {
                Util.ifElse(BuiltInRegistries.ATTRIBUTE.getHolder(resourcelocation), p_315940_ -> {
                    AttributeInstance attributeinstance = this.getInstance(p_315940_);
                    if (attributeinstance != null) {
                        attributeinstance.load(compoundtag);
                    }
                }, () -> LOGGER.warn("Ignoring unknown attribute '{}'", resourcelocation));
            } else {
                LOGGER.warn("Ignoring malformed attribute '{}'", s);
            }
        }
    }
}
