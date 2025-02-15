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
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, CrateDelight.MOD_ID);

    // Carrot Crate
    public static final RegistryObject<Block> CARROT_CRATE = registerBlock("carrot_crate",
            () -> new Block(Block.Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(2.0F, 3.0F).sound(SoundType.WOOD)));

    // Potato Crate
    public static final RegistryObject<Block> POTATO_CRATE = registerBlock("potato_crate",
            () -> new Block(Block.Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(2.0F, 3.0F).sound(SoundType.WOOD)));

    // Beetroot Crate
    public static final RegistryObject<Block> BEETROOT_CRATE = registerBlock("beetroot_crate",
            () -> new Block(Block.Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(2.0F, 3.0F).sound(SoundType.WOOD)));

    // Apple Crate
    public static final RegistryObject<Block> APPLE_CRATE = registerBlock("apple_crate",
            () -> new Block(Block.Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(2.0F, 3.0F).sound(SoundType.WOOD)));

    // Berry Crate
    public static final RegistryObject<Block> BERRY_CRATE = registerBlock("berry_crate",
            () -> new Block(Block.Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(2.0F, 3.0F).sound(SoundType.WOOD)));

    // Glow Berry Crate
    public static final RegistryObject<Block> GLOWBERRY_CRATE = registerBlock("glowberry_crate",
            () -> new Block(Block.Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(2.0F, 3.0F).sound(SoundType.WOOD).lightLevel((state) -> 13)));

    // Egg Crate
    public static final RegistryObject<Block> EGG_CRATE = registerBlock("egg_crate",
            () -> new Block(Block.Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(2.0F, 3.0F).sound(SoundType.WOOD)));

    // Salmon Crate
    public static final RegistryObject<Block> SALMON_CRATE = registerBlock("salmon_crate",
            () -> new Block(Block.Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(2.0F, 3.0F).sound(SoundType.WOOD)));

    // Cod Crate
    public static final RegistryObject<Block> COD_CRATE = registerBlock("cod_crate",
            () -> new Block(Block.Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(2.0F, 3.0F).sound(SoundType.WOOD)));

    // Red Mushroom Crate
    public static final RegistryObject<Block> RED_MUSHROOM_CRATE = registerBlock("red_mushroom_crate",
            () -> new Block(Block.Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(2.0F, 3.0F).sound(SoundType.WOOD)));

    // Brown Mushroom Crate
    public static final RegistryObject<Block> BROWN_MUSHROOM_CRATE = registerBlock("brown_mushroom_crate",
            () -> new Block(Block.Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(2.0F, 3.0F).sound(SoundType.WOOD)));

    // Golden Carrot Crate
    public static final RegistryObject<Block> GOLDEN_CARROT_CRATE = registerBlock("golden_carrot_crate",
            () -> new Block(Block.Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(2.0F, 3.0F).sound(SoundType.WOOD)));

    // Golden Apple Crate
    public static final RegistryObject<Block> GOLDEN_APPLE_CRATE = registerBlock("golden_apple_crate",
            () -> new Block(Block.Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(2.0F, 3.0F).sound(SoundType.WOOD)));

    // Cocoa Beans Bag
    public static final RegistryObject<Block> COCOABEANS_BAG = registerBlock("cocoabeans_bag",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_WOOL).strength(.8F, .8F).sound(SoundType.WOOL)));

    // Sugar Bag
    public static final RegistryObject<Block> SUGAR_BAG = registerBlock("sugar_bag",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_WOOL).strength(.8F, .8F).sound(SoundType.WOOL)));

    // Gunpowder Bag
    public static final RegistryObject<Block> GUNPOWDER_BAG = registerBlock("gunpowder_bag",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_WOOL).strength(.8F, .8F).sound(SoundType.WOOL)));

    // Cookie Bag
    public static final RegistryObject<Block> COOKIE_BAG = registerBlock("cookie_bag",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_WOOL).strength(.8F, .8F).sound(SoundType.WOOL)));

    // Stacked Melons
    public static final RegistryObject<Block> STACKED_MELONS = registerBlock("stacked_melons",
            () -> new Block(Block.Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(2.0F, 3.0F).sound(SoundType.WOOD)));

    // Stacked Pumpkins
    public static final RegistryObject<Block> STACKED_PUMPKINS = registerBlock("stacked_pumpkins",
            () -> new Block(Block.Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(2.0F, 3.0F).sound(SoundType.WOOD)));

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
