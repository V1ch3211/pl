package dev.pistonfilter;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Listens for piston extend/retract events and cancels them when the block
 * chain contains a forbidden block.
 *
 * Minecraft limits a piston to pushing at most 12 blocks, but we also walk
 * up to 16 blocks in front of the piston head manually so retract-pulls and
 * edge cases are covered.
 */
public class PistonListener implements Listener {

    /** Maximum blocks to inspect in front of the piston face. */
    private static final int MAX_SCAN = 16;

    private final PistonFilterPlugin plugin;

    public PistonListener(PistonFilterPlugin plugin) {
        this.plugin = plugin;
    }

    // -------------------------------------------------------------------------
    // Extend (push)
    // -------------------------------------------------------------------------

    /**
     * Bukkit provides the full list of blocks that will be pushed via
     * {@link BlockPistonExtendEvent#getBlocks()}.  We iterate that list
     * directly — it already respects Minecraft's 12-block push limit.
     * Additionally we walk up to MAX_SCAN blocks manually so that blocks
     * beyond the push chain (e.g. immovable blocks acting as stoppers) are
     * also checked.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        FilterConfig cfg = plugin.getFilterConfig();
        BlockFace direction = event.getDirection();

        // 1. Check every block Bukkit says will be pushed
        List<Block> pushed = event.getBlocks();
        for (Block block : pushed) {
            if (cfg.isForbidden(block.getType())) {
                event.setCancelled(true);
                notifyNearby(event.getBlock(), cfg);
                plugin.getLogger().fine(() -> "Piston extend cancelled — forbidden block in push list: " + block.getType() + " at " + block.getLocation());
                return;
            }
        }

        // 2. Manual scan ahead of the piston face (catches immovable stoppers)
        Block pistonHead = event.getBlock().getRelative(direction);
        for (int i = 0; i < MAX_SCAN; i++) {
            Block scanned = pistonHead.getRelative(direction, i);
            Material mat = scanned.getType();

            if (cfg.isForbidden(mat)) {
                event.setCancelled(true);
                notifyNearby(event.getBlock(), cfg);
                plugin.getLogger().fine(() -> "Piston extend cancelled — forbidden block in scan: " + mat + " at " + scanned.getLocation());
                return;
            }

            // Stop scanning when we hit air or a naturally immovable block
            // (Minecraft itself will handle the push failure beyond this point)
            if (mat == Material.AIR || mat == Material.CAVE_AIR || mat == Material.VOID_AIR) {
                break;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Retract (pull)
    // -------------------------------------------------------------------------

    /**
     * For sticky pistons retracting, Bukkit provides the single pulled block
     * via {@link BlockPistonRetractEvent#getBlocks()}.  We also scan the
     * column behind the piston up to MAX_SCAN blocks for extra safety.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        FilterConfig cfg = plugin.getFilterConfig();
        BlockFace direction = event.getDirection();

        // 1. Check the block(s) Bukkit says will be pulled
        for (Block block : event.getBlocks()) {
            if (cfg.isForbidden(block.getType())) {
                event.setCancelled(true);
                notifyNearby(event.getBlock(), cfg);
                plugin.getLogger().fine(() -> "Piston retract cancelled — forbidden block in pull list: " + block.getType() + " at " + block.getLocation());
                return;
            }
        }

        // 2. Manual scan in the direction the piston is pulling from
        Block pistonBase = event.getBlock();
        for (int i = 1; i <= MAX_SCAN; i++) {
            Block scanned = pistonBase.getRelative(direction, i);
            Material mat = scanned.getType();

            if (cfg.isForbidden(mat)) {
                event.setCancelled(true);
                notifyNearby(event.getBlock(), cfg);
                plugin.getLogger().fine(() -> "Piston retract cancelled — forbidden block in scan: " + mat + " at " + scanned.getLocation());
                return;
            }

            if (mat == Material.AIR || mat == Material.CAVE_AIR || mat == Material.VOID_AIR) {
                break;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Notifies players within the configured radius about the cancelled piston.
     */
    private void notifyNearby(Block pistonBlock, FilterConfig cfg) {
        if (!cfg.isNotifyPlayers()) return;

        String raw = cfg.getNotifyMessage();
        String message = org.bukkit.ChatColor.translateAlternateColorCodes('&', raw);
        double radiusSq = Math.pow(cfg.getNotifyRadius(), 2);

        for (Player player : pistonBlock.getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(pistonBlock.getLocation()) <= radiusSq) {
                player.sendMessage(message);
            }
        }
    }
}
