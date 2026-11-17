package com.blahaj;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * All mod content, independent of the mod loader.
 * Each loader module only has to register these objects.
 */
public final class BlahajContent {
    public static final Identifier ID = BlahajMod.id("blahaj");
        public static final Identifier TAB_ID = BlahajMod.id("main");

    public static final SoundEvent SQUISH_SOUND = SoundEvent.createVariableRangeEvent(BlahajMod.id("squish"));

    public static final BlahajBlock BLOCK = new BlahajBlock(BlockBehaviour.Properties.of()
            .setId(ResourceKey.create(Registries.BLOCK, ID))
            .strength(0.8F)
            .sound(SoundType.WOOL)
            .noOcclusion());

    public static final BlockEntityType<BlahajBlockEntity> BLOCK_ENTITY =
            new BlockEntityType<>(BlahajBlockEntity::new, java.util.Set.of(BLOCK));

    public static final BlahajItem ITEM = new BlahajItem(BLOCK, new Item.Properties()
            .setId(ResourceKey.create(Registries.ITEM, ID))
            .useBlockDescriptionPrefix()
            .stacksTo(1)
            .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD).build()));

    public static final CreativeModeTab TAB = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
            .title(Component.translatable("itemGroup.blahaj.main"))
            .icon(() -> new ItemStack(ITEM))
            .displayItems((params, output) -> {
                output.accept(ITEM);
                output.accept(com.blahaj.voidhaj.VoidContent.ITEM);
            })
            .build();

    private BlahajContent() {}
}
