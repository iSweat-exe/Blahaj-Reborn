package com.blahaj;

import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.model.DefaultedItemGeoModel;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * The Blåhaj item: held, worn on the head, or placed on the ground as a block.
 * Rendered with assets/blahaj/geckolib/models/item/blahaj.geo.json.
 */
public class BlahajItem extends BlockItem implements GeoItem {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public BlahajItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private final Supplier<GeoItemRenderer<BlahajItem>> renderer = com.google.common.base.Suppliers.memoize(
                    () -> new GeoItemRenderer<>(new DefaultedItemGeoModel<>(BlahajMod.id("blahaj"))));

            @Override
            public @Nullable GeoItemRenderer<BlahajItem> getGeoItemRenderer() {
                return this.renderer.get();
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
