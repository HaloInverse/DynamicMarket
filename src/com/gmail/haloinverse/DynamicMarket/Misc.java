package com.gmail.haloinverse.DynamicMarket;

/*     */ //package com.nijikokun.bukkit.SimpleShop;
/*     */ 
// CHANGED: Commented out imports that weren't being used.
/*     */ //import org.bukkit.Server;
/*     */ //import org.bukkit.entity.Player;
/*     */ 
/*     */ public class Misc
/*     */ {
			public static String headerify(String innerText)
			{
				//TODO: Catch cases where a colour code ends up at the end of the line.
				//This is capable of crashing the client!
				int extraLength = innerText.length() - (stripCodes(Messaging.stripHighlights(innerText))).length();
				String newString = "--" + innerText + "------------------------------------------------------------";
				return newString.substring(0,50+extraLength);
				// This is approximate, due to inability to get string width of the proportional font.
			}
	
			public static String stripCodes(String toStrip)
			// Removes color codes from the string.
			{
				return toStrip.replaceAll("&[a-z0-9]", "").replace("&&", "&");
			}
			 
/*     */   public static boolean isEither(String text, String against, String or)
/*     */   {
/*  76 */     return ((text.equalsIgnoreCase(against)) || (text.equalsIgnoreCase(or)));
/*     */   }

			public static boolean isAny(String text, String[] against)
			{
				for(String thisAgainst : against)
				{
					if(text.equalsIgnoreCase(thisAgainst))
						return true;
				}
				return false;
			}
/*     */ 	
			//@Deprecated
/*     */   //public static String formatCurrency(int Balance, String currency)
/*     */   //{
/*  88 */   //  return insertCommas(String.valueOf(Balance)) + " " + currency;
/*     */   //}
/*     */ 	
			//@Deprecated
/*     */   //public static String insertCommas(String str)
/*     */   //{
/*  99 */   //  if (str.length() < 4) {
/* 100 */   //    return str;
/*     */   //  }
/*     */ 
/* 103 */   //  return insertCommas(str.substring(0, str.length() - 3)) + "," + str.substring(str.length() - 3, str.length());
/*     */   //}
/*     */ 
			//@Deprecated
/*     */   //public static String string(int i)
/*     */   //{
/* 112 */   //  return String.valueOf(i);
/*     */   //}
/*     */ 
			//@Deprecated
/*     */   //public static boolean validate(String name) {
/* 116 */   //  return name.matches("([0-9a-zA-Z._-]+)");
/*     */   //}
/*     */ 
			//@Deprecated
/*     */   //public static String repeat(char c, int i) {
/* 120 */   //  String tst = "";
/* 121 */   //  for (int j = 0; j < i; ++j) {
/* 122 */   //    tst = tst + c;
/*     */   //  }
/* 124 */   //  return tst;
/*     */   //}
/*     */ 
			//@Deprecated
/*     */   //public static Player player(String name)
/*     */   //{
/* 131 */   //  if (iListen.plugin.getServer().getOnlinePlayers().length < 1) {
/* 132 */   //    return null;
/*     */   //  }
/*     */ 
/* 135 */   //  Player[] online = iListen.plugin.getServer().getOnlinePlayers();
/* 136 */   //  Player player = null;
/*     */ 
/* 138 */   //  for (Player needle : online) {
/* 139 */   //    if (needle.getName().equals(name)) {
/* 140 */   //      player = needle;
/* 141 */   //      break;
/*     */   //    }
/*     */   //  }
/*     */ 
/* 145 */   //  return player;
/*     */   //}
/*     */ 
			//@Deprecated
/*     */   //public static Player playerMatch(String name)
/*     */   //{
/* 152 */   //  if (iListen.plugin.getServer().getOnlinePlayers().length < 1) {
/* 153 */   //    return null;
/*     */   //  }
/*     */ 
/* 156 */   //  Player[] online = iListen.plugin.getServer().getOnlinePlayers();
/* 157 */   //  Player lastPlayer = null;
/* 158 */   //  name = name.toLowerCase();
/*     */ 
/* 160 */   //  for (Player player : online) {
/* 161 */   //    String playerName = player.getName();
/*     */ 
/* 163 */   //    if (playerName.toLowerCase().equals(name)) {
/* 164 */   //      lastPlayer = player;
/* 165 */   //      break;
/*     */   //    }
/*     */ 
/* 168 */   //    if (playerName.toLowerCase().indexOf(name.toLowerCase()) != -1) {
/* 169 */   //      if (lastPlayer != null) {
/* 170 */   //        return null;
/*     */   //      }
/*     */ 
/* 173 */   //      lastPlayer = player;
/*     */   //    }
/*     */   //  }
/*     */ 
/* 177 */   //  return lastPlayer;
/*     */   //}
/*     */ 

/*     */   public static String combineSplit(int startIndex, String[] string, String seperator) {
/* 181 */      StringBuilder builder = new StringBuilder();
/*     */ 
/* 183 */     for (int i = startIndex; i < string.length; ++i) {
/* 184 */       builder.append(string[i]);
/* 185 */       builder.append(seperator);
/*     */     }
/*     */ 
/* 188 */     builder.deleteCharAt(builder.length() - seperator.length());
/* 189 */     return builder.toString();
/*     */   }
/*     */ }

/* Location:           C:\Program Files\eclipse\Bukkit\SimpleShop.jar
 * Qualified Name:     com.nijikokun.bukkit.SimpleShop.Misc
 * Java Class Version: 5 (49.0)
 * JD-Core Version:    0.5.3
 */