package com.github.sandtechnology.dungeonplusrelatedleakfixer.modules;

import com.github.sandtechnology.dungeonplusrelatedleakfixer.DungeonPlusRelatedLeakFixer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.serverct.ersha.AttributePlus;
import org.serverct.ersha.attribute.data.AttributeData;
import org.serverct.ersha.dungeon.DungeonPlus;
import org.serverct.ersha.dungeon.internal.manager.DungeonManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

public class AttitudePlusCleanModule implements PluginCleanModule {
    @Override
    public void init() {

    }

    @Override
    public void unInit() {

    }

    @Override
    public void handleCleanAction(String worldName) {
        int removal = 0;
        for (Iterator<AttributeData> it = AttributePlus.instance.getAttributeManager().entityAttributeData.values().iterator(); it.hasNext(); ) {
            AttributeData value = it.next();
            LivingEntity entity = value.sourceEntity;
            if (entity != null) {
                if (entity.getWorld().getName().equalsIgnoreCase(worldName)) {
                    it.remove();
                    removal++;
                }
            }
        }
        DungeonPlusRelatedLeakFixer.getLoggerInstance().log(Level.INFO, "Unloaded " + removal + " mobs from AttributePlus");
    }

    @Override
    public void cleanIfPossible() {
        int removal = 0;
        DungeonManager manager = DungeonPlus.dungeonManager;
        List<String> loadedWorld = new ArrayList<>();
        for (World world1 : Bukkit.getServer().getWorlds()) {
            String name = world1.getName();
            String toLowerCase = name.toLowerCase();
            loadedWorld.add(toLowerCase);
        }
        List<String> unloadedWorld = new ArrayList<>();
        for (Iterator<AttributeData> it = AttributePlus.instance.getAttributeManager().entityAttributeData.values().iterator(); it.hasNext(); ) {
            AttributeData value = it.next();
            LivingEntity entity = value.sourceEntity;
            if (entity != null) {
                World world = entity.getWorld();
                String worldName = world.getName().toLowerCase(Locale.ROOT);
                if (unloadedWorld.contains(worldName)) {
                    it.remove();
                    removal++;
                } else if (manager.isCommonDungeonWorld(world) && !loadedWorld.contains(worldName)) {
                    unloadedWorld.add(worldName);
                    it.remove();
                    removal++;
                }
            }
        }
        DungeonPlusRelatedLeakFixer.getLoggerInstance().log(Level.INFO, "Unloaded " + removal + " mobs from AttributePlus");
    }
}
