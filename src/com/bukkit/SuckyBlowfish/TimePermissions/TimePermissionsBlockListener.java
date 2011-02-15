package com.bukkit.SuckyBlowfish.TimePermissions;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.Material;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockRightClickEvent;

/**
 * TimeControls block listener
 * @author SuckyBlowfish
 */
public class TimePermissionsBlockListener extends BlockListener {
    private final TimePermissions plugin;

    public TimePermissionsBlockListener(final TimePermissions plugin) {
        this.plugin = plugin;
    }
}
