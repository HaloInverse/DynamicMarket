package com.gmail.haloinverse.DynamicMarket;


import com.nijikokun.bukkit.Permissions.Permissions;
import com.nijikokun.bukkit.iConomy.iConomy;
//import com.haloinverse.AnyConomy.AnyConomy;
import java.io.File;
import java.util.Timer;
import java.util.logging.Logger;
//import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.server.PluginEvent;
import org.bukkit.event.server.ServerListener;
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
	//public DMServerListener serverListener = new DMServerListener(this);
	public static Permissions Permissions;
	public static iProperty Settings;
	public static String directory; // = "DynamicMarket" + File.separator;
	public String shop_tag = "{BKT}[{}Shop{BKT}]{} ";
	protected String currency;// = "Coin";
	protected int max_per_purchase = 64;
	protected int max_per_sale = 64;
	public String defaultShopAccount = "";
	public boolean defaultShopAccountFree = true;
 
	protected String database_type = "sqlite";
	protected static String sqlite = "jdbc:sqlite:" + directory + "shop.db";
	protected static String mysql = "jdbc:mysql://localhost:3306/minecraft";
	protected static String mysql_user = "root";
	protected static String mysql_pass = "pass";
	protected static String mysql_dbEngine = "MyISAM";
	protected static Timer timer = null;
	protected static String csvFileName;
	protected static String csvFilePath;
	protected iConomy iC = null;
	//protected AnyConomy anyConomy = null;
	protected boolean econLoaded = false;
	protected EconType econType = EconType.NONE;
	protected Items items;
	protected String itemsPath = "";
	protected DatabaseMarket db = null;
	protected boolean wrapperMode = false;
	protected boolean wrapperPermissions = false;
	protected boolean simplePermissions = false;
	protected PermissionInterface permissionWrapper = null;
	protected TransactionLogger transLog = null;
	protected String transLogFile = "transactions.log";
	protected boolean transLogAutoFlush = true;
	private MyServerListener myServerListener = new MyServerListener(this); 
	//public static YamlPropFile yamlPropTest;
 
	private class MyServerListener extends ServerListener {
	
		private DynamicMarket plugin;
		public MyServerListener(DynamicMarket thisPlugin)
		{
			this.plugin = thisPlugin;
		}
	
		@Override
		public void onPluginEnabled(PluginEvent event)
		{
			// This is called for plugins enabled AFTER this one.
			String thisPluginName = event.getPlugin().getDescription().getName();
			if(thisPluginName.equals("iConomy"))
			{
				plugin.connectEconomy(event.getPlugin());
			}
		}
	}
 
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

	  	setup();
	  	//setupItems(); //CHANGED: Initialisation moved to Items constructor.
	  	setupEconomy();
	  	setupPermissions();
	  	registerEvents();
	  	log.info(Messaging.bracketize(name) + " version " + Messaging.bracketize(version) + " (" + codename + ") enabled");
	}
 
	private void registerEvents()
	{
		PluginManager thisPluginManager = getServer().getPluginManager();
		thisPluginManager.registerEvent(Event.Type.PLAYER_COMMAND, this.playerListener, Event.Priority.Normal, this);
		thisPluginManager.registerEvent(Event.Type.PLUGIN_ENABLE, this.myServerListener, Event.Priority.Monitor, this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		if (!wrapperMode)
		{
			boolean thisReturn;
			thisReturn = this.playerListener.parseCommand(sender, cmd.getName(), args, "", defaultShopAccount, defaultShopAccountFree);
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
		
		String econTypeString = Settings.getString("economy-plugin", "iconomy3");
		if (econTypeString.equalsIgnoreCase("iconomy3"))
			econType = EconType.ICONOMY3;
		else if (econTypeString.equalsIgnoreCase("anyconomy"))
			econType = EconType.ANYCONOMY;
		else
		{	
			log.severe(Messaging.bracketize(name) + " Invalid economy setting for 'economy-plugin='.");
			econType = EconType.NONE;
		}
		
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
 
	public void setupEconomy()
	{
		// Called with iConomy 2.1(Cookies wrapper).
		// This catches plugins enabled BEFORE this plugin.
		Plugin test = getServer().getPluginManager().getPlugin("iConomy");
		if (test != null) {
			connectEconomy(test);
     	}
		
		test = getServer().getPluginManager().getPlugin("AnyConomy");
		if (test != null) {
			connectEconomy(test);
     	}	
     		
	}
	
	public void connectEconomy(Plugin thisPlugin)
	{
		if(econType == EconType.ICONOMY3)
		{
			if(thisPlugin instanceof iConomy)
				connectToiConomy((iConomy)thisPlugin);
		}
		
		/*
		else if(econType == EconType.ANYCONOMY)
		{
			if(thisPlugin instanceof AnyConomy)
				connectToAnyConomy((AnyConomy)thisPlugin);
		}
		*/
		
	}
	
	/*
	public void connectToAnyConomy(AnyConomy thisPlugin)
	{
		anyConomy = thisPlugin;
		currency = "";
		econLoaded = true;
		log.info(Messaging.bracketize(name) + " AnyConomy connected.");
	}
	*/
	
	
	public void connectToiConomy(iConomy thisPlugin)
	{
		iC = thisPlugin;
		currency = iConomy.currency;
		econLoaded = true;
		log.info(Messaging.bracketize(name) + " iConomy connected.");
	}
	
	public static enum EconType
	{
		NONE, ICONOMY2, ICONOMY3, ANYCONOMY;
	}
}