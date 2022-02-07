package com.github.sandtechnology.dungeonplusrelatedleakfixer;

import com.github.sandtechnology.dungeonplusrelatedleakfixer.modules.AttitudePlusCleanModule;
import com.github.sandtechnology.dungeonplusrelatedleakfixer.modules.MythicMobsCleanModule;
import com.github.sandtechnology.dungeonplusrelatedleakfixer.modules.PluginCleanModule;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.serverct.ersha.dungeon.DungeonPlus;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DungeonPlusRelatedLeakFixer extends JavaPlugin implements Listener {

    private static final Map<String, PluginCleanModule> pluginCleanModuleMap = new ConcurrentHashMap<>();
    private static Logger logger;

    static {
        pluginCleanModuleMap.put("AttributePlus", new AttitudePlusCleanModule());
        pluginCleanModuleMap.put("MythicMobs", new MythicMobsCleanModule());
    }

    private final List<PluginCleanModule> pluginCleanModuleList = new LinkedList<>();

    public DungeonPlusRelatedLeakFixer() {
        logger = super.getLogger();
    }

    @NotNull
    public static Logger getLoggerInstance() {
        return logger;
    }

    @Override
    public void onEnable() {

        PluginManager pluginManager = getServer().getPluginManager();
        for (Map.Entry<String, PluginCleanModule> moduleEntry : pluginCleanModuleMap.entrySet()) {
            if (pluginManager.isPluginEnabled(moduleEntry.getKey())) {
                pluginCleanModuleList.add(moduleEntry.getValue());
            }
        }
        for (PluginCleanModule module : pluginCleanModuleList) {
            try {
                module.init();
                getLogger().log(Level.INFO, module.getClass().getName() + " Loaded!");
            } catch (Throwable e) {
                getLogger().log(Level.WARNING, "Failed to init module " + module.getClass().getName(), e);
            }
        }
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getScheduler().runTask(this, () -> {
            for (PluginCleanModule cleanModule : pluginCleanModuleList) {
                cleanModule.cleanIfPossible();
            }
        });
        // Plugin startup logic
    }

    @Override
    public void onDisable() {
        for (PluginCleanModule module : pluginCleanModuleList) {
            try {
                module.unInit();
                getLogger().log(Level.INFO, module.getClass().getName() + " Unloaded!");
            } catch (Throwable e) {
                getLogger().log(Level.WARNING, "Failed to unInit module " + module.getClass().getName(), e);
            }
        }
        HandlerList.unregisterAll((Listener) this);
        // Plugin shutdown logic
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        World world = event.getWorld();
        logger.log(Level.INFO, "World is unloading, world name is " + world.getName());
        if (DungeonPlus.dungeonManager.isCommonDungeonWorld(world)) {
            logger.log(Level.INFO, "Unloading mob in plugins where world name is " + world.getName());
            if (!Bukkit.isPrimaryThread()) {
                getServer().getScheduler().runTask(this, () ->
                        doCleanAction(world.getName()));
            } else {
                doCleanAction(world.getName());
            }
        }
    }

    private void doCleanAction(String worldName) {
        for (PluginCleanModule module : pluginCleanModuleList) {
            module.handleCleanAction(worldName);
        }
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        PluginCleanModule cleanModule = pluginCleanModuleMap.get(event.getPlugin().getName());
        if (cleanModule != null) {
            cleanModule.init();
            getLogger().log(Level.INFO, cleanModule.getClass().getName() + " Loaded!");
            pluginCleanModuleList.add(cleanModule);
        }
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        PluginCleanModule cleanModule = pluginCleanModuleMap.get(event.getPlugin().getName());
        if (cleanModule != null) {
            cleanModule.unInit();
            getLogger().log(Level.INFO, cleanModule.getClass().getName() + " Unloaded!");
            pluginCleanModuleList.remove(cleanModule);
        }
    }

}
