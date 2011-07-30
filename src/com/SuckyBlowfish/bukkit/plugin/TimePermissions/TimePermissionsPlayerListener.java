package com.SuckyBlowfish.bukkit.plugin.TimePermissions;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Handle events for all Player related events
 * @author SuckyBlowfish
 */
public class TimePermissionsPlayerListener extends PlayerListener {
    private final TimePermissions plugin;

    public TimePermissionsPlayerListener(TimePermissions plugin) {
        this.plugin = plugin;
    }

//    public void onPlayerIteract(PlayerInteractEvent event){
//    	String playerName = event.getPlayer().getName();
//    	if(!plugin.playerCanUseItem(event.getPlayer(),event.getItem().getType().getId())){
//    		event.setCancelled(true);
//    	}
//    }
}

