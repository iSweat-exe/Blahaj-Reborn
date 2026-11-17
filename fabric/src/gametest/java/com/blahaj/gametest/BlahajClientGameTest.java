package com.blahaj.gametest;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/** Checks the arm pose while holding a Blåhaj: a motionless mob seen from the side, then from the front. */
public class BlahajClientGameTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext sp = context.worldBuilder().create()) {
            var s = sp.getServer();
            s.runCommand("gamemode creative @p");
            s.runCommand("time set day");
            // Side view (facing east) and front view
            s.runCommand("summon husk 0.5 -60 0.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b,Rotation:[-90f,0f],equipment:{mainhand:{id:\"blahaj:blahaj\",count:1}}}");
            s.runCommand("summon husk 4.5 -60 0.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b,Rotation:[0f,0f],equipment:{mainhand:{id:\"blahaj:blahaj\",count:1}}}");
            s.runCommand("effect give @p invisibility 1000 0 true");
            s.runCommand("tp @p 0.5 -60 6.5 180 8");
            context.waitTicks(40);
            context.takeScreenshot("side");
            s.runCommand("tp @p 4.5 -60 6.5 180 8");
            context.waitTicks(10);
            context.takeScreenshot("front");
        }
    }
}
