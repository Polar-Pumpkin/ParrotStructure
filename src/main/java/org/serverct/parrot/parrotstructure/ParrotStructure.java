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
import java.util.concurrent.atomic.AtomicReference;

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

    public Structure match(Block block, Material material) {
        AtomicReference<Structure> result = new AtomicReference<>();
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            for (Structure structure : structureMap.values()) {
                if (structure.match(block, material)) {
                    result.set(structure);
                }
            }
            result.set(null);
        });
        return result.get();
    }
}
