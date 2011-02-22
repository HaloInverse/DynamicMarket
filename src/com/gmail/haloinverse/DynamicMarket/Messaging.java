package com.gmail.haloinverse.DynamicMarket;

/*     */ //package com.nijikokun.bukkit.SimpleShop;
/*     */ 
// CHANGED: Commented out imports that weren't being used.
/*     */ //import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
/*     */ 
/*     */ public class Messaging
/*     */ {
/*  32 */   public Player player = null;
			private CommandSender sender = null;

			public static String colNormal = "&e";	// Normal text colour {}
			public static String colCmd = "&f"; 		// Command highlight colour {CMD}
			public static String colBracket = "&d"; // Highlighting of brackets around params/data {PBK}
			public static String colParam = "&b";		// Highlighting of parameters. 
			public static String colError = "&c";		// Highlighting for errors. {ERR}
/*     */ 
			public Messaging(CommandSender thisSender)
			{
				sender = thisSender;
				if (thisSender instanceof Player)
					player = (Player)thisSender;
			}
			
			public boolean isPlayer()
			{
				if (player == null)
					return false;
				return true;
			}

			@Deprecated
/*     */   public static String argument(String original, String[] arguments, String[] points)
/*     */   {
/*  44 */     for (int i = 0; i < arguments.length; ++i) {
/*  45 */       if (arguments[i].contains(",")) {
/*  46 */         for (String arg : arguments[i].split(","))
/*  47 */           original = original.replace(arg, points[i]);
/*     */       }
/*     */       else {
/*  50 */         original = original.replace(arguments[i], points[i]);
/*     */       }
/*     */     }
/*     */ 
/*  54 */     return original;
/*     */   }
/*     */ 
			//TODO: Use Bukkit's colour enums.
			//TODO: Make highlight colours configurable.
			
			public static String parseHighlights(String original)
			{
				return original.replace("{}",colNormal).replace("{CMD}",colCmd).replace("{BKT}",colBracket).replace("{ERR}",colError).replace("{PRM}",colParam);
			}
			
			public static String stripHighlights(String original)
			{
				return original.replace("{}","").replace("{CMD}","").replace("{BKT}","").replace("{ERR}","").replace("{PRM}","");
			}

/*     */   public static String parse(String original)
/*     */   {
/*  70 */     return parseHighlights(original).replaceAll("(&([a-z0-9]))", "§$2").replace("&&", "&");
/*     */   }
/*     */ 
/*     */   public static String colorize(String original)
/*     */   {
/*  87 */     return original.replace("<black>", "§0").replace("<navy>", "§1").replace("<green>", "§2").replace("<teal>", "§3").replace("<red>", "§4").replace("<purple>", "§5").replace("<gold>", "§6").replace("<silver>", "§7").replace("<gray>", "§8").replace("<blue>", "§9").replace("<lime>", "§a").replace("<aqua>", "§b").replace("<rose>", "§c").replace("<pink>", "§d").replace("<yellow>", "§e").replace("<white>", "§f");
/*     */   }
/*     */ 
/*     */   public static String bracketize(String message)
/*     */   {
/*  98 */     return "[" + message + "]";
/*     */   }
/*     */ 
/*     */ 	
/*     */   public void send(String message)
/*     */   {
/* 138 */     if (sender != null)
/* 139 */       sender.sendMessage(parse(message));
/*     */   }
/*     */ 
/*     */   public static void broadcast(String message)
/*     */   {
/* 148 */     for (Player p : iListen.plugin.getServer().getOnlinePlayers())
/* 149 */       p.sendMessage(parse(message));
/*     */   }
/*     */ }

/* Location:           C:\Program Files\eclipse\Bukkit\SimpleShop.jar
 * Qualified Name:     com.nijikokun.bukkit.SimpleShop.Messaging
 * Java Class Version: 5 (49.0)
 * JD-Core Version:    0.5.3
 */