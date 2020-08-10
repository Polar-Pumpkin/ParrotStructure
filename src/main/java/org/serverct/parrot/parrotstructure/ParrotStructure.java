package org.serverct.parrot.parrotstructure;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;
import org.serverct.parrot.parrotstructure.data.Structure;
import org.serverct.parrot.parrotstructure.listener.StructureListener;

import java.util.HashMap;
import java.util.Map;

public final class ParrotStructure extends JavaPlugin {

    @Getter private static ParrotStructure instance;
    @Getter private final Map<NamespacedKey, Structure> structureMap = new HashMap<>();

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;

        Bukkit.getPluginManager().registerEvents(new StructureListener(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        Bukkit.getScheduler().cancelTasks(this);
    }

    public Structure match(Block block) {
        for (Structure structure : structureMap.values()) {
            if (structure.match(block)) {
                return structure;
            }
        }
        return null;
    }

    public Structure destroyMatch(Block block, Material material) {
        for (Structure structure : structureMap.values()) {
            if (structure.destroyMatch(block, material)) {
                return structure;
            }
        }
        return null;
    }
}
