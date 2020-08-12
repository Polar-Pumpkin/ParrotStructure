package org.serverct.parrot.parrotstructure.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.serverct.parrot.parrotstructure.ParrotStructure;

import java.util.*;

public @Data
abstract class Structure {

    private final Map<UUID, Boolean> cooldownMap = new HashMap<>();

    private final NamespacedKey key;
    private final List<StructurePart> parts;
    private final ItemStack activeKey;

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
            ItemStack hold = event.getItem();

            if (activeKey != null) {
                if (hold == null) {
                    return;
                }
                if (!activeKey.isSimilar(hold)) {
                    return;
                }
                hold.setAmount(hold.getAmount() - 1);
            }

            onInteract(event);

            Block clicked = event.getClickedBlock();
            if (clicked != null) {
                Location center = clicked.getLocation();
                StructurePart trigger = getTrigger(clicked, event.getAction());

                if (trigger != null) {
                    this.parts.forEach(part -> {
                        Location target = part.getLocation().center(trigger.getLocation()).get(center);
                        if (part.disappear) {
                            target.getBlock().setType(Material.AIR);
                        } else if (part.broke) {
                            target.getBlock().breakNaturally();
                        }
                        if (part.explode > 0) {
                            World world = target.getWorld();
                            if (world != null) {
                                world.createExplosion(target, part.explode);
                            }
                        }
                    });
                }
            }

            Bukkit.getScheduler().runTaskLaterAsynchronously(ParrotStructure.getInstance(), () -> cooldownMap.put(user, false), 10L);
        }
    }

    public boolean isInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null) return false;
        return getTrigger(block, event.getAction()) != null;
    }

    public List<StructurePart> getTrigger() {
        List<StructurePart> target = new ArrayList<>(parts);
        target.removeIf(part -> part.getType() == StructurePart.TriggerType.NONE);
        return target;
    }

    public StructurePart getTrigger(Block block, Action action) {
        for (StructurePart part : getTrigger()) {
            if (part.getMaterial() == block.getType()) {
                switch (part.getType()) {
                    case LEFT:
                        if (action == Action.LEFT_CLICK_BLOCK) {
                            return part;
                        }
                    case RIGHT:
                        if (action == Action.RIGHT_CLICK_BLOCK) {
                            return part;
                        }
                    case BOTH: {
                        if (action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK) {
                            return part;
                        }
                    }
                }
            }
        }
        return null;
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
    @AllArgsConstructor
    static class StructurePart {
        private final Material material;
        private final TriggerType type;
        private final RelativeLocation location;
        private boolean disappear = false;
        private float explode = -1;
        private boolean broke = false;

        public StructurePart(Material material, TriggerType type, RelativeLocation location) {
            this.material = material;
            this.type = type;
            this.location = location;
        }

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
