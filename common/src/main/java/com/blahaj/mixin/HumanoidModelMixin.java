package com.blahaj.mixin;

import com.blahaj.BlahajItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Raises an arm 90 degrees forward while it holds a Blåhaj.
 *
 * <p>FIXME (known bug, fix planned for the next update): the plush does not sit correctly in the hand.
 * The arm pose below is fine, but the item placement comes from the {@code thirdperson_righthand} and
 * {@code thirdperson_lefthand} entries in {@code assets/blahaj/models/item/blahaj.json}, which were tuned
 * by eye. The first-person transforms ({@code firstperson_*}) have not been adjusted for this pose either.
 */
@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin {
    @Shadow @org.spongepowered.asm.mixin.Final public ModelPart rightArm;
    @Shadow @org.spongepowered.asm.mixin.Final public ModelPart leftArm;

    // FIXME: see class comment, the held item position still needs tuning.
    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V", at = @At("TAIL"))
    private void blahaj$raiseArms(HumanoidRenderState state, CallbackInfo ci) {
        float ninety = (float) (-Math.PI / 2);
        if (state.rightHandItemStack.getItem() instanceof BlahajItem) {
            this.rightArm.xRot = ninety;
            this.rightArm.yRot = 0.0F;
            this.rightArm.zRot = 0.0F;
        }
        if (state.leftHandItemStack.getItem() instanceof BlahajItem) {
            this.leftArm.xRot = ninety;
            this.leftArm.yRot = 0.0F;
            this.leftArm.zRot = 0.0F;
        }
    }
}
