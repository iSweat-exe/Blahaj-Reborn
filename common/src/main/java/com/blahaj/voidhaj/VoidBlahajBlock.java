package com.blahaj.voidhaj;

import com.blahaj.BlahajBlock;
import com.blahaj.BlahajBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/** The Void Blåhaj: looks identical, but squishing it triggers the black hole. */
public class VoidBlahajBlock extends BlahajBlock {
    public VoidBlahajBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new VoidBlahajBlockEntity(pos, state);
    }

    @Override
    protected void onSquished(BlockState state, Level level, BlockPos pos, Player player, BlahajBlockEntity blahaj) {
        if (!level.isClientSide() && blahaj instanceof VoidBlahajBlockEntity voidBlahaj) {
            voidBlahaj.arm();
        }
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != VoidContent.BLOCK_ENTITY) return null;
        return (lvl, pos, st, be) -> ((VoidBlahajBlockEntity) be).serverTick();
    }
}
