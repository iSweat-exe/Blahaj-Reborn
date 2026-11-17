package com.blahaj.voidhaj;

import com.blahaj.BlahajItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/** Blåhaj copy with a purple, obfuscated name. */
public class VoidBlahajItem extends BlahajItem {
    public VoidBlahajItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        MutableComponent glyphs = Component.literal("▓▒").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.OBFUSCATED);
        MutableComponent name = Component.literal("Bl").withStyle(ChatFormatting.DARK_PURPLE)
                .append(Component.literal("å").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.OBFUSCATED))
                .append(Component.literal("haj").withStyle(ChatFormatting.DARK_PURPLE));
        return Component.empty().append(glyphs).append(Component.literal(" ")).append(name)
                .append(Component.literal(" ")).append(glyphs.copy());
    }
}
