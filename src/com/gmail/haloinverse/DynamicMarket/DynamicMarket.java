package com.gmail.haloinverse.DynamicMarket;


import com.nijikokun.bukkit.Permissions.Permissions;
import com.nijikokun.bukkit.iConomy.iConomy;
import java.io.File;
import java.util.Timer;
import java.util.logging.Logger;
//import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
//import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
 
public class DynamicMarket extends JavaPlugin
{
	public final Logger log = Logger.getLogger("Minecraft");
 
	public String name; // = "SimpleMarket";
	public String codename = "Caribou";
	public String version; // = "0.4a";
 
	public iListen playerListener = new iListen(this);
	public DMServerListener serverListener = new DMServerListener(this);
	public static Permissions Permissions;
	public static iProperty Settings;
	public static String directory; // = "SimpleMarket" + File.separator;
	public String shop_tag = "{BKT}[{}Shop{BKT}]{} ";
	public String currency;// = "Coin";
	public int max_per_purchase = 64;
	public int max_per_sale = 64;
	public String defaultShopAccount = "";
	public boolean defaultShopAccountFree = true;
 
	private String database_type = "sqlite";
	public static String sqlite = "jdbc:sqlite:" + directory + "shop.db";
	public static String mysql = "jdbc:mysql://localhost:3306/minecraft";
	public static String mysql_user = "root";
	public static String mysql_pass = "pass";
	public static String mysql_dbEngine = "MyISAM";
	public static Timer timer = null;
	public static String csvFileName;
	public static String csvFilePath;
	public iConomy iC = null;
	public Items items;
	private String itemsPath = "";
	public DatabaseMarket db = null;
	public boolean wrapperMode = false;
	public boolean wrapperPermissions = false;
	public boolean simplePermissions = false;
	public PermissionInterface permissionWrapper = null;
	public TransactionLogger transLog = null;
	public String transLogFile = "transactions.log";
	public boolean transLogAutoFlush = false;
	//public static YamlPropFile yamlPropTest;
 
			
	// On newer builds of CraftBukkit, commenting out this constructor cause an InvalidPluginException on load.
	/*
	public DynamicMarket(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
		super(pluginLoader, instance, desc, folder, plugin, cLoader);

		// Moved to onEnable.
	//	folder.mkdir();

	//  	name = desc.getName();
	//  	version = desc.getVersion();

	//	directory = getDataFolder() + File.separator;
	//	sqlite = "jdbc:sqlite:" + directory + "shop.db";
	
		//registerEvents();
		log.info(Messaging.bracketize(name) + " version " + Messaging.bracketize(version) + " (" + codename + ") loaded");
	}
	*/
			
 
	public void onDisable() {
		log.info(Messaging.bracketize(name) + " version " + Messaging.bracketize(version) + " (" + codename + ") disabled");
	}
 
	@Override
	public void onEnable() {
		PluginDescriptionFile desc = getDescription();
		getDataFolder().mkdir();

		name = desc.getName();
	  	version = desc.getVersion();
 
	  	directory = getDataFolder() + File.separator;
	  	sqlite = "jdbc:sqlite:" + directory + "shop.db";

	  	registerEvents();
	  	setup();
	  	//setupItems(); //CHANGED: Initialisation moved to Items constructor.
	  	setupCurrency();
	  	setupPermissions();
	  	log.info(Messaging.bracketize(name) + " version " + Messaging.bracketize(version) + " (" + codename + ") enabled");
	}
 
	private void registerEvents()
	{
		PluginManager thisPluginManager = getServer().getPluginManager();
		//thisPluginManager.registerEvent(Event.Type.PLUGIN_ENABLE, this.l, Event.Priority.Normal, this);
		thisPluginManager.registerEvent(Event.Type.PLAYER_COMMAND, this.playerListener, Event.Priority.Normal, this);
		//thisPluginManager.registerEvent(Event.Type.SERVER_COMMAND, this.serverListener, Event.Priority.Normal, this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		//log.info(Messaging.bracketize(name) + " OnCommand called with: " + cmd.getName());
		if (!wrapperMode)
		{
			boolean thisReturn;
			thisReturn = this.playerListener.parseCommand(sender, cmd.getName(), args, "", defaultShopAccount, defaultShopAccountFree);
			//log.info(Messaging.bracketize(name) + " Command returning: " + (thisReturn? "True" : "False"));
			return thisReturn;
		}
		else
			return true;
	}
	
	public boolean wrapperCommand(CommandSender sender, String cmd, String[] args, String shopLabel, String accountName, boolean freeAccount)
	{
		return this.playerListener.parseCommand(sender, cmd, args, (shopLabel == null ? "" : shopLabel), accountName, freeAccount);
	}

	public boolean wrapperCommand(CommandSender sender, String cmd, String[] args, String shopLabel)
	{
		return wrapperCommand(sender, cmd, args, (shopLabel == null ? "" : shopLabel), defaultShopAccount, defaultShopAccountFree);
	}
	
	public boolean wrapperCommand(CommandSender sender, String cmd, String[] args)
	{
		return wrapperCommand(sender, cmd, args, "");
	}

	
	public void setup()
	{
		Settings = new iProperty(getDataFolder() + File.separator + name + ".settings");
	
	    //ItemsFile = new iProperty("items.db");
		itemsPath = Settings.getString("items-db-path", getDataFolder() + File.separator);
		items = new Items(itemsPath + "items.db", this);
	 
	    shop_tag = Settings.getString("shop-tag", shop_tag);
	    max_per_purchase = Settings.getInt("max-items-per-purchase", 64);
	    max_per_sale = Settings.getInt("max-items-per-sale", 64);
 
	    this.database_type = Settings.getString("database-type", "sqlite");
	 
	    mysql = Settings.getString("mysql-db", mysql);
	    mysql_user = Settings.getString("mysql-user", mysql_user);
	    mysql_pass = Settings.getString("mysql-pass", mysql_pass);
	    mysql_dbEngine = Settings.getString("mysql-dbengine", mysql_dbEngine);
	 
		if (this.database_type.equalsIgnoreCase("mysql"))
			db = new DatabaseMarket(DatabaseMarket.Type.MYSQL, "Market", items, mysql_dbEngine, this);
		else
			db = new DatabaseMarket(DatabaseMarket.Type.SQLITE, "Market", items, "", this);
	
		csvFileName = Settings.getString("csv-file", "shopDB.csv");
		csvFilePath = Settings.getString("csv-file-path", getDataFolder() + File.separator);
		wrapperMode = Settings.getBoolean("wrapper-mode", false);
		simplePermissions = Settings.getBoolean("simple-permissions", false);
		wrapperPermissions = Settings.getBoolean("wrapper-permissions", false);
		  
		Messaging.colNormal = "&" + Settings.getString("text-colour-normal", "e");
		Messaging.colCmd = "&" + Settings.getString("text-colour-command", "f");
		Messaging.colBracket = "&" + Settings.getString("text-colour-bracket", "d");
		Messaging.colParam = "&" + Settings.getString("text-colour-param", "b");
		Messaging.colError = "&" + Settings.getString("text-colour-error", "c");
		
		defaultShopAccount = Settings.getString("default-shop-account", "");
		defaultShopAccountFree = Settings.getBoolean("default-shop-account-free", defaultShopAccountFree);
		
		transLogFile = Settings.getString("transaction-log-file", transLogFile);
		transLogAutoFlush = Settings.getBoolean("transaction-log-autoflush", transLogAutoFlush);
		if ((transLogFile != null) && (!transLogFile.isEmpty()))
			transLog = new TransactionLogger(this, getDataFolder() + File.separator + transLogFile, transLogAutoFlush);
		else
			transLog = new TransactionLogger(this, null, false);
		
		//yamlPropTest = new YamlPropFile(getDataFolder() + File.separator + "SimpleMarket.yml");
		//yamlPropTest.load();
	}
 
	public void setupPermissions()
	{
		if (simplePermissions)
		{	
			Permissions = null;
			log.info(Messaging.bracketize(name) + " Simple permission system active.");
		}
		else if (wrapperPermissions)
			log.info(Messaging.bracketize(name) + " Permissions will be delegated to wrapper plugin.");
		else
		{
			Plugin test = getServer().getPluginManager().getPlugin("Permissions");
 
     		if (Permissions == null)
       		if (test != null) {
         			Permissions = (Permissions)test;
							log.info(Messaging.bracketize(name) + " Standard Permission plugin enabled.");
			} else {
				log.info(Messaging.bracketize(name) + " Permission system not enabled. Disabling plugin.");
				getServer().getPluginManager().disablePlugin(this);
			}	
		}
	}
 
	public void setupCurrency()
	{
		Plugin test = getServer().getPluginManager().getPlugin("iConomy");
 
		if (test != null) {
			iC = (iConomy)test;
			currency = iConomy.currency;
     	} else {
     		log.info(Messaging.bracketize(name) + " iConomy is not loaded. Disabling plugin.");
     		getPluginLoader().disablePlugin(this);
     	}
	}
}