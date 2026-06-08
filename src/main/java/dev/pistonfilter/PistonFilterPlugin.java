package dev.pistonfilter;

import org.bukkit.plugin.java.JavaPlugin;

public final class PistonFilterPlugin extends JavaPlugin {

    private static PistonFilterPlugin instance;
    private FilterConfig filterConfig;

    @Override
    public void onEnable() {
        instance = this;

        // Save default config if absent
        saveDefaultConfig();

        // Load our typed config wrapper
        filterConfig = new FilterConfig(this);

        // Register piston event listener
        getServer().getPluginManager().registerEvents(new PistonListener(this), this);

        // Register /pistonfilter command
        PistonCommand command = new PistonCommand(this);
        getCommand("pistonfilter").setExecutor(command);
        getCommand("pistonfilter").setTabCompleter(command);

        getLogger().info("PistonFilter enabled — watching " + filterConfig.getForbiddenBlocks().size() + " forbidden block(s).");
    }

    @Override
    public void onDisable() {
        getLogger().info("PistonFilter disabled.");
    }

    public static PistonFilterPlugin getInstance() {
        return instance;
    }

    public FilterConfig getFilterConfig() {
        return filterConfig;
    }

    /** Reload config from disk and refresh in-memory state. */
    public void reloadFilterConfig() {
        reloadConfig();
        filterConfig = new FilterConfig(this);
    }
}
