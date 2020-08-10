package org.serverct.parrot.parrotstructure.listener;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.serverct.parrot.parrotstructure.ParrotStructure;
import org.serverct.parrot.parrotstructure.data.Structure;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StructureListener implements Listener {

    private final Map<UUID, Material> damageMap = new HashMap<>();

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(ParrotStructure.getInstance(), () -> {
            Structure structure = ParrotStructure.getInstance().match(event.getBlock());
            if (structure != null) {
                structure.onCreate(event);
            }
        });
    }

    @EventHandler
    public void onDamage(BlockDamageEvent event) {
        UUID user = event.getPlayer().getUniqueId();
        damageMap.put(user, event.getBlock().getType());
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(ParrotStructure.getInstance(), () -> {
            Structure structure = ParrotStructure.getInstance().destroyMatch(event.getBlock(), damageMap.getOrDefault(event.getPlayer().getUniqueId(), Material.AIR));
            if (structure != null) {
                structure.onDestroy(event);
            }
        });
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        switch (event.getAction()) {
            case LEFT_CLICK_BLOCK:
            case RIGHT_CLICK_BLOCK:
                Bukkit.getScheduler().runTaskAsynchronously(ParrotStructure.getInstance(), () -> {
                    Structure structure = ParrotStructure.getInstance().match(event.getClickedBlock());
                    if (structure != null && structure.isInteract(event)) {
                        structure.interact(event);
                    }
                });
                break;
        }
    }

}
