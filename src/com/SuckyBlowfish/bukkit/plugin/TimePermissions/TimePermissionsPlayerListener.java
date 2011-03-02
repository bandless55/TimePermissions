package com.SuckyBlowfish.bukkit.plugin.TimePermissions;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockRightClickEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerItemEvent;
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

    public TimePermissionsPlayerListener(TimePermissions instance) {
        plugin = instance;
    }
    
    public void onPlayerJoin(PlayerEvent event){
    	String playerName = event.getPlayer().getName();
    	if (!plugin.playerConfigTime.containsKey(playerName))plugin.playerConfigTime.put(playerName, 0);
    	plugin.playerConfigLastOnline.put(playerName, 0);
    }
    
    public void onPlayerQuit(PlayerEvent event){
    	String playerName = event.getPlayer().getName();
    	plugin.playerConfigLastOnline.put(playerName, ((Long)(System.currentTimeMillis()/1000)).intValue());
    }

    public void onPlayerItem(PlayerItemEvent event){
    	String playerName = event.getPlayer().getName();
    	if(!plugin.playerCanUseItem(event.getPlayer(),event.getItem().getType().getId())){
    		event.setCancelled(true);
    	}
    }
}

