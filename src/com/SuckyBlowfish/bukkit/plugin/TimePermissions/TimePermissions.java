package com.SuckyBlowfish.bukkit.plugin.TimePermissions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.imageio.stream.FileImageInputStream;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Type;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

//import org.yaml.snakeyaml.*;
//import org.yaml.snakeyaml.DumperOptions.FlowStyle;

//import com.nijikokun.bukkit.Permissions.Permissions;
//import com.nijiko.permissions.PermissionHandler;
import org.bukkit.plugin.Plugin;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;

/**
 * TimeControls for Bukkit
 *
 * @author SuckyBlowfish
 */
public class TimePermissions extends JavaPlugin {
    private final TimePermissionsPlayerListener playerListener = new TimePermissionsPlayerListener(this);
    private final TimePermissionsBlockListener blockListener = new TimePermissionsBlockListener(this);
    private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
    
    //public HashMap<String, Integer> playerConfigTime = new HashMap<String, Integer>();
    public HashMap<Integer,HashMap> configItems = new HashMap<Integer,HashMap>();
    public HashMap<Integer, String> configGroups = new HashMap<Integer, String>();
    
    public int writeDelay;
    public TimeHolder timeHolder = new TimeHolder(this);
    
    public File settingsFile;
    public File playerDataFile;
    private Timer timeUpdateTimer;
    private Timer dataSaveTimer;
    public long initialTime = System.currentTimeMillis();
    Yaml yaml = null;

    public void onEnable() {
    	DumperOptions options = new DumperOptions();
        options.setWidth(50);
        options.setIndent(4);
        options.setDefaultFlowStyle(FlowStyle.BLOCK);
        yaml = new Yaml(options);
        
    	loadData();
    	
		timeHolder.start();
		
        // Register our events
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvent(Type.PLAYER_JOIN, playerListener, Priority.Lowest, this);
        pm.registerEvent(Type.PLAYER_QUIT, playerListener, Priority.Lowest, this);
        pm.registerEvent(Type.PLAYER_INTERACT, playerListener, Priority.Normal, this);
        
        // EXAMPLE: Custom code, here we just output some info so we can check all is well
        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
    }
    public void loadData(){
    	File folder = this.getDataFolder(); 
  	  	if (!folder.exists()){
        	folder.mkdir();
        }
        
        // Settings config file
        settingsFile=new File(folder.getAbsolutePath(),"config.yml");
        // Player data file
        playerDataFile=new File(folder.getAbsolutePath(),"data");
        
    	try {
    		if (!this.settingsFile.exists()){
    			this.settingsFile.createNewFile();
    		}
    		if (!this.playerDataFile.exists()){
    			this.playerDataFile.createNewFile();
    		}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		timeHolder.load(playerDataFile);
		
    }
    public void onDisable() {  	
    	timeHolder.stop();
    	timeHolder.save();
    }
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        String commandName = command.getName().toLowerCase();
        
        if (commandName.equals("ptime")){
        	
        	if (args.length==0){//No args, they want to check their own time
        		if(sender instanceof Player){
        			int time = timeHolder.getTime((Player) sender);
	        		sender.sendMessage(ChatColor.GREEN+"You have played for "+secondsToString(time));
        		}else{
            		sender.sendMessage(ChatColor.RED+"Good one, but the you don't have a play-time!");
            	}
        	}else{//There are args!
        		if (args[0].equals("top")){
        			sender.sendMessage(ChatColor.DARK_PURPLE+"Doesn't work yet kthxbai!");
        			//TODO: Also sort by world
//	    			if (args.length==1){
//		    			sender.sendMessage(ChatColor.DARK_PURPLE+"### TOP 5 PLAYTIMES ###");
//	    				for(String player : timeHolder.topTime(5)){
//	    					sender.sendMessage(ChatColor.GREEN+player+": "+secondsToString(timeHolder.getTime(player)));
//	    				}
//	    			}else if (args.length==2){
//	    				int top = Integer.getInteger(args[1]);
//	    				sender.sendMessage(ChatColor.DARK_PURPLE+"### TOP "+top+" PLAYTIMES ###");
//	    				for(String player : timeHolder.topTime(top)){
//	    					sender.sendMessage(ChatColor.GREEN+player+": "+secondsToString(timeHolder.getTime(player)));
//	    				}
//	    			}else if (args.length>2){
//	    				sender.sendMessage(ChatColor.RED+"Too many arguments.");
//	    			}
	    		}else if (args[0].equals("save")){
	    			timeHolder.save();
	    			sender.sendMessage(ChatColor.GREEN+"All player times saved to disk!");
	    		}else if(args[0].equals("check")||args[0].equals("c")||args[0].equals("get")){
	    			if (args.length==1){
	    				sender.sendMessage(ChatColor.RED+"Too few arguments.");
	    			}else if (args.length==2){
	    				Player victim = matchPlayer(args[1],sender);
	    				if (victim != null){
	    					sender.sendMessage(ChatColor.GREEN+victim.getDisplayName()+" has played for "+
	    							secondsToStringTruncated(timeHolder.getTime(victim)));
	    				} else {
		    				Integer time = timeHolder.getTime(args[1]);
	    					if (time != null){
	    						sender.sendMessage(ChatColor.GREEN+args[1]+" has played for "+
			    						secondsToStringTruncated(time));
	    					} else {
	    						sender.sendMessage(ChatColor.RED+"Player not found.");
	    					}
	    				}
	    			}else if (args.length>2){
	    				sender.sendMessage(ChatColor.RED+"Too many arguments.");
	    			}
	    		}
        	}
        }
        return true;
    }

    public String secondsToString(int time){
    	int weeks = time / 604800;
    	int r = time % 604800;
    	int days = r / 86400;
    	r = r % 86400;
    	int hours = r / 3600;
    	r = r % 3600;
    	int minutes = r / 60;
    	r = r % 60;
    	int seconds = r;
    	
//    		   (var    <1 ? "" : (var<2     ? var    +" var "     : var    +" vars "    ) )
    	return (weeks  <1 ? "" : (weeks<2   ? weeks  +" week"   : weeks  +" weeks"  ) )+
    		   (days   <1 ? "" : ((weeks>0)?", ":"")+(days<2    ? days   +" day"    : days   +" days"   ) )+
    	       (hours  <1 ? "" : ((weeks>0|days>0)?", ":"")+(hours<2   ? hours  +" hour"   : hours  +" hours"  ) )+
    	       (minutes<1 ? "" : ((weeks>0|days>0|hours>0)?", ":"")+(minutes<2 ? minutes+" minute" : minutes+" minutes") )+
    	       (seconds<1 ? "" : (seconds<2 ? ((weeks>0|days>0|hours>0|minutes>0)?" and ":"")+seconds+" second"  : ((weeks>0|days>0|hours>0|minutes>0)?" and ":"")+seconds+" seconds" ) )+".";
    }
    
    public String secondsToStringTruncated(int time){
    	int weeks = time / 604800;
    	int r = time % 604800;
    	int days = r / 86400;
    	r = r % 86400;
    	int hours = r / 3600;
    	r = r % 3600;
    	int minutes = r / 60;
    	r = r % 60;
    	int seconds = r;
    	
//    		   (var    <1 ? "" : (var<2     ? var    +" var "     : var    +" vars "    ) )
    	return (weeks>1 || days>1 || hours>1)
    		   ?   (weeks  <1 ? "" : (weeks<2   ? weeks  +" week"   : weeks  +" weeks"  ) )+
    			   (days   <1 ? "" : ((weeks>0)?", ":"")+(days<2    ? days   +" day"    : days   +" days"   ) )+
    	           (hours  <1 ? "" : ((weeks>0|days>0)?", and ":"")+(hours<2   ? hours  +" hour"   : hours  +" hours"  ) )
    	           
    	       :   (minutes<1 ? "not long." : ((weeks>0|days>0|hours>0)?", ":"")+(minutes<2 ? minutes+" minute" : minutes+" minutes") )+".";
    }
    private Player matchPlayer(String playerName, CommandSender sender) {
        Player player;
        List<Player> players = getServer().matchPlayer(playerName);
        if (players.isEmpty()) {
            player = null;
        } else {
            player = players.get(0);
        }
        return player;
    }
    public Integer parseTime(String time){
    	//4-Minutes
    	//6-Hours
    	//1-Day
    	String[] split = time.split(",");
    	int parsedTime=0;
    	for (String s : split){
    		String[] flit=s.split("-");
    		if (flit[1].equalsIgnoreCase("second")||flit[1].equalsIgnoreCase("seconds")){
    			parsedTime += Integer.parseInt(flit[0])*1000;
        	}else if (flit[1].equalsIgnoreCase("minute")||flit[1].equalsIgnoreCase("minutes")){
        		parsedTime += Integer.parseInt(flit[0])*1000*60;
        	}else if (flit[1].equalsIgnoreCase("hour")||flit[1].equalsIgnoreCase("hours")){
        		parsedTime += Integer.parseInt(flit[0])*1000*60*60;
        	}else if (flit[1].equalsIgnoreCase("day")||flit[1].equalsIgnoreCase("days")){
        		parsedTime += Integer.parseInt(flit[0])*1000*60*60*24;
        	}
    	}
    	
    	return parsedTime;
    }
    public boolean isDebugging(final Player player) {
        if (debugees.containsKey(player)) {
            return debugees.get(player);
        } else {
            return false;
        }
    }
    public void setDebugging(final Player player, final boolean value) {
        debugees.put(player, value);
    }
}

