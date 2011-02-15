package com.bukkit.SuckyBlowfish.TimePermissions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.yaml.snakeyaml.*;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;

import com.nijikokun.bukkit.Permissions.Permissions;
import com.nijiko.permissions.PermissionHandler;
import org.bukkit.plugin.Plugin;

/**
 * TimeControls for Bukkit
 *
 * @author SuckyBlowfish
 */
public class TimePermissions extends JavaPlugin {
    private final TimePermissionsPlayerListener playerListener = new TimePermissionsPlayerListener(this);
    private final TimePermissionsBlockListener blockListener = new TimePermissionsBlockListener(this);
    private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
    public HashMap<String, Integer> playerConfigTime = new HashMap<String, Integer>();
    public HashMap<String, Object> configData = new HashMap<String, Object>();
    public HashMap<Integer,HashMap<String,Object>> configItems = new HashMap<Integer,HashMap<String,Object>>();
    public HashMap<String, String> configGroups = new HashMap<String, String>();
    public static PermissionHandler Permissions = null;
    private final File configFile;
    private final File dataFile;
    private final Yaml yaml;
    private Timer timeUpdateTimer;
    private Timer dataSaveTimer;
    public long initialTime = System.currentTimeMillis();

    public TimePermissions(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
        super(pluginLoader, instance, desc, folder, plugin, cLoader);
        // TODO: Place any custom initialisation code here

        if (!folder.exists()){
        	folder.mkdir();
        }
        this.configFile=new File(folder.getAbsolutePath(),"config.yml");
        this.dataFile=new File(folder.getAbsolutePath(),"data.yml");
        
    	try {
    		if (!this.configFile.exists()){
    			this.configFile.createNewFile();
    		}
    		if (!this.dataFile.exists()){
    			this.dataFile.createNewFile();
    		}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
        }
		
		DumperOptions options = new DumperOptions();
		options.setDefaultFlowStyle(FlowStyle.BLOCK);
		this.yaml = new Yaml(options);
		
		
    }
    
    public void setupPermissions() {
    	Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");


    	if(this.Permissions == null) {
    	    if(test != null) {
    		this.Permissions = ((Permissions)test).getHandler();
    	    } else {
    		this.getServer().getPluginManager().disablePlugin(this);
    	    }
    	}
    }

   

    public void onEnable() {
        // TODO: Place any custom enable code here including the registration of any events
    	try {
			this.configData=(HashMap<String, Object>) yaml.load(new FileInputStream(this.configFile));
			this.configItems=(HashMap<Integer,HashMap<String,Object>>)configData.get("Items");
			for (Integer key : configItems.keySet()){
				configItems.get(key).put("Time", parseTime((String) configItems.get(key).get("Time")));
			}
			this.configGroups=(HashMap)configData.get("Groups");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("fail");
		}
		try {
			this.playerConfigTime=(HashMap<String, Integer>) yaml.load(new FileInputStream(this.dataFile));
			if (playerConfigTime==null)playerConfigTime=new HashMap<String, Integer>();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.playerConfigTime=new HashMap<String, Integer>();
		}	
		
		
		setupPermissions();
		long writeDelay = parseTime((String) configData.get("WriteDelay"));
		
		timeUpdateTimer = new Timer();
		timeUpdateTimer.scheduleAtFixedRate(new timeUpdateTask(),1000,1000);
		
		dataSaveTimer = new Timer();
		dataSaveTimer.scheduleAtFixedRate(new dataSaveTask(),writeDelay,writeDelay);
		
        // Register our events
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvent(Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_QUIT, playerListener, Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_ITEM, playerListener, Priority.Normal, this);

        // EXAMPLE: Custom code, here we just output some info so we can check all is well
        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
        for(Player player: getServer().getOnlinePlayers()){
        	System.out.println(player.getName());
        }
    }
    public void onDisable() {
    	savePlayerTimes();   	
    	timeUpdateTimer.cancel(); 	
    	dataSaveTimer.cancel();
    }
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        String[] trimmedArgs = args;
        String commandName = command.getName().toLowerCase();
        
        if (commandName.equals("ptime")){
        	if (trimmedArgs.length==0){
        		if(sender instanceof Player){
	        		int time = playerConfigTime.get(((Player)sender).getName());
	        		sender.sendMessage(ChatColor.GREEN+"You have played for "+secondsToString(time));
	        		return true;
        		}else{
            		sender.sendMessage("Good one, but the you don't have a play-time!");
            		return false;
            	}
        	}else if (trimmedArgs.length==1){
        		if (trimmedArgs[0].equals("unlocks")){
            		//Display unlocks
        		}else if (trimmedArgs[0].equals("top")){
        			//Display top 5
        		}
        	}else if (trimmedArgs.length==2){
        		if (trimmedArgs[0].equals("top")){
        			//Display top #
        		}else if(trimmedArgs[0].equals("check")){
        			//Check player's ptime
        		}
        	}else if (trimmedArgs.length==3){
        		if (trimmedArgs[0].equals("set")){
        			//Set player's ptime
        		}
        	}
        }
        return false;
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
    	return (weeks  <1 ? "" : (weeks<2   ? weeks  +" week, "   : weeks  +" weeks, "  ) )+
    		   (days   <1 ? "" : (days<2    ? days   +" day, "    : days   +" days, "   ) )+
    	       (hours  <1 ? "" : (hours<2   ? hours  +" hour, "   : hours  +" hours, "  ) )+
    	       (minutes<1 ? "" : (minutes<2 ? minutes+" minute, " : minutes+" minutes, ") )+
    	"and "+(seconds<1 ? "" : (seconds<2 ? seconds+" second!"  : seconds+" seconds." ) );
    }
    private Player matchPlayer(String playerName, CommandSender sender) {
        Player player;
        List<Player> players = getServer().matchPlayer(playerName);
        if (players.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "Unknown player");
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
    	ArrayList<String> flit = new ArrayList();
    	for (String s : split){
    		
    	}
    	if (split[1].equalsIgnoreCase("second")||split[1].equalsIgnoreCase("seconds")){
    		return Integer.parseInt(split[0])*1000;
    	}else if (split[1].equalsIgnoreCase("minute")||split[1].equalsIgnoreCase("minutes")){
    		return Integer.parseInt(split[0])*1000*60;
    	}else if (split[1].equalsIgnoreCase("hour")||split[1].equalsIgnoreCase("hours")){
    		return Integer.parseInt(split[0])*1000*60*60;
    	}else if (split[1].equalsIgnoreCase("day")||split[1].equalsIgnoreCase("days")){
    		return Integer.parseInt(split[0])*1000*60*60*24;
    	}
    	return null;
    }
    public boolean playerCanUseItem(Player player,Material itemType){
    	if (configItems.containsKey(itemType.getId())){
    		return playerConfigTime.get(player.getName()) > (Integer)configItems.get(itemType.getId()).get("Time")/1000;    
    	}else{
    		return true;
    	}
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
    public void savePlayerTimes(){
    	try {
			FileWriter dataWrite = new FileWriter(dataFile);
			yaml.dump(playerConfigTime,dataWrite);
			System.out.println(yaml.dump(playerConfigTime));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    public void refreshPlayerTimes(){
    	Player[] players = getServer().getOnlinePlayers();
    	for(int i = 0; i < players.length; i++){
    		playerConfigTime.put(players[i].getName(),playerConfigTime.get(players[i].getName())+1);    		
    	}
    }
    class timeUpdateTask extends TimerTask{
    	public void run(){
    		refreshPlayerTimes();
    	}
    }
    class dataSaveTask extends TimerTask{
    	public void run(){
    		savePlayerTimes();
    	}
    }
}

