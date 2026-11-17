package com.blahaj.voidhaj;

import com.blahaj.BlahajBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class VoidBlahajBlockEntity extends BlahajBlockEntity {
    /** Three quiet seconds before everything goes wrong. */
    public static final int FUSE_TICKS = 60;

    private int fuse = -1;

    public VoidBlahajBlockEntity(BlockPos pos, BlockState state) {
        super(VoidContent.BLOCK_ENTITY, pos, state);
    }

    /** First squish starts the countdown; later squishes do nothing more. */
    public void arm() {
        if (fuse < 0) fuse = FUSE_TICKS;
    }

    void serverTick() {
        if (fuse < 0 || !(level instanceof ServerLevel serverLevel)) return;
        if (--fuse > 0) return;

        BlockPos pos = getBlockPos();
        float yaw = getBlockState().getValue(VoidBlahajBlock.FACING).toYRot();
        // Single use: the block disappears and becomes the rising entity.
        serverLevel.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        RisingVoidBlahaj rising = new RisingVoidBlahaj(VoidContent.RISING_BLAHAJ, serverLevel);
        rising.start(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, yaw);
        serverLevel.addFreshEntity(rising);
    }
}
