package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;

public class FixedPlacement extends PlacementModifier {
    public static final MapCodec<FixedPlacement> CODEC = RecordCodecBuilder.mapCodec(
        p_352897_ -> p_352897_.group(BlockPos.CODEC.listOf().fieldOf("positions").forGetter(p_352962_ -> p_352962_.positions))
                .apply(p_352897_, FixedPlacement::new)
    );
    private final List<BlockPos> positions;

    public static FixedPlacement of(BlockPos... pPositions) {
        return new FixedPlacement(List.of(pPositions));
    }

    private FixedPlacement(List<BlockPos> p_352933_) {
        this.positions = p_352933_;
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext pContext, RandomSource pRandom, BlockPos pPos) {
        int i = SectionPos.blockToSectionCoord(pPos.getX());
        int j = SectionPos.blockToSectionCoord(pPos.getZ());
        boolean flag = false;

        for (BlockPos blockpos : this.positions) {
            if (isSameChunk(i, j, blockpos)) {
                flag = true;
                break;
            }
        }

        return !flag ? Stream.empty() : this.positions.stream().filter(p_352956_ -> isSameChunk(i, j, p_352956_));
    }

    private static boolean isSameChunk(int pX, int pZ, BlockPos pPos) {
        return pX == SectionPos.blockToSectionCoord(pPos.getX()) && pZ == SectionPos.blockToSectionCoord(pPos.getZ());
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifierType.FIXED_PLACEMENT;
    }
}
