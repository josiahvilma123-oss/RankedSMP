package com.blossomsmp.ranks;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class BlossomRanks extends JavaPlugin {

    private RankManager ranks;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        ranks = new RankManager(this);
        ranks.load();

        getServer().getPluginManager().registerEvents(new RankListener(this), this);

        RanksCommand command = new RanksCommand(this);
        if (getCommand("killranks") != null) {
            getCommand("killranks").setExecutor(command);
            getCommand("killranks").setTabCompleter(command);
        }

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new RankPlaceholders(this).register();
            getLogger().info("Hooked into PlaceholderAPI (%blossomranks_...% placeholders).");
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            ranks.check(player, false);
        }
        getLogger().info("BlossomRanks enabled with " + ranks.getRanks().size() + " kill ranks.");
    }

    @Override
    public void onDisable() {
        if (ranks != null) {
            ranks.save();
        }
    }

    public void reload() {
        reloadConfig();
        ranks.loadRanks();
    }

    public RankManager ranks() {
        return ranks;
    }

    /** Vault economy (BlossomEconomy), or null if there isn't one. */
    public Economy economy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return null;
        }
        RegisteredServiceProvider<Economy> provider = getServer().getServicesManager().getRegistration(Economy.class);
        return provider == null ? null : provider.getProvider();
    }

    /** A message from config with the prefix. */
    public String msg(String key) {
        return getConfig().getString("messages.prefix", "") + getConfig().getString("messages." + key, "");
    }

    public String raw(String key) {
        return getConfig().getString("messages." + key, "");
    }
}
