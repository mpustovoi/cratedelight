package com.axperty.cratedelight.block;

import com.axperty.cratedelight.CrateDelight;
import com.axperty.cratedelight.item.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, CrateDelight.MOD_ID);

    // Apple Crate
    public static final RegistryObject<Block> APPLE_CRATE = registerBlock("apple_crate",
            () -> new Block(Block.Properties.copy(Blocks.OAK_PLANKS).strength(2.0F, 3.0F).sound(SoundType.WOOD)));

    // Berry Crate
    public static final RegistryObject<Block> BERRY_CRATE = registerBlock("berry_crate",
            () -> new Block(Block.Properties.copy(Blocks.OAK_PLANKS).strength(2.0F, 3.0F).sound(SoundType.WOOD)));

    // Blueberry Crate (Nutritious Feast)
    public static final RegistryObject<Block> BLUEBERRY_CRATE = registerBlock("blueberry_crate",
            () -> new Block(Block.Properties.copy(Blocks.OAK_PLANKS).strength(2.0F, 3.0F).sound(SoundType.WOOD)));

    // Glow Berry Crate
    public static final RegistryObject<Block> GLOWBERRY_CRATE = registerBlock("glowberry_crate",
            () -> new Block(Block.Properties.copy(Blocks.OAK_PLANKS).strength(2.0F, 3.0F).sound(SoundType.WOOD).lightLevel((state) -> 13)));

    // Egg Crate
    public static final RegistryObject<Block> EGG_CRATE = registerBlock("egg_crate",
            () -> new Block(Block.Properties.copy(Blocks.OAK_PLANKS).strength(2.0F, 3.0F).sound(SoundType.WOOD)));

    // Duck Egg Crate (Naturalist)
    public static final RegistryObject<Block> DUCK_EGG_CRATE = registerBlock("duck_egg_crate",
            () -> new Block(Block.Properties.copy(Blocks.OAK_PLANKS).strength(2.0F, 3.0F).sound(SoundType.WOOD)));

    // Salmon Crate
    public static final RegistryObject<Block> SALMON_CRATE = registerBlock("salmon_crate",
            () -> new Block(Block.Properties.copy(Blocks.OAK_PLANKS).strength(2.0F, 3.0F).sound(SoundType.WOOD)));

    // Cod Crate
    public static final RegistryObject<Block> COD_CRATE = registerBlock("cod_crate",
            () -> new Block(Block.Properties.copy(Blocks.OAK_PLANKS).strength(2.0F, 3.0F).sound(SoundType.WOOD)));

    // Catfish Crate (Naturalist)
    public static final RegistryObject<Block> CATFISH_CRATE = registerBlock("catfish_crate",
            () -> new Block(Block.Properties.copy(Blocks.OAK_PLANKS).strength(2.0F, 3.0F).sound(SoundType.WOOD)));

    // Bass Crate (Naturalist)
    public static final RegistryObject<Block> BASS_CRATE = registerBlock("bass_crate",
            () -> new Block(Block.Properties.copy(Blocks.OAK_PLANKS).strength(2.0F, 3.0F).sound(SoundType.WOOD)));

    // Golden Apple Crate
    public static final RegistryObject<Block> GOLDEN_APPLE_CRATE = registerBlock("golden_apple_crate",
            () -> new Block(Block.Properties.copy(Blocks.OAK_PLANKS).strength(2.0F, 3.0F).sound(SoundType.WOOD)));

    // Cocoa Beans Bag
    public static final RegistryObject<Block> COCOABEANS_BAG = registerBlock("cocoabeans_bag",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_WOOL).strength(.8F, .8F).sound(SoundType.WOOL)));

    // Sugar Bag
    public static final RegistryObject<Block> SUGAR_BAG = registerBlock("sugar_bag",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_WOOL).strength(.8F, .8F).sound(SoundType.WOOL)));

    // Gunpowder Bag
    public static final RegistryObject<Block> GUNPOWDER_BAG = registerBlock("gunpowder_bag",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_WOOL).strength(.8F, .8F).sound(SoundType.WOOL)));

    // Wheat Flour Bag (Create)
    public static final RegistryObject<Block> WHEAT_FLOUR_BAG = registerBlock("wheat_flour_bag",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_WOOL).strength(.8F, .8F).sound(SoundType.WOOL)));

    // Powdered Obsidian Bag (Create)
    public static final RegistryObject<Block> POWDERED_OBSIDIAN_BAG = registerBlock("powdered_obsidian_bag",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_WOOL).strength(.8F, .8F).sound(SoundType.WOOL)));

    // Cinder Flour Bag (Create)
    public static final RegistryObject<Block> CINDER_FLOUR_BAG = registerBlock("cinder_flour_bag",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_WOOL).strength(.8F, .8F).sound(SoundType.WOOL)));

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
