package com.blahaj;

import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animatable.GeoBlockEntity;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Block entity driving the GeckoLib "squish" animation. */
public class BlahajBlockEntity extends BlockEntity implements GeoBlockEntity {
    public static final String SQUISH = "squish";
    private static final String CONTROLLER = "squish_controller";

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public BlahajBlockEntity(BlockPos pos, BlockState state) {
        this(BlahajContent.BLOCK_ENTITY, pos, state);
    }

    protected BlahajBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Triggerable animation: every triggerAnim call restarts it from the beginning.
        controllers.add(new AnimationController<>(CONTROLLER, test -> PlayState.STOP)
                .triggerableAnim(SQUISH, RawAnimation.begin().thenPlay(SQUISH)));
    }

    public void squish() {
        triggerAnim(CONTROLLER, SQUISH);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
