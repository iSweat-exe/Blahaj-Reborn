package com.blahaj.gametest;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/** Plays the whole Void Blåhaj sequence and takes screenshots of each stage. */
public class VoidSequenceGameTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext sp = context.worldBuilder().create()) {
            var s = sp.getServer();
            s.runCommand("gamemode creative @p");
            s.runCommand("time set day");
            s.runCommand("setblock 0 -60 0 blahaj:void_blahaj[facing=south]");
            String[] mobs = {"cow", "sheep", "pig", "chicken", "cow", "sheep", "pig", "cow", "pig"};
            for (int i = 0; i < mobs.length; i++) {
                double a = i / (double) mobs.length * Math.PI * 2;
                s.runCommand(String.format(java.util.Locale.ROOT, "summon %s %.1f -60 %.1f", mobs[i], Math.cos(a) * 9, Math.sin(a) * 9 - 4));
            }
            s.runCommand("tp @p 0.5 -60 4.5 180 20");
            context.waitTicks(40);
            context.getInput().pressKey(options -> options.keyUse);
            context.waitTicks(5);
            context.takeScreenshot("void-1-squish");
            s.runCommand("tp @p 0.5 -60 16.5 180 -28");
            context.waitTicks(75);
            context.takeScreenshot("void-2-rise");
            context.waitTicks(75);
            context.takeScreenshot("void-3-hole");
            context.waitTicks(160);
            context.takeScreenshot("void-4-hole-big");
            context.waitTicks(45);
            context.takeScreenshot("void-5-collapse");
            context.waitTicks(28);
            context.takeScreenshot("void-6-flash");
            context.waitTicks(40);
            context.takeScreenshot("void-7-debris");
            context.waitTicks(80);
            s.runCommand("tp @p 0.5 -38 0.5 0 90");
            context.waitTicks(20);
            context.takeScreenshot("void-8-crater");
        }
    }
}
