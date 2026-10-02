package com.blossomsmp.ranks;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/** /killranks  and  /killranks reload */
public final class RanksCommand implements TabExecutor {

    private final BlossomRanks plugin;

    public RanksCommand(BlossomRanks plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("blossomranks.admin")) {
                sender.sendMessage(Text.component(plugin.msg("no-permission")));
                return true;
            }
            plugin.reload();
            sender.sendMessage(Text.component(plugin.msg("reloaded")));
            return true;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Text.component(plugin.msg("players-only")));
            return true;
        }

        RankManager manager = plugin.ranks();
        int kills = manager.kills(player);
        player.sendMessage(Text.component(plugin.raw("header").replace("{kills}", String.valueOf(kills))));
        for (RankManager.Rank rank : manager.getRanks()) {
            String line = kills >= rank.kills() ? plugin.raw("line-done") : plugin.raw("line-todo");
            player.sendMessage(Text.component(line
                    .replace("{rank}", rank.name())
                    .replace("{kills}", String.valueOf(rank.kills()))));
        }
        RankManager.Rank next = manager.nextRank(player);
        if (next == null) {
            player.sendMessage(Text.component(plugin.raw("max")));
        } else {
            player.sendMessage(Text.component(plugin.raw("next")
                    .replace("{rank}", next.name())
                    .replace("{needed}", String.valueOf(next.kills() - kills))));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1 && sender.hasPermission("blossomranks.admin") && "reload".startsWith(args[0].toLowerCase())) {
            out.add("reload");
        }
        return out;
    }
}
