package com.gmail.haloinverse.DynamicMarket;


import com.nijikokun.bukkit.Permissions.Permissions;
import com.nijikokun.bukkit.iConomy.iConomy;
import java.io.File;
import java.util.Timer;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
 
public class DynamicMarket extends JavaPlugin
{
/*  44 */   public final Logger log = Logger.getLogger("Minecraft");
 
/*  49 */   public String name; // = "SimpleMarket";
/*  50 */   public String codename = "Caribou";
/*  51 */   public String version; // = "0.4a";
 
/*  56 */   public iListen l = new iListen(this);
   			public static Permissions Permissions;
   			public static iProperty Settings;
/*  76 */   public static String directory; // = "SimpleMarket" + File.separator;
			public String shop_tag = "{BKT}[{}Shop{BKT}]{} ";
			public String currency;// = "Coin";
/*  78 */   public int max_per_purchase = 64;
			public int max_per_sale = 64;
 
			private String database_type = "sqlite";
/*  85 */   public static String sqlite = "jdbc:sqlite:" + directory + "shop.db";
/*  86 */   public static String mysql = "jdbc:mysql://localhost:3306/minecraft";
/*  87 */   public static String mysql_user = "root";
/*  88 */   public static String mysql_pass = "pass";
			public static String mysql_dbEngine = "MyISAM";
/*  89 */   public static Timer timer = null;
			public static String csvFileName;
			public static String csvFilePath;
/*  90 */   public iConomy iC = null;
			public Items items;
			private String itemsPath = "";
/* 100 */   public DatabaseMarket db = null;
			public boolean wrapperMode = false;
			public boolean wrapperPermissions = false;
			public boolean simplePermissions = false;
			public PermissionInterface permissionWrapper = null;
			//public static YamlPropFile yamlPropTest;
 
			
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
/* 126 */     	thisPluginManager.registerEvent(Event.Type.PLAYER_COMMAND, this.l, Event.Priority.Normal, this);
			}

			@Override
			public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
			{
				// Only triggered by console events, apparently...
				log.info(Messaging.bracketize(name) + " OnCommand called with: " + cmd.getName());
				if (!wrapperMode)
				{
					boolean thisReturn;
					thisReturn = this.l.onCommand(sender, cmd.getName(), commandLabel, args, "");
					log.info(Messaging.bracketize(name) + " Command returning: " + (thisReturn? "True" : "False"));
					return thisReturn;
				}
				else
					return true;
			}
			
			public boolean wrapperCommand(CommandSender sender, Command cmd, String commandLabel, String[] args, String shopLabel)
			{
				return this.l.onCommand(sender, cmd.getName(), commandLabel, args, (shopLabel == null ? "" : shopLabel));
			}
			
			public boolean wrapperCommand(CommandSender sender, String cmd, String commandLabel, String[] args, String shopLabel)
			{
				return this.l.onCommand(sender, cmd, commandLabel, args, (shopLabel == null ? "" : shopLabel));
			}
 
			public void setup()
			{

/* 131 */     Settings = new iProperty(getDataFolder() + File.separator + name + ".settings");

/* 132 */     //ItemsFile = new iProperty("items.db");
			  itemsPath = Settings.getString("items-db-path", getDataFolder() + File.separator);
			  items = new Items(itemsPath + "items.db", this);
 
/* 135 */     shop_tag = Settings.getString("shop-tag", shop_tag);
/* 136 */     max_per_purchase = Settings.getInt("max-items-per-purchase", 64);
/* 137 */     max_per_sale = Settings.getInt("max-items-per-sale", 64);
 
/* 140 */     this.database_type = Settings.getString("database-type", "sqlite");
 
/* 143 */     mysql = Settings.getString("mysql-db", mysql);
/* 144 */     mysql_user = Settings.getString("mysql-user", mysql_user);
/* 145 */     mysql_pass = Settings.getString("mysql-pass", mysql_pass);
			  mysql_dbEngine = Settings.getString("mysql-dbengine", mysql_dbEngine);
 
/* 148 */     if (this.database_type.equalsIgnoreCase("mysql"))
/* 149 */       db = new DatabaseMarket(DatabaseMarket.Type.MYSQL, "Market", items, mysql_dbEngine, this);
     		  else
/* 151 */       db = new DatabaseMarket(DatabaseMarket.Type.SQLITE, "Market", items, "", this);

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
/* 156 */     		Plugin test = getServer().getPluginManager().getPlugin("Permissions");
 
/* 158 */     		if (Permissions == null)
/* 159 */       		if (test != null) {
/* 160 */         			Permissions = (Permissions)test;
							log.info(Messaging.bracketize(name) + " Standard Permission plugin enabled.");
       					} else {
/* 162 */         			log.info(Messaging.bracketize(name) + " Permission system not enabled. Disabling plugin.");
/* 163 */         			getServer().getPluginManager().disablePlugin(this);
       					}	
				}
			}
 

 
			public void setupCurrency()
			{
/* 188 */     Plugin test = getServer().getPluginManager().getPlugin("iConomy");
 
/* 190 */     if (test != null) {
/* 191 */       	iC = (iConomy)test;
/* 192 */       	currency = iConomy.currency;
     		  } else {
/* 194 */       	log.info(Messaging.bracketize(name) + " iConomy is not loaded. Disabling plugin.");
/* 195 */       	getPluginLoader().disablePlugin(this);
     		  }
			}
}