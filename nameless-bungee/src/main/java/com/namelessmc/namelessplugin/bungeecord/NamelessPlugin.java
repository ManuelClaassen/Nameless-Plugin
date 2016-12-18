package com.namelessmc.namelessplugin.bungeecord;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import com.namelessmc.namelessplugin.bungeecord.commands.GetUserCommand;
import com.namelessmc.namelessplugin.bungeecord.commands.RegisterCommand;
import com.namelessmc.namelessplugin.bungeecord.commands.ReportCommand;
import com.namelessmc.namelessplugin.bungeecord.mcstats.Metrics;
import com.namelessmc.namelessplugin.bungeecord.player.PlayerEventListener;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

/*
 *  Bungeecord Version by IsS127
 */

public class NamelessPlugin extends Plugin {

	/*
	 *  API URL
	 */
	private String apiURL = "";
	public boolean hasSetUrl = true;

	/*
	 *  NamelessMC permission string.
	 */

	public final String permission = "namelessmc";
	public final String permissionAdmin = "namelessmc.admin";

	/*
	 *  Metrics
	 */
	Metrics metrics;

	/*
	 *  Configuration
	 */
	Configuration config;
	Configuration playerInfoFile;

	public Configuration getConfig(){
		return config;
	}

	/*
	 *  OnEnable method
	 */
	@Override
	public void onEnable(){
		// Initialise Files
		initConfig();
		initPlayerInfoFile();

		registerListeners();
	}

	/*
	 *  OnDisable method
	 */
	@Override
	public void onDisable(){
		unRegisterListeners();
	}

	/*
	 * Register Commands/Events
	 */
	public void registerListeners(){
		try {
            metrics = new Metrics(this);
            metrics.start();
            getLogger().info(ChatColor.translateAlternateColorCodes('&', "&3Metrics Started!"));
        } catch (IOException e) {
            e.printStackTrace();
        } 

		// Register commands
		getProxy().getPluginManager().registerCommand(this, new RegisterCommand(this, "register"));
		getProxy().getPluginManager().registerCommand(this, new GetUserCommand(this, "getuser"));
		if (config.getBoolean("enable-reports")) {
			getProxy().getPluginManager().registerCommand(this, new ReportCommand(this, "report"));
		}

		// Register events
		getProxy().getPluginManager().registerListener(this, new PlayerEventListener(this));
	}

	/*
	 * UnRegister Commands/Events
	 */
	public void unRegisterListeners(){
		// UnRegister commands
		getProxy().getPluginManager().unregisterCommand(new RegisterCommand(this, "register"));
		getProxy().getPluginManager().unregisterCommand(new GetUserCommand(this, "getuser"));
		if (config.getBoolean("enable-reports")) {
			getProxy().getPluginManager().unregisterCommand(new ReportCommand(this, "report"));
		}

		// UnRegister Listeners/Events
		getProxy().getPluginManager().unregisterListener(new PlayerEventListener(this));
	}

	/*
	 *  Initialise configuration
	 */
	private void initConfig(){
		// Check config exists, if not create one
		try {
			if(!getDataFolder().exists()){
				// Folder within plugins doesn't exist, create one now...
				getDataFolder().mkdirs();
			}

			File file = new File(getDataFolder(), "config.yml");

			if(!file.exists()){
				try (InputStream in = getResourceAsStream("config.yml")) {
					// Config doesn't exist, create one now...
					getLogger().info(ChatColor.translateAlternateColorCodes('&', "&1Creating NamelessMC configuration file..."));
                    Files.copy(in, file.toPath());

					getLogger().info(ChatColor.translateAlternateColorCodes('&', "&4NamelessMC needs configuring, disabling features..."));
					getLogger().info(ChatColor.translateAlternateColorCodes('&', "&4Please Configure NamelessMC config.yml!"));
					hasSetUrl = false;

                } catch (IOException e) {
                    e.printStackTrace();
                }

			} else {
				config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));

				// Exists already, load it
				getLogger().info(ChatColor.translateAlternateColorCodes('&', "&2Loading NamelessMC configuration file..."));

				apiURL = config.getString("api-url");

				if(apiURL.isEmpty()){
					// API URL not set
					getLogger().info(ChatColor.translateAlternateColorCodes('&', "&4No API URL set in the NamelessMC configuration, disabling features."));
					hasSetUrl = false;
				}
			}

		} catch(Exception e){
			// Exception generated
			e.printStackTrace();
		}
	}

	/*
	 *  Gets API URL
	 */
	public String getAPIUrl(){
		return apiURL;
	}

	/*
	 * Initialise the Player Info File
	 */
	private void initPlayerInfoFile() {
	    File iFile = new File(this.getDataFolder() + File.separator + "playersInformation.yml");
		if(!iFile.exists()){
			try {
				iFile.createNewFile();
				getLogger().info(ChatColor.translateAlternateColorCodes('&', "&2Created Players Information File."));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 *  Update username on Login
	 */
	public void userCheck(ProxiedPlayer player){
		// Check if user does NOT contain information in the Players Information file. 
		// If so, add him.
    	try {
			playerInfoFile = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "playersInformation.yml"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if(!playerInfoFile.contains(player.getUniqueId().toString())){
			getLogger().info(ChatColor.translateAlternateColorCodes('&',"&a" + player.getName() + " &cDoes not contain in the Player Information File.."));
			getLogger().info(ChatColor.translateAlternateColorCodes('&',"&2Adding&a" + player.getName() + " &2to the Player Information File."));
			playerInfoFile.set(player.getUniqueId().toString() + ".Username", player.getName());
			getLogger().info(ChatColor.translateAlternateColorCodes('&',"&2Added&a" + player.getName() + " &2to the Player Information File."));
		}

		// Check if user has changed Username
		// If so, change the username in the Players Information File. (NOT COMPLETED)
		// And change the username on the website.
		else if(playerInfoFile.getString(player.getUniqueId() + ".Username") !=  player.getName()){
			getLogger().info(ChatColor.translateAlternateColorCodes('&',"&cDetected that &a" + player.getName() + " &2has changed his/her username!"));
			getLogger().info(ChatColor.translateAlternateColorCodes('&',"&2Changing &a" + player.getName() + "s &2username."));

			String previousUsername = playerInfoFile.get(player.getUniqueId() + ".Username").toString();
			String newUsername = player.getName();
			playerInfoFile.set(player.getUniqueId() + ".PreviousUsername", previousUsername);
			playerInfoFile.set(player.getUniqueId() + ".Username", newUsername);
			getLogger().info(ChatColor.translateAlternateColorCodes('&',"&2Changed &a" + player.getName() + "s &2username in the Player Information File."));

			// Changing username on Website here.
			// Comming in a bit.
		}
			
	}

}