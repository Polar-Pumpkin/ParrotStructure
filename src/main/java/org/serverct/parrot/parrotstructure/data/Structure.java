package org.serverct.parrot.parrotstructure.data;

import lombok.Data;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.serverct.parrot.parrotstructure.ParrotStructure;

import java.util.ArrayList;
import java.util.List;

public abstract class Structure {

    @Getter private final NamespacedKey key;
    @Getter private final List<StructurePart> parts;

    public Structure(NamespacedKey key, List<StructurePart> parts) {
        this.key = key;
        this.parts = parts;
    }

    public abstract void onInteract(PlayerInteractEvent event);

    public abstract void onCreate(BlockPlaceEvent event);

    public abstract void onDestroy(BlockBreakEvent event);

    public void register() {
        ParrotStructure.getInstance().getStructureMap().put(key, this);
    }

    public boolean interact(PlayerInteractEvent event) {
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
        target.removeIf(part -> part.getType() != StructurePart.TriggerType.NONE);
        return target;
    }

    public boolean match(Block block) {
        List<StructurePart> target = new ArrayList<>(parts);
        target.removeIf(part -> part.getMaterial() != block.getType());
        All_Possibility: for (StructurePart targetPart : target) {
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
            BOTH, LEFT, RIGHT, NONE;
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
    }
}
