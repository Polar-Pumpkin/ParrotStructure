package org.serverct.parrot.parrotstructure;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
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

    public Structure matchAsync(Block block, Material material) {
        final Structure[] result = {null};
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Structure structure : structureMap.values()) {
                    if (structure.match(block, material)) {
                        result[0] = structure;
                    }
                }
                result[0] = null;
            }
        }.runTaskAsynchronously(this);
        return result[0];
    }

    public Structure match(Block block, Material material) {
        for (Structure structure : structureMap.values()) {
            if (structure.match(block, material)) {
                return structure;
            }
        }
        return null;
    }
}
