// ***************************************************************************
//
//  Copyright 2017 David (Dizzy) Smith, dizzyd@dizzyd.com
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
// ***************************************************************************

package com.dizzyd.creativezone;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.File;
import java.time.Instant;
import java.util.HashSet;

@Mod(modid = CreativeZoneMod.MODID, version = CreativeZoneMod.VERSION)
public class CreativeZoneMod
{
    public static final String MODID = "creativezone";
    public static final String VERSION = "1.0.1";

    static int checkInterval;
    static int zoneRadius;
    static HashSet<String> whitelist = new HashSet<>();

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {

        Configuration config = new Configuration(new File(e.getModConfigurationDirectory(), "creativezone.cfg"));
        config.load();

        // Check interval (seconds)
        checkInterval = config.getInt("ScanInterval", "config", 1, 1, 60,
                "Sets the interval (in seconds) for scanning player locations");

        // Creative zone radius
        zoneRadius = config.getInt("ZoneRadius", "config", 25, 5, 1000,
                "Sets the radius of the creative zone");

        Property whiteListProp = config.get("config", "Whitelist", new String[0],
                "Gets the list of whitelisted users");
        for (String s : whiteListProp.getStringList()) {
            whitelist.add(s);
        }

        config.save();
    }
    
    @Mod.EventHandler
    public void load(FMLInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(new EventHandler());
    }

    public static class EventHandler {
        long lastCheck = 0;

        @SubscribeEvent
        public void onWorldTick(TickEvent.WorldTickEvent e) {
            if (!e.world.isRemote && e.phase == TickEvent.Phase.START && e.world.provider.getDimension() == 0) {
                long now = Instant.now().getEpochSecond();
                if (now - lastCheck > checkInterval) {
                    BlockPos spawn = e.world.getSpawnPoint();
                    for (int i = 0; i < e.world.playerEntities.size(); i++)
                    {
                        EntityPlayer p = e.world.playerEntities.get(i);
                        // If the user is inside the zone radius, force them back to creative
                        if (p.getDistance(spawn.getX(), p.posY, spawn.getZ()) < zoneRadius) {
                            p.setGameType(GameType.CREATIVE);
                        } else {
                            // Otherwise, the user is outside the radius and we need to force
                            // them back to survival (assuming they're not on the whitelist)
                            if (!whitelist.contains(p.getName())) {
                                p.setGameType(GameType.SURVIVAL);
                            }
                        }
                    }
                    lastCheck = now;
                }
            }
        }
    }

}
