package com.SuckyBlowfish.bukkit.plugin.TimePermissions;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.Material;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPhysicsEvent;

/**
 * TimeControls block listener
 * @author SuckyBlowfish
 */
public class TimePermissionsBlockListener extends BlockListener {
    private final TimePermissions plugin;

    public TimePermissionsBlockListener(TimePermissions plugin) {
        this.plugin = plugin;
    }
}
