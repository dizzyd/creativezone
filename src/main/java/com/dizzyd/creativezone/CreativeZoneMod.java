package com.dizzyd.creativezone;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.File;
import java.time.Instant;

@Mod(modid = CreativeZoneMod.MODID, version = CreativeZoneMod.VERSION)
public class CreativeZoneMod
{
    public static final String MODID = "creativezone";
    public static final String VERSION = "1.0";

    static int checkInterval;
    static int zoneRadius;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {

        Configuration config = new Configuration(new File(e.getModConfigurationDirectory(), "creativezone.cfg"));
        config.load();

        // Check interval (seconds)
        checkInterval = config.getInt("ScanInterval", "", 1, 1, 60,
                "Sets the interval (in seconds) for scanning player locations");

        // Creative zone radius
        zoneRadius = config.getInt("ZoneRadius", "", 25, 5, 1000,
                "Sets the radius of the creative zone");
        
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
                        if (p.getDistance(spawn.getX(), p.posY, spawn.getZ()) < zoneRadius) {
                            p.setGameType(GameType.CREATIVE);
                        } else {
                            p.setGameType(GameType.SURVIVAL);
                        }
                    }
                    lastCheck = now;
                }
            }
        }
    }

}
