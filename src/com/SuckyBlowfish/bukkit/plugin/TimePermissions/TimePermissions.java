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

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

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
    public HashMap<String, Integer> playerConfigLastOnline = new HashMap<String, Integer>();
    public HashMap<Integer,HashMap> configItems = new HashMap<Integer,HashMap>();
    public HashMap<Integer, String> configGroups = new HashMap<Integer, String>();
    public static PermissionHandler Permissions = null;
    private File settingsFile;
    private File dataFile;
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
        // TODO: Place any custom enable code here including the registration of any events
    	File folder = this.getDataFolder(); 
    	  if (!folder.exists()){
          	folder.mkdir();
          }
          
          // Settings config file
          this.settingsFile=new File(folder.getAbsolutePath(),"config.yml");
          // Player data file
          this.dataFile=new File(folder.getAbsolutePath(),"data.yml");
          
      	try {
      		if (!this.settingsFile.exists()){
      			this.settingsFile.createNewFile();
      		}
      		if (!this.dataFile.exists()){
      			this.dataFile.createNewFile();
      		}
  		} catch (IOException e) {
  			e.printStackTrace();
          }	
  		
    	Map<String, Object> settings = null;
    	Map<String, Map> times = null;
		try {
			settings = (Map<String, Object>)yaml.load(new FileInputStream(settingsFile));
			times = (Map<String,Map>)yaml.load(new FileInputStream(dataFile));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (Entry<Integer, Map> m : ((Map<Integer,Map>)settings.get("Items")).entrySet()){
			HashMap nodeConfig = new HashMap();
			
			nodeConfig.put("time", parseTime(((Map<String,String>)m.getValue()).get("Time")));
			nodeConfig.put("name", ((Map<String,String>)m.getValue()).get("Name"));
			
			configItems.put(m.getKey(), nodeConfig);
		}
		
		for (Entry<String, Map> m : ((Map<String,Map>)settings.get("Groups")).entrySet()){
			HashMap nodeConfig = new HashMap();
			
			configGroups.put(parseTime(((Map<String,String>)m.getValue()).get("Time")), m.getKey());
		}
		System.out.println(times);
		playerConfigTime = (HashMap<String, Integer>) times.get("Times");
		playerConfigLastOnline = (HashMap<String, Integer>) times.get("LastOnline");
		System.out.println(playerConfigTime);
		
		long writeDelay = parseTime((String)((Map<String,Object>) settings).get("WriteDelay"));
		
		//setupPermissions();
		
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
            		sender.sendMessage(ChatColor.RED+"Good one, but the you don't have a play-time!");
            		return true;
            	}
        	}else if (trimmedArgs.length==1){
        		if (trimmedArgs[0].equals("unlocks")){
            		//Display unlocks
        		}else if (trimmedArgs[0].equals("top")){
        			//Display top 5
        		}else if (trimmedArgs[0].equals("save")){
        			savePlayerTimes();
        			sender.sendMessage(ChatColor.GREEN+"Saved all play-times to disk! "+
        					ChatColor.DARK_AQUA+"I like waffles?");
        			return true;
        		}
        	}else if (trimmedArgs.length==2){
        		if (trimmedArgs[0].equals("top")){
        			//Display top #
        		}else if(trimmedArgs[0].equals("check")){
        			//Check player's ptime
    				if (playerConfigTime.containsKey(trimmedArgs[1])){
    					sender.sendMessage(ChatColor.GREEN+
    							trimmedArgs[1]+
    							" has played for "+
    							secondsToString(playerConfigTime.get(trimmedArgs[1])));
    					sender.sendMessage(ChatColor.GREEN+"This player "+
    							(Arrays.asList(getServer().getOnlinePlayers()).contains(getServer().getPlayer(trimmedArgs[1]))
    								? "is currently online!" 
    								: "was last online "+
    								secondsToStringTruncated((Integer)( ((Long)(System.currentTimeMillis()/1000)).intValue() -
    										(playerConfigLastOnline.get(trimmedArgs[1])) ))+
    								" ago."));
    					return true;
    				}else{
    					sender.sendMessage(ChatColor.RED+"Specified player does not exist.");
    					return true;
    				}

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
    	return (weeks  <1 ? "" : (weeks<2   ? weeks  +" week"   : weeks  +" weeks"  ) )+
    		   (days   <1 ? "" : ((weeks>0)?", ":"")+(days<2    ? days   +" day"    : days   +" days"   ) )+
    	       (hours  <1 ? "" : ((weeks>0|days>0)?", ":"")+(hours<2   ? hours  +" hour"   : hours  +" hours"  ) )+
    	       (minutes<1 ? "" : ((weeks>0|days>0|hours>0)?", ":"")+(minutes<2 ? minutes+" minute" : minutes+" minutes") )+
    	       (seconds<1 ? "" : (seconds<2 ? ((weeks>0|days>0|hours>0|minutes>0)?", and ":"")+seconds+" second!"  : ((weeks>0|days>0|hours>0|minutes>0)?", and ":"")+seconds+" seconds." ) );
    }
    public void setupPermissions() {
    	Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");
    	PluginDescriptionFile pdfFile = this.getDescription();
    		
    	if (this.Permissions == null) {
    		if (test!= null) {
    			this.getServer().getPluginManager().enablePlugin(test);
    			this.Permissions = ((Permissions) test).getHandler();
    		}
    		else {
    			getServer().getLogger().info(pdfFile.getName() + " version " + pdfFile.getVersion() + "not enabled. Permissions not detected");
    			this.getServer().getPluginManager().disablePlugin(this);
    		}
    	}
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
    	           
    	       :   (minutes<1 ? "not long" : ((weeks>0|days>0|hours>0)?", ":"")+(minutes<2 ? minutes+" minute" : minutes+" minutes") );
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
    public boolean playerCanUseItem(Player player,int materialId){
    	if (configItems.containsKey(materialId)){
    		if (playerConfigTime.get(player.getName()) > (Integer)(configItems.get(materialId).get("time"))/1000){
    			return true;
    		}else{
    			player.sendMessage("To use "+
    				(String)(configItems.get(materialId).get("name"))+
    				" you must have at least "+
    				secondsToString((Integer)(configItems.get(materialId).get("time"))/1000)+".");
    			return false;
    		}
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
    	Integer now = ((Long)(System.currentTimeMillis()/1000)).intValue();
    	for (Player p : getServer().getOnlinePlayers()){
    		playerConfigLastOnline.put(p.getName(), now);
    	}
    	Map<String,Map> map = new HashMap<String,Map>();
    	map.put("Times", playerConfigTime);
    	map.put("LastOnline", playerConfigLastOnline);
		try {
			yaml.dump(map, new FileWriter(dataFile));
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

