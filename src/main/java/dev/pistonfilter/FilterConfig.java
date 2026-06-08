package dev.pistonfilter;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
 * Typed wrapper around the plugin's config.yml.
 */
public class FilterConfig {

    private final Set<Material> forbiddenBlocks = EnumSet.noneOf(Material.class);
    private boolean notifyPlayers;
    private int notifyRadius;
    private String notifyMessage;

    public FilterConfig(PistonFilterPlugin plugin) {
        FileConfiguration cfg = plugin.getConfig();

        // Parse forbidden block list
        List<String> names = cfg.getStringList("forbidden-blocks");
        for (String name : names) {
            Material mat = Material.matchMaterial(name.toUpperCase());
            if (mat == null) {
                plugin.getLogger().log(Level.WARNING, "Unknown material in forbidden-blocks: ''{0}'' — skipping.", name);
            } else {
                forbiddenBlocks.add(mat);
            }
        }

        notifyPlayers = cfg.getBoolean("notify-players", false);
        notifyRadius  = cfg.getInt("notify-radius", 10);
        notifyMessage = cfg.getString("notify-message", "&cA piston was blocked by a protected block!");
    }

    public Set<Material> getForbiddenBlocks() {
        return forbiddenBlocks;
    }

    public boolean isForbidden(Material material) {
        return forbiddenBlocks.contains(material);
    }

    public boolean isNotifyPlayers() {
        return notifyPlayers;
    }

    public int getNotifyRadius() {
        return notifyRadius;
    }

    public String getNotifyMessage() {
        return notifyMessage;
    }
}
