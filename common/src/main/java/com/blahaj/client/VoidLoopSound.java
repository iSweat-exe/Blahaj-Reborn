package com.blahaj.client;

import com.blahaj.voidhaj.BlackHoleEntity;
import com.blahaj.voidhaj.VoidContent;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

/** Looping hum that follows the black hole: louder and lower as it grows, shrill while collapsing. */
public class VoidLoopSound extends AbstractTickableSoundInstance {
    private final BlackHoleEntity hole;

    public VoidLoopSound(BlackHoleEntity hole) {
        super(VoidContent.LOOP, SoundSource.HOSTILE, RandomSource.create());
        this.hole = hole;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.01F;
        this.x = hole.getX();
        this.y = hole.getY();
        this.z = hole.getZ();
    }

    @Override
    public void tick() {
        if (hole.isRemoved()) {
            stop();
            return;
        }
        float r = hole.getRadius();
        this.x = hole.getX();
        this.y = hole.getY();
        this.z = hole.getZ();
        if (hole.isCollapsing()) {
            this.volume = 4.0F;
            this.pitch = Math.min(2.0F, this.pitch + 0.03F);
        } else {
            this.volume = 0.6F + r * 0.9F;  // above 1: audible from farther away
            this.pitch = 1.2F - r * 0.15F;
        }
    }
}
