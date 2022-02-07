package com.github.sandtechnology.dungeonplusrelatedleakfixer.modules;

import com.github.sandtechnology.dungeonplusrelatedleakfixer.DungeonPlusRelatedLeakFixer;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.mobs.MobRegistry;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.serverct.ersha.dungeon.DungeonPlus;
import org.serverct.ersha.dungeon.internal.manager.DungeonManager;

import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Level;

public class MythicMobsCleanModule implements PluginCleanModule {
    @Nullable
    private Field persistentMobsField;

    @Override
    public void init() {
        try {
            persistentMobsField = MobRegistry.class.getDeclaredField("persistentMobs");
            persistentMobsField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Failed to init MythicMobsCleanModule!", e);
        }
    }

    @Override
    public void unInit() {

    }

    @Override
    public void cleanIfPossible() {
        if (persistentMobsField != null) {
            DungeonManager manager = DungeonPlus.dungeonManager;
            List<String> loadedWorld = new ArrayList<>();
            for (World world1 : Bukkit.getServer().getWorlds()) {
                String name = world1.getName();
                String toLowerCase = name.toLowerCase();
                loadedWorld.add(toLowerCase);
            }
            List<String> unloadedWorld = new ArrayList<>();
            int unloaded = 0;
            MobRegistry mobRegistry = MythicMobs.inst().getMobManager().getMobRegistry().get();
            for (Iterator<ActiveMob> it = mobRegistry.values().iterator(); it.hasNext(); ) {
                ActiveMob value = it.next();
                AbstractEntity entity = value.getEntity();
                if (entity != null) {
                    Entity bukkitEntity = entity.getBukkitEntity();
                    if (bukkitEntity != null) {
                        World world = bukkitEntity.getWorld();
                        String worldName = world.getName().toLowerCase(Locale.ROOT);
                        if (unloadedWorld.contains(worldName)) {
                            it.remove();
                            unloaded++;
                        } else if (manager.isCommonDungeonWorld(world) && !loadedWorld.contains(worldName)) {
                            unloadedWorld.add(worldName);
                            it.remove();
                            unloaded++;
                        }
                    }
                }
            }
            try {
                Map<UUID, ActiveMob> persistentMobsMap = (Map<UUID, ActiveMob>) persistentMobsField.get(mobRegistry);
                for (Iterator<ActiveMob> it = persistentMobsMap.values().iterator(); it.hasNext(); ) {
                    ActiveMob value = it.next();
                    AbstractEntity entity = value.getEntity();
                    if (entity != null) {
                        Entity bukkitEntity = entity.getBukkitEntity();
                        if (bukkitEntity != null) {
                            World world = bukkitEntity.getWorld();
                            String worldName = world.getName().toLowerCase(Locale.ROOT);
                            if (unloadedWorld.contains(worldName)) {
                                it.remove();
                                unloaded++;
                            } else if (manager.isCommonDungeonWorld(world) && !loadedWorld.contains(worldName)) {
                                unloadedWorld.add(worldName);
                                it.remove();
                                unloaded++;
                            }
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            DungeonPlusRelatedLeakFixer.getLoggerInstance().log(Level.INFO, "Unloaded " + unloaded + " mobs from MythicMobs");

        }
    }

    @Override
    public void handleCleanAction(String worldName) {
        if (persistentMobsField != null) {
            int unloaded = 0;
            MobRegistry mobRegistry = MythicMobs.inst().getMobManager().getMobRegistry().get();
            for (Iterator<ActiveMob> it = mobRegistry.values().iterator(); it.hasNext(); ) {
                ActiveMob value = it.next();
                AbstractEntity entity = value.getEntity();
                if (entity != null) {
                    Entity bukkitEntity = entity.getBukkitEntity();
                    if (bukkitEntity != null && bukkitEntity.getWorld().getName().equalsIgnoreCase(worldName)) {
                        it.remove();
                        unloaded++;
                    }
                }
            }
            try {
                Map<UUID, ActiveMob> persistentMobsMap = (Map<UUID, ActiveMob>) persistentMobsField.get(mobRegistry);
                for (Iterator<ActiveMob> it = persistentMobsMap.values().iterator(); it.hasNext(); ) {
                    ActiveMob value = it.next();
                    AbstractEntity entity = value.getEntity();
                    if (entity != null) {
                        Entity bukkitEntity = entity.getBukkitEntity();
                        if (bukkitEntity != null && bukkitEntity.getWorld().getName().equalsIgnoreCase(worldName)) {
                            it.remove();
                            unloaded++;
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            DungeonPlusRelatedLeakFixer.getLoggerInstance().log(Level.INFO, "Unloaded " + unloaded + " mobs from MythicMobs");
        }
    }
}
