package org.serverct.parrot.parrotstructure.listener;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.serverct.parrot.parrotstructure.ParrotStructure;
import org.serverct.parrot.parrotstructure.data.Structure;

public class StructureListener implements Listener {

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
    public void onBreak(BlockBreakEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(ParrotStructure.getInstance(), () -> {
            Structure structure = ParrotStructure.getInstance().match(event.getBlock());
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
                    if (structure != null && structure.interact(event)) {
                        structure.onInteract(event);
                    }
                });
                break;
        }
    }

}
