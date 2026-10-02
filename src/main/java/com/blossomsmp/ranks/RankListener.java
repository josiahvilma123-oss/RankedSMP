package com.blossomsmp.ranks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class RankListener implements Listener {

    private final BlossomRanks plugin;

    public RankListener(BlossomRanks plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        plugin.ranks().check(event.getPlayer(), false);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onKill(PlayerDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null || killer.equals(event.getEntity())) {
            return;
        }
        // Wait one tick so Minecraft has counted the kill
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (killer.isOnline()) {
                plugin.ranks().check(killer, true);
            }
        });
    }
}
