package com.blahaj.client;

import com.blahaj.BlahajMod;
import com.blahaj.voidhaj.RisingVoidBlahaj;
import com.geckolib.model.DefaultedItemGeoModel;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/** The rising Blåhaj: same model as the item, spinning faster and faster, then shaking. */
public class RisingBlahajRenderer<R extends EntityRenderState & GeoRenderState> extends GeoEntityRenderer<RisingVoidBlahaj, R> {
    public RisingBlahajRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedItemGeoModel<>(BlahajMod.id("blahaj")));
        this.shadowRadius = 0.3F;
    }

    @Override
    public void adjustRenderPose(RenderPassInfo<R> info) {
        super.adjustRenderPose(info);
        double age = info.renderState().getAnimatableAge();
        double t = Math.min(age / RisingVoidBlahaj.RISE_TICKS, 1.0);
        // Spin accelerates while rising.
        info.poseStack().rotateDegrees(Axis.YP, (float) (age * age * 0.9));
        if (age > RisingVoidBlahaj.RISE_TICKS) {
            // Hovering: shaking gets more violent over time.
            double k = (age - RisingVoidBlahaj.RISE_TICKS) / RisingVoidBlahaj.HOVER_TICKS;
            info.poseStack().translate(Math.sin(age * 9.1) * 0.08 * k, Math.sin(age * 7.3) * 0.08 * k, Math.cos(age * 8.7) * 0.08 * k);
            info.poseStack().rotateDegrees(Axis.ZP, (float) (Math.sin(age * 5.0) * 25 * k));
        } else {
            info.poseStack().rotateDegrees(Axis.XP, (float) (Math.sin(age * 0.6) * 15 * t));
        }
    }
}
