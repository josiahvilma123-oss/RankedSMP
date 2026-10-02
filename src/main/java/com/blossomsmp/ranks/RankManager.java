package com.blossomsmp.ranks;

import net.milkbowl.vault.economy.Economy;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/** Kill ranks: more player kills = higher rank. Kills come from the normal Minecraft statistic. */
public class RankManager {

    public record Rank(String id, String name, int kills, double reward, List<String> commands) {
    }

    private final BlossomRanks plugin;
    private final File file;
    /** Highest rank number each player has already been rewarded for. */
    private final Map<UUID, Integer> reached = new HashMap<>();
    private final List<Rank> ranks = new ArrayList<>();

    public RankManager(BlossomRanks plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "killranks.yml");
    }

    public void load() {
        loadRanks();
        reached.clear();
        if (!file.exists()) {
            return;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        for (String key : yaml.getKeys(false)) {
            try {
                reached.put(UUID.fromString(key), yaml.getInt(key));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public void loadRanks() {
        ranks.clear();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("ranks");
        if (section == null) {
            return;
        }
        for (String id : section.getKeys(false)) {
            ConfigurationSection r = section.getConfigurationSection(id);
            if (r == null) {
                continue;
            }
            ranks.add(new Rank(id,
                    r.getString("name", id),
                    Math.max(0, r.getInt("kills", 0)),
                    Math.max(0, r.getDouble("reward", 0)),
                    new ArrayList<>(r.getStringList("commands"))));
        }
        ranks.sort(Comparator.comparingInt(Rank::kills));
    }

    public void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        for (Map.Entry<UUID, Integer> entry : reached.entrySet()) {
            yaml.set(entry.getKey().toString(), entry.getValue());
        }
        try {
            plugin.getDataFolder().mkdirs();
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save killranks.yml: " + e.getMessage());
        }
    }

    public boolean enabled() {
        return !ranks.isEmpty();
    }

    public List<Rank> getRanks() {
        return ranks;
    }

    // ------------------------------------------------------------ lookups

    public int kills(OfflinePlayer player) {
        try {
            Player online = player.getPlayer();
            if (online != null) {
                return online.getStatistic(Statistic.PLAYER_KILLS);
            }
            return player.getStatistic(Statistic.PLAYER_KILLS);
        } catch (Exception e) {
            return 0;
        }
    }

    /** Index of the rank for this many kills, or -1 if they don't have the first rank yet. */
    public int indexFor(int kills) {
        int index = -1;
        for (int i = 0; i < ranks.size(); i++) {
            if (kills >= ranks.get(i).kills()) {
                index = i;
            }
        }
        return index;
    }

    public Rank rankOf(OfflinePlayer player) {
        int i = indexFor(kills(player));
        return i < 0 ? null : ranks.get(i);
    }

    public Rank nextRank(OfflinePlayer player) {
        int i = indexFor(kills(player)) + 1;
        return i < ranks.size() ? ranks.get(i) : null;
    }

    // ------------------------------------------------------------ ranking up

    /**
     * Checks if a player has reached a new rank. Gives the rewards for every new rank.
     * announce = false is used on join, so old progress doesn't spam the chat.
     */
    public void check(Player player, boolean announce) {
        if (!enabled()) {
            return;
        }
        UUID id = player.getUniqueId();
        int current = indexFor(kills(player));
        Integer before = reached.get(id);

        if (before == null || current < before) {
            // First time we see them, or stats were reset: just remember, no rewards
            reached.put(id, current);
            save();
            return;
        }
        if (current == before) {
            return;
        }

        reached.put(id, current);
        save();

        double totalReward = 0;
        for (int i = before + 1; i <= current; i++) {
            Rank rank = ranks.get(i);
            totalReward += rank.reward();
            for (String command : rank.commands()) {
                String cmd = command.replace("{player}", player.getName()).replace("%player%", player.getName());
                if (cmd.startsWith("/")) {
                    cmd = cmd.substring(1);
                }
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            }
        }
        if (totalReward > 0) {
            Economy economy = plugin.economy();
            if (economy != null) {
                economy.depositPlayer(player, totalReward);
            } else {
                plugin.getLogger().warning("No Vault economy found - could not give " + player.getName() + " their rank reward.");
            }
        }
        if (!announce) {
            return;
        }

        Rank rank = ranks.get(current);
        String kills = String.valueOf(kills(player));
        String reward = Text.money(totalReward);

        if (plugin.getConfig().getBoolean("broadcast", true)) {
            String message = plugin.getConfig().getString("broadcast-message", "");
            if (message != null && !message.isEmpty()) {
                Bukkit.broadcast(Text.component(message
                        .replace("{player}", player.getName())
                        .replace("{rank}", rank.name())
                        .replace("{kills}", kills)));
            }
        }

        String title = plugin.getConfig().getString("title", "{rank}").replace("{rank}", rank.name());
        String subtitle = plugin.getConfig().getString("subtitle", "")
                .replace("{rank}", rank.name()).replace("{reward}", reward).replace("{kills}", kills);
        player.showTitle(Title.title(Text.component(title), Text.component(subtitle),
                Title.Times.times(Duration.ofMillis(300), Duration.ofSeconds(3), Duration.ofMillis(700))));

        String sound = plugin.getConfig().getString("sound", "");
        if (sound != null && !sound.isBlank()) {
            try {
                player.playSound(player.getLocation(), sound.trim().toLowerCase(Locale.ROOT), 1f, 1f);
            } catch (Exception ignored) {
            }
        }
    }
}
