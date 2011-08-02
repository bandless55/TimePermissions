package com.SuckyBlowfish.bukkit.plugin.TimePermissions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class TimeHolder {
	private final TimePermissions plugin;
	private ArrayList<String> mPlayerList = new ArrayList<String>();
	private ArrayList<Integer> mTimeList = new ArrayList<Integer>();
//	private ArrayList<ArrayList<Integer>> mTimeList = new ArrayList<ArrayList<Integer>>();
	private ArrayList<String> mWorldList = new ArrayList<String>();
	private final Map<Integer,String> mAnnouncements = new HashMap<Integer,String>(){{
		put(0,      ChatColor.GREEN + "Please welcome our newest member %1$ to the server!");
		put(3600,   ChatColor.GREEN + "%1$ has played for 1 hour!");
		put(7200,   ChatColor.GREEN + "%1$ has played for 2 hours!");
		put(18000,  ChatColor.GREEN + "%1$ has played for 5 hours!");
		put(25200,  ChatColor.GREEN + "%1$ has played for 7 hours!");
		put(36000,  ChatColor.GREEN + "%1$ has played for 10 hours!");
		put(46800,  ChatColor.GREEN + "%1$ has played for 13 hours!");
		put(86400,  ChatColor.GREEN + "%1$ has played for 1 day!");
		put(172800, ChatColor.GREEN + "%1$ has played for 2 days!");
		put(259200, ChatColor.GREEN + "%1$ has played for 3 days!");
	}};
	private Timer updateTimer;
	private Timer saveTimer;
	private File dataSaveFile = null;
	
	public TimeHolder(TimePermissions plugin){
		this.plugin = plugin;
	}
	
	public void start(){
		updateTimer=new Timer("UpdateTimer");
		updateTimer.scheduleAtFixedRate(new updateTask(),1000,1000);
		saveTimer=new Timer("SaveTimer");
		saveTimer.scheduleAtFixedRate(new saveTask(),600000,60000);
	}
	
	public void stop(){
		updateTimer.cancel();
		saveTimer.cancel();
	}
	
	public void load(File data){
		dataSaveFile = data;
		Scanner dataScanner = null;
		try {
			FileReader dataStream = new FileReader(dataSaveFile);
			dataScanner = new Scanner(dataStream);
	        while (dataScanner.hasNextLine()){
	            String[] split = dataScanner.nextLine().split(":");
	            if (split.length > 0){
	            	if (split[0].startsWith("@"))continue;
		            if (split.length==2){
		            	int time;
		            	try {
		            		time = Integer.parseInt(split[1]);
		            	} catch (NumberFormatException e){
		            		time = 0;
		            	}
		            	
		            	addPlayer(split[0], Integer.parseInt(split[1]));
		            }
	            }
	        }
		} catch (FileNotFoundException e) {
			System.out.print("[TimePermissions] Data file not found");
			e.printStackTrace();
		} finally{
			dataScanner.close();
		}
	}	
	
	public void save(){
		if (dataSaveFile!=null){
			String dataString = "";
			dataString+="@Version:v0.01";
			for (int i=0;i<mPlayerList.size();i++){
				dataString+="\n"+mPlayerList.get(i)+":"+mTimeList.get(i);
			}
			Writer out = null;
			try{
				out = new OutputStreamWriter(new FileOutputStream(dataSaveFile));
				out.write(dataString);
				out.close();
				System.out.print("[TimePermissions] All player times saved to disk!");
		    } catch (IOException e) {
				e.printStackTrace();
				System.out.print("[TimePermissions] Error with saving :@");
			} finally {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}else{
			System.out.print("[TimePermissions] Tried to save before data was loaded!");
		}
	}
	
	private class updateTask extends TimerTask{
		public void run(){
			increaseTimes(plugin.getServer().getOnlinePlayers());
		}
	}
	
	private class saveTask extends TimerTask{
		public void run(){
			save();
		}
	}
	
	private void addPlayer(Player player){
		String name = player.getName();
		addPlayer(name);
	}
	
	private void addPlayer(String name){
		if (!mPlayerList.contains(name)){
			addPlayer(name,0);
		}
	}
	
	public void addPlayer(Player player, int time){
		String name = player.getName();
		addPlayer(name,time);
	}
	
	public void addPlayer(String name, int time){
		mPlayerList.add(name);
		mTimeList.add(time);
	}
	
	private void increaseTimes(Player[] players){
		for(Player player: players){
			String name = player.getName();
			int i = mPlayerList.indexOf(name);
			if (i==-1){
				addPlayer(name);
				i=mPlayerList.indexOf(name);
			}
			int time = mTimeList.get(i);
			if (mAnnouncements.containsKey(time))announceTime(name,time);
			mTimeList.set(i, time+1);
		}
	}
	
	public int getTime(Player player){
		String name = player.getName();
		return getTime(name);
	}
	
	public Integer getTime(String name){
		if (mPlayerList.contains(name)){
			return mTimeList.get(mPlayerList.indexOf(name));
		}
		else{
			return null;
		}
	}
	
	public void announceTime(String name, int time){
		if (mAnnouncements.containsKey(time)){
			plugin.getServer().broadcastMessage(String.format(mAnnouncements.get(time), name));
		}
	}
	
	public String[] topTime(Integer top){
		if (top==null)top=5;
		ArrayList<Integer> rTimeList = mTimeList;
		Collections.sort(rTimeList);
		top=Math.min(top, rTimeList.size());
		String[] topPlayerArray = new String[top];
		for (int i=0;i<5;i++){
			topPlayerArray[i]=mPlayerList.get(mTimeList.indexOf(rTimeList.get(i)));
		}
		return topPlayerArray;
	}
}
