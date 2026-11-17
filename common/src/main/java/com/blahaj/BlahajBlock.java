package com.blahaj;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class BlahajBlock extends Block implements EntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    // Hitbox parts in pixels (0-16) for FACING=north; rotated for the other directions.
    private static final VoxelShape SHAPE_NORTH = Shapes.or(
            Block.box(6, 0, 0, 10, 4, 10.14),  // body + nose
            Block.box(7, 1, 9.94, 9, 5.66, 16),  // tail
            Block.box(6.74, 2.61, 3.4, 9.24, 7.53, 7.25),  // dorsal fin
            Block.box(10, 0, 3.26, 11, 2.38, 8.12),  // left fin
            Block.box(5, 0, 3.26, 6, 2.38, 8.12));  // right fin
    private static final VoxelShape[] SHAPES = new VoxelShape[4];

    static {
        for (Direction d : Direction.Plane.HORIZONTAL) {
            SHAPES[d.get2DDataValue()] = rotate(SHAPE_NORTH, d);
        }
    }

    public BlahajBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    private static VoxelShape rotate(VoxelShape shape, Direction to) {
        int turns = (to.get2DDataValue() - Direction.NORTH.get2DDataValue() + 4) % 4;
        VoxelShape[] out = {shape};
        for (int i = 0; i < turns; i++) {
            VoxelShape[] acc = {Shapes.empty()};
            out[0].forAllBoxes((x1, y1, z1, x2, y2, z2) ->
                    acc[0] = Shapes.or(acc[0], Shapes.box(1 - z2, y1, x1, 1 - z1, y2, x2)));
            out[0] = acc[0];
        }
        return out[0];
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(FACING).get2DDataValue()];
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlahajBlockEntity(pos, state);
    }

    /** Right-click with an empty hand plays the squish animation and sound. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof BlahajBlockEntity blahaj) {
            blahaj.squish();
            // Random pitch (0.8 - 1.2) on every squish.
            float pitch = 0.8F + level.getRandom().nextFloat() * 0.4F;
            level.playSound(null, pos, BlahajContent.SQUISH_SOUND, SoundSource.BLOCKS, 0.7F, pitch);
            onSquished(state, level, pos, player, blahaj);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    /** Called after every squish, on both logical sides. */
    protected void onSquished(BlockState state, Level level, BlockPos pos, Player player, BlahajBlockEntity blahaj) {}
}
