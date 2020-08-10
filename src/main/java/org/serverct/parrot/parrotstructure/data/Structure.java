package org.serverct.parrot.parrotstructure.data;

import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.serverct.parrot.parrotstructure.ParrotStructure;

import java.util.*;

public @Data
abstract class Structure {

    private final Map<UUID, Boolean> cooldownMap = new HashMap<>();

    private final NamespacedKey key;
    private final List<StructurePart> parts;

    public abstract void onInteract(PlayerInteractEvent event);

    public abstract void onCreate(BlockPlaceEvent event);

    public abstract void onDestroy(BlockBreakEvent event);

    public void register() {
        ParrotStructure.getInstance().getStructureMap().put(key, this);
    }

    public void interact(PlayerInteractEvent event) {
        UUID user = event.getPlayer().getUniqueId();
        if (!cooldownMap.getOrDefault(user, false)) {
            cooldownMap.put(user, true);
            onInteract(event);
            Bukkit.getScheduler().runTaskLaterAsynchronously(ParrotStructure.getInstance(), () -> {
                cooldownMap.put(user, false);
            }, 10L);
        }
    }

    public boolean isInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null) return false;
        for (StructurePart part : getTrigger()) {
            if (part.getMaterial() == block.getType()) {
                Action action = event.getAction();
                switch (part.getType()) {
                    case LEFT:
                        if (action == Action.LEFT_CLICK_BLOCK) {
                            return true;
                        }
                    case RIGHT:
                        if (action == Action.RIGHT_CLICK_BLOCK) {
                            return true;
                        }
                    case BOTH: {
                        if (action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public List<StructurePart> getTrigger() {
        List<StructurePart> target = new ArrayList<>(parts);
        target.removeIf(part -> part.getType() == StructurePart.TriggerType.NONE);
        return target;
    }

    public boolean match(Block block, Material material) {
        List<StructurePart> target = new ArrayList<>(parts);
        target.removeIf(part -> part.getMaterial() != (material == null ? block.getType() : material));
        All_Possibility:
        for (StructurePart targetPart : target) {
            RelativeLocation center = targetPart.getLocation();
            for (StructurePart part : parts) {
                if (part.getLocation().center(center).get(block.getLocation()).getBlock().getType() != part.getMaterial()) {
                    continue All_Possibility;
                }
            }
            return true;
        }
        return false;
    }

    public @Data
    static class StructurePart {
        private final Material material;
        private final TriggerType type;
        private final RelativeLocation location;

        public enum TriggerType {
            BOTH, LEFT, RIGHT, NONE
        }
    }

    public @Data
    static class RelativeLocation {
        private final int x;
        private final int y;
        private final int z;

        RelativeLocation add(int x, int y, int z) {
            return new RelativeLocation(this.x + x, this.y + y, this.z + z);
        }

        RelativeLocation add(RelativeLocation loc) {
            return new RelativeLocation(this.x + loc.x, this.y + loc.y, this.z + loc.z);
        }

        Location get(Location loc) {
            return loc.clone().add(x, y, z);
        }

        RelativeLocation center(int x, int y, int z) {
            return new RelativeLocation(this.x - x, this.y - y, this.z - z);
        }

        RelativeLocation center(RelativeLocation loc) {
            return new RelativeLocation(this.x - loc.x, this.y - loc.y, this.z - loc.z);
        }

        String out() {
            return "(x, y, z)"
                    .replace("x", String.valueOf(x))
                    .replace("y", String.valueOf(y))
                    .replace("z", String.valueOf(z));

        }
    }
}
