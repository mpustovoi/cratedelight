package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class EndPlatformFeature extends Feature<NoneFeatureConfiguration> {
    public EndPlatformFeature(Codec<NoneFeatureConfiguration> pCodec) {
        super(pCodec);
    }

    /**
     * Places the given feature at the given location.
     * During world generation, features are provided with a 3x3 region of chunks, centered on the chunk being generated, that they can safely generate into.
     *
     * @param pContext A context object with a reference to the level and the position
     *                 the feature is being placed at
     */
    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> pContext) {
        createEndPlatform(pContext.level(), pContext.origin(), false);
        return true;
    }

    public static void createEndPlatform(ServerLevelAccessor pLevel, BlockPos pPos, boolean pDropBlocks) {
        BlockPos.MutableBlockPos blockpos$mutableblockpos = pPos.mutable();

        for (int i = -2; i <= 2; i++) {
            for (int j = -2; j <= 2; j++) {
                for (int k = -1; k < 3; k++) {
                    BlockPos blockpos = blockpos$mutableblockpos.set(pPos).move(j, k, i);
                    Block block = k == -1 ? Blocks.OBSIDIAN : Blocks.AIR;
                    if (!pLevel.getBlockState(blockpos).is(block)) {
                        if (pDropBlocks) {
                            pLevel.destroyBlock(blockpos, true, null);
                        }

                        pLevel.setBlock(blockpos, block.defaultBlockState(), 3);
                    }
                }
            }
        }
    }
}
