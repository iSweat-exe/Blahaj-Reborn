package com.blahaj.voidhaj;

import com.blahaj.BlahajMod;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.List;
import java.util.Set;

/** All Void Blåhaj content (loader independent). */
public final class VoidContent {
    public static final Identifier ID = BlahajMod.id("void_blahaj");
    public static final Identifier RISING_ID = BlahajMod.id("void_blahaj_rising");
    public static final Identifier BLACK_HOLE_ID = BlahajMod.id("black_hole");

    public static final VoidBlahajBlock BLOCK = new VoidBlahajBlock(BlockBehaviour.Properties.of()
            .setId(ResourceKey.create(Registries.BLOCK, ID))
            .strength(0.8F)
            .sound(SoundType.WOOL)
            .noOcclusion());

    public static final BlockEntityType<VoidBlahajBlockEntity> BLOCK_ENTITY =
            new BlockEntityType<>(VoidBlahajBlockEntity::new, Set.of(BLOCK));

    public static final VoidBlahajItem ITEM = new VoidBlahajItem(BLOCK, new Item.Properties()
            .setId(ResourceKey.create(Registries.ITEM, ID))
            .useBlockDescriptionPrefix()
            .stacksTo(1)
            .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD).build())
            .component(DataComponents.LORE, new ItemLore(List.of(
                    Component.translatable("item.blahaj.void_blahaj.lore").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)))));

    public static final EntityType<RisingVoidBlahaj> RISING_BLAHAJ = EntityType.Builder
            .<RisingVoidBlahaj>of(RisingVoidBlahaj::new, MobCategory.MISC)
            .sized(0.75F, 0.5F).clientTrackingRange(10).updateInterval(1).noSave().fireImmune()
            .build(ResourceKey.create(Registries.ENTITY_TYPE, RISING_ID));

    public static final EntityType<BlackHoleEntity> BLACK_HOLE = EntityType.Builder
            .<BlackHoleEntity>of(BlackHoleEntity::new, MobCategory.MISC)
            .sized(1.0F, 1.0F).clientTrackingRange(16).updateInterval(1).noSave().fireImmune()
            .build(ResourceKey.create(Registries.ENTITY_TYPE, BLACK_HOLE_ID));

    // Sounds (files in assets/blahaj/sounds/void, declared in sounds.json)
    public static final SoundEvent RISE = sound("void.rise");
    public static final SoundEvent OPEN = sound("void.open");
    public static final SoundEvent LOOP = sound("void.loop");
    public static final SoundEvent GLITCH = sound("void.glitch");
    public static final SoundEvent COLLAPSE = sound("void.collapse");
    public static final SoundEvent EXPLODE = sound("void.explode");
    public static final SoundEvent RING = sound("void.ring");
    public static final List<SoundEvent> SOUNDS = List.of(RISE, OPEN, LOOP, GLITCH, COLLAPSE, EXPLODE, RING);

    private static SoundEvent sound(String name) {
        return SoundEvent.createVariableRangeEvent(BlahajMod.id(name));
    }

    private VoidContent() {}
}
