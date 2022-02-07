package com.github.sandtechnology.dungeonplusrelatedleakfixer.modules;

public interface PluginCleanModule {
    void init();

    void unInit();

    void handleCleanAction(String worldName);

    void cleanIfPossible();
}
