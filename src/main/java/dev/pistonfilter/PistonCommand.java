package dev.pistonfilter;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * /pistonfilter <reload | list | add <block> | remove <block>>
 */
public class PistonCommand implements CommandExecutor, TabCompleter {

    private static final String PREFIX = ChatColor.GRAY + "[" + ChatColor.AQUA + "PistonFilter" + ChatColor.GRAY + "] ";

    private final PistonFilterPlugin plugin;

    public PistonCommand(PistonFilterPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {

        if (!sender.hasPermission("pistonfilter.admin")) {
            sender.sendMessage(PREFIX + ChatColor.RED + "You don't have permission.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "reload" -> {
                plugin.reloadFilterConfig();
                int count = plugin.getFilterConfig().getForbiddenBlocks().size();
                sender.sendMessage(PREFIX + ChatColor.GREEN + "Config reloaded. " + count + " forbidden block(s) loaded.");
            }

            case "list" -> {
                var blocks = plugin.getFilterConfig().getForbiddenBlocks();
                if (blocks.isEmpty()) {
                    sender.sendMessage(PREFIX + ChatColor.YELLOW + "No forbidden blocks configured.");
                } else {
                    sender.sendMessage(PREFIX + ChatColor.AQUA + "Forbidden blocks (" + blocks.size() + "):");
                    blocks.forEach(mat -> sender.sendMessage(ChatColor.GRAY + "  - " + ChatColor.WHITE + mat.name()));
                }
            }

            case "add" -> {
                if (args.length < 2) {
                    sender.sendMessage(PREFIX + ChatColor.RED + "Usage: /pistonfilter add <block>");
                    return true;
                }
                Material mat = Material.matchMaterial(args[1].toUpperCase());
                if (mat == null || !mat.isBlock()) {
                    sender.sendMessage(PREFIX + ChatColor.RED + "Unknown block: " + args[1]);
                    return true;
                }
                addToConfig(mat);
                plugin.reloadFilterConfig();
                sender.sendMessage(PREFIX + ChatColor.GREEN + "Added " + mat.name() + " to the forbidden list.");
            }

            case "remove" -> {
                if (args.length < 2) {
                    sender.sendMessage(PREFIX + ChatColor.RED + "Usage: /pistonfilter remove <block>");
                    return true;
                }
                Material mat = Material.matchMaterial(args[1].toUpperCase());
                if (mat == null) {
                    sender.sendMessage(PREFIX + ChatColor.RED + "Unknown block: " + args[1]);
                    return true;
                }
                boolean removed = removeFromConfig(mat);
                plugin.reloadFilterConfig();
                if (removed) {
                    sender.sendMessage(PREFIX + ChatColor.GREEN + "Removed " + mat.name() + " from the forbidden list.");
                } else {
                    sender.sendMessage(PREFIX + ChatColor.YELLOW + mat.name() + " was not in the forbidden list.");
                }
            }

            default -> sendHelp(sender);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender,
                                      @NotNull Command command,
                                      @NotNull String alias,
                                      @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.asList("reload", "list", "add", "remove");
        }
        if (args.length == 2) {
            String input = args[1].toUpperCase();
            if (args[0].equalsIgnoreCase("add")) {
                return Arrays.stream(Material.values())
                        .filter(Material::isBlock)
                        .map(Material::name)
                        .filter(n -> n.startsWith(input))
                        .limit(20)
                        .collect(Collectors.toList());
            }
            if (args[0].equalsIgnoreCase("remove")) {
                return plugin.getFilterConfig().getForbiddenBlocks().stream()
                        .map(Material::name)
                        .filter(n -> n.startsWith(input))
                        .collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }

    // -------------------------------------------------------------------------

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(PREFIX + ChatColor.AQUA + "Commands:");
        sender.sendMessage(ChatColor.GRAY + "  /pistonfilter reload " + ChatColor.WHITE + "— Reload config from disk");
        sender.sendMessage(ChatColor.GRAY + "  /pistonfilter list " + ChatColor.WHITE + "— Show all forbidden blocks");
        sender.sendMessage(ChatColor.GRAY + "  /pistonfilter add <block> " + ChatColor.WHITE + "— Add a block to the forbidden list");
        sender.sendMessage(ChatColor.GRAY + "  /pistonfilter remove <block> " + ChatColor.WHITE + "— Remove a block from the forbidden list");
    }

    /** Persists a new material to config.yml on disk. */
    private void addToConfig(Material mat) {
        FileConfiguration cfg = plugin.getConfig();
        List<String> list = cfg.getStringList("forbidden-blocks");
        if (!list.contains(mat.name())) {
            list.add(mat.name());
            cfg.set("forbidden-blocks", list);
            plugin.saveConfig();
        }
    }

    /** Removes a material from config.yml on disk. Returns true if it was present. */
    private boolean removeFromConfig(Material mat) {
        FileConfiguration cfg = plugin.getConfig();
        List<String> list = cfg.getStringList("forbidden-blocks");
        boolean removed = list.remove(mat.name());
        if (removed) {
            cfg.set("forbidden-blocks", list);
            plugin.saveConfig();
        }
        return removed;
    }
}
