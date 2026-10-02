package com.blossomsmp.ranks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

import java.util.Locale;

/**
 *   %blossomranks_rank%          -> Blossom (with colours)
 *   %blossomranks_kills%         -> 31
 *   %blossomranks_next_rank%     -> Petal Warrior
 *   %blossomranks_kills_needed%  -> 19
 */
public final class RankPlaceholders extends PlaceholderExpansion {

    private final BlossomRanks plugin;

    public RankPlaceholders(BlossomRanks plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "blossomranks";
    }

    @Override
    public String getAuthor() {
        return "BlossomSMP";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player == null) {
            return "";
        }
        RankManager manager = plugin.ranks();
        switch (params.toLowerCase(Locale.ROOT)) {
            case "rank": {
                RankManager.Rank rank = manager.rankOf(player);
                return rank == null ? "-" : Text.section(rank.name());
            }
            case "kills":
                return String.valueOf(manager.kills(player));
            case "next_rank": {
                RankManager.Rank next = manager.nextRank(player);
                return next == null
                        ? Text.section(plugin.getConfig().getString("max-rank-text", "MAX"))
                        : Text.section(next.name());
            }
            case "kills_needed": {
                RankManager.Rank next = manager.nextRank(player);
                return next == null ? "0" : String.valueOf(Math.max(0, next.kills() - manager.kills(player)));
            }
            default:
                return null;
        }
    }
}
