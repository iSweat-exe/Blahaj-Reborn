package com.blahaj.client;

import com.blahaj.BlahajMod;
import com.blahaj.voidhaj.BlackHoleEntity;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.DefaultedEntityGeoModel;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.builtin.AutoGlowingGeoLayer;
import com.google.common.reflect.TypeToken;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

/** Black hole renderer: GeckoLib model (black core + glowing discs) scaled to the synced radius. */
public class BlackHoleRenderer<R extends EntityRenderState & GeoRenderState> extends GeoEntityRenderer<BlackHoleEntity, R> {
    private static final DataTicket<Float> RADIUS = DataTicket.create("blahaj_black_hole_radius", new TypeToken<>() {});
    private static final DataTicket<Boolean> COLLAPSING = DataTicket.create("blahaj_black_hole_collapsing", new TypeToken<>() {});

    public BlackHoleRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedEntityGeoModel<>(BlahajMod.id("black_hole")));
        // Glow layer (black_hole_glowmask.png) using a vanilla render type: GeckoLib's custom
        // emissive pipeline is not recognised by Iris, which stretches the model when a shader pack is active.
        withRenderLayer(new AutoGlowingGeoLayer<>(this) {
            @Override
            protected @Nullable RenderType getRenderType(R renderState) {
                return ShaderPackCompat.isShaderPackActive()
                        ? RenderTypes.entityTranslucentEmissive(getTextureResource(renderState))
                        : super.getRenderType(renderState);
            }
        });
        this.shadowRadius = 0;
    }

    @Override
    public void addRenderData(BlackHoleEntity entity, @Nullable Void relatedObject, R renderState, float partialTick) {
        super.addRenderData(entity, relatedObject, renderState, partialTick);
        renderState.addGeckolibData(RADIUS, entity.getRadius(partialTick));
        renderState.addGeckolibData(COLLAPSING, entity.isCollapsing());
    }

    /** Translucent so the disc fades into streaks and the dark halo lets the sky show through. */
    @Override
    public @Nullable RenderType getRenderType(R renderState, Identifier texture) {
        return RenderTypes.entityTranslucent(texture);
    }

    @Override
    public void scaleModelForRender(RenderPassInfo<R> info, float widthScale, float heightScale) {
        // The model has a radius of 0.5 blocks; scale it to the synced radius.
        float scale = info.renderState().getOrDefaultGeckolibData(RADIUS, 1F) * 2F;
        super.scaleModelForRender(info, widthScale * scale, heightScale * scale);
    }

    @Override
    public void adjustRenderPose(RenderPassInfo<R> info) {
        super.adjustRenderPose(info);
        if (Boolean.TRUE.equals(info.renderState().getGeckolibData(COLLAPSING))) {
            // Shake violently while collapsing.
            double age = info.renderState().getAnimatableAge();
            info.poseStack().rotateDegrees(Axis.ZP, (float) Math.sin(age * 2.7) * 8F);
            info.poseStack().rotateDegrees(Axis.XP, (float) Math.cos(age * 3.1) * 8F);
        }
    }

    @Override
    protected AABB getBoundingBoxForCulling(BlackHoleEntity entity, float partialTicks) {
        double r = entity.getRadius(partialTicks) * 5 + 2;
        return entity.getBoundingBox().inflate(r);
    }
}
