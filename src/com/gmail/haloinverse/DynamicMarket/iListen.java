package com.gmail.haloinverse.DynamicMarket;

import com.nijikokun.bukkit.Permissions.Permissions;
import com.nijikokun.bukkit.iConomy.iConomy;
import java.util.ArrayList;
//import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.inventory.ItemStack;


public class iListen extends PlayerListener
{
	public static DynamicMarket plugin;
	
	public iListen(DynamicMarket instance)
	{
		plugin = instance;
	}

	private boolean hasPermission(CommandSender sender, String permString)
	{
		if (plugin.simplePermissions)
		{
			if (sender instanceof Player)
			{
				if (Misc.isAny(permString, new String[] {"access", "buy", "sell"}))
					return true;
				else
					return false;
			}
			else
				return true;
		}
		if (plugin.wrapperPermissions)
		{
			if (plugin.permissionWrapper != null)
				return plugin.permissionWrapper.permission(sender, permString);
			plugin.log.severe(Messaging.bracketize(plugin.name) + "WARNING: wrapper-permissions set, but no permission handler registered!");
			return false;
		}
		// Permissions not overridden.
		if (sender instanceof Player)
			return Permissions.Security.permission((Player)sender, plugin.name.toLowerCase()+"."+permString);
		return true;
	}

	private boolean showHelp(CommandSender sender, String topic)
	{
		//TODO: Migrate help system to an MCDocs-like plugin eventually.
		Messaging message = new Messaging(sender);

		if (topic.isEmpty())
		{
			String commands = "";
			String topics = "";
			String shortcuts = "";
			message.send("{}" + Misc.headerify("{CMD} " + plugin.name + " {BKT}({CMD}" + plugin.codename + "{BKT}){} "));
			message.send("{} {BKT}(){} Optional, {PRM}<>{} Parameter");
			message.send("{CMD} /shop help {BKT}({PRM}<topic/command>{BKT}){} - Show help.");
			message.send("{CMD} /shop {PRM}<id>{BKT}({CMD}:{PRM}<count>{BKT}){} - Show buy/sell info on an item.");
			message.send("{CMD} /shop {PRM}<command> <params>{} - Use a shop command.");
			commands += " list";
			shortcuts += " -? -l";
			if (hasPermission(sender,"buy"))
			{
				commands += " buy";
				shortcuts += " -b";
			}
			if (hasPermission(sender,"sell"))
			{
				commands += " sell";
				shortcuts += " -s";
			}
			
			commands += " info";
			shortcuts += " -i";
			
			if (hasPermission(sender,"items.add"))
			{
				commands += " add";
				shortcuts += " -a";
			}
			if (hasPermission(sender,"items.update"))
			{
				commands += " update";
				shortcuts += " -u";
			}
			if (hasPermission(sender,"items.remove"))
			{
				commands += " remove";
				shortcuts += " -r";
			}    	
			if (hasPermission(sender,"admin"))
			{
				commands += " reload";
				commands +=  " reset";
				commands += " exportdb importdb";
			}
			
			topics += "ids details about";
			if (hasPermission(sender, "items.add") || hasPermission(sender, "items.update"))
				topics += " tags";
			
			message.send("{} Commands: {CMD}" + commands);
			message.send("{} Shortcuts: {CMD}" + shortcuts);
			message.send("{} Other help topics: {PRM}" + topics);
			return true;
		}
		message.send("{}" + Misc.headerify("{} " + plugin.name + " {BKT}({}" + plugin.codename + "{BKT}){} : " + topic + "{} "));
		if (topic.equalsIgnoreCase("buy"))
		{
			if (hasPermission(sender,"buy"))
			{
				message.send("{CMD} /shop buy {PRM}<id>{BKT}({CMD}:{PRM}<count>{CMD})");
				message.send("{} Buy {PRM}<count>{} bundles of an item.");
				message.send("{} If {PRM}<count>{} is missing, buys 1 bundle.");
				return true;
			}
		}
		if (topic.equalsIgnoreCase("sell"))
		{
			if (hasPermission(sender,"sell"))
			{
				message.send("{CMD} /shop sell {PRM}<id>{BKT}({CMD}:{PRM}<count>{CMD})");
				message.send("{} Sell {PRM}<count>{} bundles of an item.");
				message.send("{} If {PRM}<count>{} is missing, sells 1 bundle.");
				return true;
			}
		}
		if (topic.equalsIgnoreCase("info"))
		{
			//if (hasPermission(player,"sell"))
			//{
				message.send("{CMD} /shop info {PRM}<id>");
				message.send("{} Show detailed information about a shop item.");
				message.send("{} Unlike {CMD}/shop {PRM}<id>{}, this shows ALL fields.");
				return true;
			//}
		}
		if (topic.equalsIgnoreCase("add"))
		{
			if (hasPermission(sender,"items.add"))
			{
				message.send("{CMD} /shop add {PRM}<id>{BKT}({CMD}:{PRM}<bundle>{BKT}) ({PRM}<buyPrice>{BKT} ({PRM}<sellPrice>{BKT})) {PRM}<tags>");
				message.send("{} Adds item {PRM}<id>{} to the shop.");
				message.send("{} Transactions will be in {PRM}<bundle>{} units (default 1).");
				message.send("{PRM} <buyPrice>{} and {PRM}<sellPrice>{} will be converted, if used.");
				message.send("{} Prices are per-bundle.");
				message.send("{} See also: {CMD}/shop help tags");
				return true;
			}
		}
		if (topic.equalsIgnoreCase("update"))
		{
			if (hasPermission(sender,"items.update"))
			{
				message.send("{CMD} /shop update {PRM}<id>{BKT}({CMD}:{PRM}<bundle>{BKT}) ({PRM}<buyPrice>{BKT} ({PRM}<sellPrice>{BKT})) {PRM}<tags>");
				message.send("{} Changes item {PRM}<id>{}'s shop details.");
				message.send("{PRM} <bundle>{}, {PRM}<buyPrice>{}, {PRM}<sellPrice>{}, and {PRM}<tags>{} will be changed.");
				message.send("{} Transactions will be in {PRM}<bundle>{} units (default 1).");
				message.send("{} Prices are per-bundle.");
				message.send("{} See also: {CMD}/shop help tags");
				return true;
			}
		}
		if (topic.equalsIgnoreCase("remove"))
		{
			if (hasPermission(sender,"items.remove"))
			{
				message.send("{CMD} /shop remove {PRM}<id>");
				message.send("{} Removes item {PRM}<id>{} from the shop.");
				return true;
			}
		}
		if (hasPermission(sender,"admin"))
		{
			if (topic.equalsIgnoreCase("reload"))
			{
				message.send("{CMD} /shop reload");
				message.send("{} Restarts the shop plugin.");
				message.send("{} Attempts to reload all relevant config files.");
				return true;
			}
			if (topic.equalsIgnoreCase("reset"))
			{
				message.send("{CMD} /shop reset");
				message.send("{} Completely resets the shop database.");
				message.send("{} This will remove all items from the shop, and");
				message.send("{} create a new empty shop database.");
				return true;
			}
			if (topic.equalsIgnoreCase("exportdb"))
			{
				message.send("{CMD} /shop exportdb");
				message.send("{} Dumps the shop database to a .csv file.");
				message.send("{} Name and location are set in the main config file.");
				message.send("{} The file can be edited by most spreadsheet programs.");
				return true;
			}
			if (topic.equalsIgnoreCase("importdb"))
			{
				message.send("{CMD} /shop importdb");
				message.send("{} Reads a .csv file in to the shop database.");
				message.send("{} Name and location are set in the main config file.");
				message.send("{} The format MUST be the same as the export format.");
				message.send("{} Records matching id/subtype will be updated.");
				return true;
			}					
		}
		if (topic.equalsIgnoreCase("ids"))
		{
			message.send("{} Item ID format: {PRM}<id>{BKT}({CMD},{PRM}<subtype>{BKT})({CMD}:{PRM}<count>{BKT})");
			message.send("{PRM} <id>{}: Full name or ID number of the item.");
			message.send("{PRM} <subtype>{}: Subtype of the item (default: 0)");
			message.send("{} Subtypes are used for wool/dye colours, log types, etc.");
			message.send("{PRM} <count>{}: For shop items, this specifies bundle size.");
			message.send("{} For transactions, this sets the number of bundles bought or sold.");
			return true;
		}
		if (topic.equalsIgnoreCase("list"))
		{
			message.send("{CMD} /shop list {BKT}({PRM}<nameFilter>{BKT}) ({PRM}<page>{BKT})");
			message.send("{} Displays the items in the shop.");
			message.send("{} List format: {BKT}[{PRM}<id#>{BKT}]{PRM} <fullName> {BKT}[{PRM}<bundleSize>{BKT}]{} Buy {BKT}[{PRM}<buyPrice>{BKT}]{} Sell {BKT}[{PRM}<sellPrice>{BKT}]");
			message.send("{} Page 1 is displayed by default, if no page number is given.");
			message.send("{} If {PRM}<nameFilter>{} is used, displays items containing {PRM}<nameFilter>{}.");
			return true;
		}
		if (topic.equalsIgnoreCase("details"))
		{
			message.send("{CMD} /shop {PRM}<id>{BKT}({CMD}:{PRM}<count>{BKT})");
			message.send("{} Displays the current buy/sell price of the selected item.");
			message.send("{} Since prices can fluctuate, use {PRM}<count>{} to get batch pricing.");
			message.send("{} See {CMD}/shop help ids{} for information on IDs.");
			return true;
		}
		if ((Misc.isEither(topic.split(" ")[0],"tags","tag")) && ((hasPermission(sender, "items.add") || hasPermission(sender, "items.update"))))
		{
			if(topic.indexOf(" ") > -1)
			{
				// Possible tag listed!
				String thisTag = topic.split(" ")[1].replace(":","");
				if (Misc.isEither(thisTag, "n","name"))
				{
					message.send("{CMD} n:{BKT}|{CMD}name:{} - Name/rename item");
					message.send("{} Sets the item's name in the shop DB.");
					message.send("{} New name will persist until the item is removed.");
					message.send("{} If name is blank, will try to reload the name from items.db.");
					return true;
				}
				if (Misc.isEither(thisTag, "bp","baseprice"))
				{
					message.send("{CMD} bp:{BKT}|{CMD}BasePrice:{} - Base purchase price");
					message.send("{} Buy price of the item at stock level 0.");
					message.send("{} All other prices are derived from this starting value.");
					message.send("{} Referenced by {PRM}SalesTax{}, {PRM}Stock{}, and {PRM}Volatility{}.");
					message.send("{} Soft-limited by {PRM}PriceFloor{}/{PRM}PriceCeiling{}.");
					return true;
				}
				if (Misc.isEither(thisTag, "s","stock"))
				{
					message.send("{CMD} s:{BKT}|{CMD}Stock:{} - Current stock level");
					message.send("{} Stock level of this item (in bundles).");
					message.send("{} Increases/decreases when items are sold/bought.");
					message.send("{} Affects buy/sell prices, if {PRM}Volatility{} > 0.");
					message.send("{} Soft-limited by {PRM}StockFloor{}/{PRM}StockCeiling{}.");
					message.send("{} Hard-limited (transactions fail) by {PRM}StockLowest{}/{PRM}StockHighest{}.");
					return true;
				}
				if (Misc.isEither(thisTag, "cb","canbuy"))
				{
					message.send("{CMD} cb:{BKT}|{CMD}CanBuy:{} - Buyability of item");
					message.send("{} Set to 'Y', 'T', or blank to allow buying from shop.");
					message.send("{} Set to 'N' or 'F' to disallow buying from shop.");
					return true;
				}
				if (Misc.isEither(thisTag, "cs","cansell"))
				{
					message.send("{CMD} cs:{BKT}|{CMD}CanSell:{} - Sellability of item");
					message.send("{} Set to 'Y', 'T', or blank to allow selling to shop.");
					message.send("{} Set to 'N' or 'F' to disallow selling to shop.");
					return true;
				}
				if (Misc.isAny(thisTag, new String[] {"v","vol", "volatility"}))
				{
					message.send("{CMD} v:{BKT}|{CMD}Vol:{}{BKT}|{CMD}Volatility:{} - Price volatility");
					message.send("{} Percent increase in price per 1 bundle bought from shop, * 10000.");
					message.send("{} v=0 prevents the price from changing with stock level.");
					message.send("{} v=1 increases the price 1% per 100 bundles bought.");
					message.send("{} v=10000 increases the price 100% per 1 bundle bought.");
					message.send("{} Calculations are compound vs. current stock level.");							
					return true;
				}
				if (Misc.isAny(thisTag, new String[] {"iv","ivol", "invvolatility"}))
				{
					message.send("{CMD} iv:{BKT}|{CMD}IVol:{}{BKT}|{CMD}InvVolatility:{} - Inverse Volatility");
					message.send("{} Number of bundles bought in order to double the price.");
					message.send("{} Converted to volatility when entered.");						
					message.send("{} iv=+INF prevents the price from changing with stock level.");
					message.send("{} iv=6400 doubles the price for each 6400 items bought.");
					message.send("{} iv=1 doubles the price for each item bought.");
					message.send("{} Calculations are compound vs. current stock level.");							
					return true;
				}
				if (Misc.isEither(thisTag, "st","salestax"))
				{
					message.send("{CMD} st:{BKT}|{CMD}SalesTax:{} - Sales Tax");
					message.send("{} Percent difference between BuyPrice and SellPrice, * 100.");
					message.send("{} {PRM}SellPrice{}={PRM}BuyPrice{}*(1-({PRM}SalesTax{}/100))");						
					message.send("{} If {PRM}SellPrice{} is entered as an untagged value, it is used to calculate {PRM}SalesTax{}.");
					message.send("{} {PRM}SalesTax{} is applied after {PRM}PriceFloor{}/{PRM}PriceCeiling{}.");							
					return true;
				}
				if (Misc.isEither(thisTag, "sl","stocklowest"))
				{
					message.send("{CMD} sl:{BKT}|{CMD}StockLowest:{} - Lowest stock level (hard limit)");
					message.send("{} Buying from shop will fail if it would put stock below {PRM}StockLowest{}.");
					message.send("{} Set to 0 to to make stock 'finite'.");
					message.send("{} Set to -INF or a negative value to use stock level as a 'relative offset'.");						
					return true;
				}
				if (Misc.isEither(thisTag, "sh","stockhighest"))
				{
					message.send("{CMD} sh:{BKT}|{CMD}StockHighest:{} - Highest stock level (hard limit)");
					message.send("{} Selling to shop will fail if it would put stock above {PRM}StockHighest{}.");
					message.send("{} Set to +INF to let maximum stock be unlimited.");						
					return true;
				}
				if (Misc.isEither(thisTag, "sf","stockfloor"))
				{
					message.send("{CMD} sf:{BKT}|{CMD}StockFloor:{} - Lowest stock level (soft limit)");
					message.send("{} If {PRM}Stock{} falls below {PRM}StockFloor{}, it will be reset to {PRM}StockFloor{}.");
					message.send("{} Further purchases will be at a flat rate, until {PRM}Stock{} rises.");						
					return true;
				}
				if (Misc.isEither(thisTag, "sc","stockceiling"))
				{
					message.send("{CMD} sc:{BKT}|{CMD}StockCeiling:{} - Highest stock level (soft limit)");
					message.send("{} If {PRM}Stock{} rises above {PRM}StockCeiling{}, it will be reset to {PRM}StockCeiling{}.");
					message.send("{} Further sales will be at a flat rate, until {PRM}Stock{} falls.");						
					return true;
				}
				if (Misc.isEither(thisTag, "pf","pricefloor"))
				{
					message.send("{CMD} pf:{BKT}|{CMD}PriceFloor:{} - Lowest buy price (soft limit)");
					message.send("{} If {PRM}BuyPrice{} falls below {PRM}PriceFloor{}, it will be cropped at {PRM}PriceFloor{}.");
					message.send("{} Buy/sell prices will be at a flat rate, until {PRM}BuyPrice{} rises above {PRM}PriceFloor{}.");
					message.send("{} {PRM}PriceFloor{} is applied to {PRM}SellPrice{} before {PRM}SalesTax{}.");
					return true;
				}
				if (Misc.isEither(thisTag, "pc","priceceiling"))
				{
					message.send("{CMD} pc:{BKT}|{CMD}PriceCeiling:{} - Highest buy price (soft limit)");
					message.send("{} If {PRM}BuyPrice{} rises above {PRM}PriceCeiling{}, it will be cropped at {PRM}PriceCeiling{}.");
					message.send("{} Buy/sell prices will be at a flat rate, until {PRM}BuyPrice{} falls below {PRM}PriceCeiling{}.");
					message.send("{} {PRM}PriceCeiling{} is applied to {PRM}SellPrice{} before {PRM}SalesTax{}.");
					return true;
				}
				if (thisTag.equalsIgnoreCase("flat"))
				{
					message.send("{CMD} flat{} - Set item with flat pricing.");
					message.send("{} Buy/sell prices for this item will not change with stock level.");
					message.send("{} Stock level WILL be tracked, and can float freely.");
					message.send("{} Equivalent to: {CMD}s:0 sl:-INF sh:+INF sf:-INF sc:+INF v:0 pf:0 pc:+INF");
					return true;
				}
				if (thisTag.equalsIgnoreCase("fixed"))
				{
					message.send("{CMD} fixed{} - Set item with fixed pricing.");
					message.send("{} Buy/sell prices for this item will not change with transactions.");
					message.send("{} Stock level WILL NOT be tracked, and {PRM}Stock{} will remain at 0.");
					message.send("{} Equivalent to: {CMD}s:0 sl:-INF sh:+INF sf:0 sc:0 v:0 pf:0 pc:+INF");
					return true;
				}
				if (thisTag.equalsIgnoreCase("float"))
				{
					message.send("{CMD} float{} - Set item with floating pricing.");
					message.send("{} Buy/sell prices for this item will vary by stock level.");
					message.send("{} If {PRM}Vol{}=0, {PRM}Vol{} will be set to a default of 100.");
					message.send("{} (For finer control, set {PRM}Volatility{} to an appropriate value.)");
					message.send("{} Stock level can float freely above and below 0 with transactions.");
					message.send("{} Equivalent to: {CMD}sl:-INF sh:+INF sf:-INF sc:+INF {BKT}({CMD}v:100{BKT}){CMD} pf:0 pc:+INF");
					return true;
				}
				if (thisTag.equalsIgnoreCase("finite"))
				{
					message.send("{CMD} finite{} - Set item with finite stock.");
					message.send("{} Buying from shop will fail if it would make {PRM}Stock{} < 0.");
					message.send("{} Any number of items can be sold to the shop.");
					message.send("{} Equivalent to: {CMD}sl:0 sh:+INF sf:-INF sc:+INF");
					return true;
				}
				if (thisTag.equalsIgnoreCase("renorm"))
				{
					message.send("{CMD} renorm{BKT}({CMD}:{PRM}<stock>{BKT}){} - Renormalize an item's price.");
					message.send("{} Resets an item's {PRM}Stock{}, while preserving its current price.");
					message.send("{} Sets an item's {PRM}BasePrice{} to its current {PRM}BuyPrice{},");
					message.send("{} then sets {PRM}Stock{} to {PRM}<stock>{} (0 if blank or missing).");
					return true;
				}
				message.send("{ERR} Unknown tag {PRM}"+thisTag+"{ERR}.");
				message.send("{ERR} Use {CMD}/shop help tags{ERR} to list tags.");
				return false;
			}
			else
			{
				message.send("{} Tag format: {PRM}<tagName>{BKT}({CMD}:{PRM}<value>{BKT}) ({PRM}<tagName>{BKT}({CMD}:{PRM}<value>{BKT}))...");
				message.send("{} Available tags: {CMD} Name: BasePrice: SalesTax: Stock: CanBuy: CanSell: Vol: IVol:");
				message.send("{CMD} StockLowest: StockHighest: StockFloor: StockCeiling: PriceFloor: PriceCeiling:");
				message.send("{} Available preset tags: {CMD}Fixed Flat Float Finite Renorm:");
				message.send("{} Use {CMD}/shop help tag {PRM}<tagName>{} for tag descriptions.");
				return true;
			}
		}
		if (topic.equalsIgnoreCase("about"))
		{
			message.send("{} " + plugin.name + " " + plugin.version + " written by HaloInverse.");
			message.send("{} Original structure and portions of code are from SimpleShop 1.1 by Nijikokun.");
			return true;
		}				
		message.send("{}Unknown help topic:{CMD} " + topic);
		message.send("{}Use {CMD}/shop help{} to list topics.");
				return false;
			}

		 
		   private int get_balance(String name) {
		     return iConomy.db.get_balance(name);
		   }
		 
		   private void set_balance(String name, int amount) {
		     iConomy.db.set_balance(name, amount);
		   }
		 
		   private void show_balance(Player player) {
		     plugin.iC.l.showBalance(player.getName(), player, true);
		   }
		 

			private boolean shopShowItemInfo(String itemString, Messaging message, boolean fullInfo, String shopLabel)
			{
				ItemClump requested = new ItemClump(itemString, plugin.db, shopLabel);
 
				if (!requested.isValid()) {
					message.send(plugin.shop_tag + "{ERR}Unrecognized or invalid item or command.");
					return false;
				}
 
				MarketItem data = plugin.db.data(requested, shopLabel);

				if (data == null) {
					message.send(plugin.shop_tag + "{ERR}Item currently not traded in shop.");
			return false;
		}

		message.send(plugin.shop_tag + "{}Item {PRM}" + data.getName() + "{BKT}[{PRM}" + data.idString() + "{BKT}]{} info:");
		if (fullInfo)
		{
			ArrayList<String> thisList = data.infoStringFull();
			for (String thisLine : thisList)
				message.send(thisLine);
		}
		else
		{
			//message.send("Placeholder...");
			message.send(data.infoStringBuy(requested.count));
			message.send(data.infoStringSell(requested.count));
		}
		return true;
	}

	private boolean shopBuyItem(Player player, String itemString, String shopLabel)
	{
		//TODO: Check for sufficient inventory space for received items.
		ItemClump requested = new ItemClump(itemString, plugin.db, shopLabel);
		Messaging message = new Messaging(player);

		int balance = get_balance(player.getName());
		
		int transValue;

		if (!requested.isValid()) {
			message.send(plugin.shop_tag + "{ERR}Invalid item.");
			message.send("Use: {CMD}/shop buy {PRM}<item id or name>{BKT}({CMD}:{PRM}<bundles>{BKT})");
			return false;
		}

		MarketItem data = plugin.db.data(requested, shopLabel);

		if ((data == null) || !data.isValid()) {
			message.send(plugin.shop_tag + "{ERR}Unrecognized item name, or not in shop.");
			return false;
		}

		if (data.isDefault()) {
			message.send(plugin.shop_tag + "{ERR}The default item template is not buyable.");
			return false;
		}				
		
		if (!data.canBuy) {
			message.send(plugin.shop_tag + "{ERR}" + data.getName() + " currently not purchaseable from shop.");
			return false;
		}
		
		if (!data.getCanBuy(requested.count)) {
			message.send(plugin.shop_tag + "{ERR}" + data.getName() + " understocked: only " + data.formatBundleCount(data.leftToBuy()) + " left.");
			return false;
		}

		if ((requested.count < 1) || (requested.count * data.count > plugin.max_per_purchase)) {
			message.send(plugin.shop_tag + "{ERR}Amount over max items per purchase.");
					return false;
				}
 
				transValue = data.getBuyPrice(requested.count);
				
				//if (balance < data.buy * requested.count) {
		if (balance < transValue) {
			message.send(plugin.shop_tag + "{ERR}You do not have enough " + plugin.currency + " to do this.");
					message.send(data.infoStringBuy(requested.count));
					return false;
				}
 
				//set_balance(player.getName(), balance - (data.buy * requested.count));
		set_balance(player.getName(), balance - transValue);

		player.getInventory().addItem(new ItemStack[] { new ItemStack(data.itemId, requested.count * data.count, (short) 0, (byte)requested.subType) });
		
		plugin.db.removeStock(requested, shopLabel);

		message.send(plugin.shop_tag + "Purchased {BKT}[{PRM}" + data.formatBundleCount(requested.count) + "{BKT}]{PRM} " + data.getName() + "{} for {PRM}" + transValue + " " + iConomy.currency);
		show_balance(player);
		return true;
	}
	
	private boolean shopSellItem(Player player, String itemString, String shopLabel)
	{
		ItemClump requested = new ItemClump(itemString, plugin.db, shopLabel);
		Messaging message = new Messaging(player);

		int balance = get_balance(player.getName());
		int transValue;

		if (!requested.isValid()) {
			message.send(plugin.shop_tag + "{ERR}Invalid item.");
			message.send("Use: {CMD}/shop sell {PRM}<item id or name>{BKT}({CMD}:{PRM}<bundles>{BKT})");
			return false;
		}
		
		MarketItem data = plugin.db.data(requested, shopLabel);
		
		if ((data == null) || !data.isValid()) {
			message.send(plugin.shop_tag + "{ERR}Unrecognized item name, or not in shop.");
			return false;
		}

		if (data.isDefault()) {
			message.send(plugin.shop_tag + "{ERR}The default template is not sellable.");
			return false;
		}
		
		if (data.canSell == false) {
			message.send(plugin.shop_tag + "{ERR}" + data.getName() + " currently not sellable to shop.");
					return false;
				}
 
				if ((requested.count < 1) || (requested.count * data.count > plugin.max_per_sale)) {
					message.send(plugin.shop_tag + "{ERR}Amount over max items per sale.");
			return false;
		}
		
		if (!data.getCanSell(requested.count)) {
			message.send(plugin.shop_tag + "{ERR}" + data.getName() + " overstocked: only " + data.formatBundleCount(data.leftToSell()) + " can be sold.");
					return false;
				}
 
				if (!(Items.has(player, data, requested.count))) {
					message.send(plugin.shop_tag + "{ERR}You do not have enough " + data.getName() + " to do this.");
			return false;
		}

		transValue = data.getSellPrice(requested.count);
		
		plugin.items.remove(player, data, requested.count);

		//set_balance(player.getName(), balance + data.sell * requested.count);
		set_balance(player.getName(), balance + transValue);
		plugin.db.addStock(requested, shopLabel);

		message.send(plugin.shop_tag + "Sold {BKT}[{PRM}" + data.formatBundleCount(requested.count) + "{BKT}]{PRM} " + data.getName() + "{} for {PRM}" + transValue + " " + iConomy.currency);
				show_balance(player);
				return true;
			}
			
			
			private boolean shopAddItem(String itemString, Messaging message, String shopLabel)
			{
				MarketItem newItem = new MarketItem(itemString, plugin.db.getDefault(shopLabel), plugin.db, shopLabel);
 
				if (!newItem.isValid()) {
					message.send(plugin.shop_tag + "{ERR}Unrecognized item name or ID.");
			return false;
		}

		if (plugin.db.hasRecord(newItem, shopLabel))
		{
			message.send(plugin.shop_tag + "{ERR}"+newItem.getName()+" is already in the market list.");
			message.send(plugin.shop_tag + "{ERR}Use {CMD}/shop update{ERR} instead.");
			return false;					
		}
		
		if ((newItem.count < 1) || (newItem.count > plugin.max_per_sale)) {
			message.send(plugin.shop_tag + "{ERR}Invalid amount. (Range: 1.." + plugin.max_per_sale + ")");
			return false;
		}

		if (plugin.db.add(newItem, shopLabel))
		{
			message.send(plugin.shop_tag + "Item {PRM}" + newItem.getName() + "{} added:");
		 	//message.send(newItem.infoStringBuy());
		 	//message.send(newItem.infoStringSell());
			ArrayList<String> thisList = newItem.infoStringFull();
			for (String thisLine : thisList)
				message.send(thisLine);
		 	return true;
		}
		else
		{
			message.send(plugin.shop_tag + "{ERR}Item {PRM}" + newItem.getName() + "{ERR} could not be added.");
			return false;
		}
	}
	
	private boolean shopUpdateItem(String itemStringIn, Messaging message, String shopLabel)
	{
		String itemString = new String(itemStringIn); 
		// Fetch the previous record and use it as the default for parsing these string tags.

		ItemClump requested = new ItemClump(itemString, plugin.db, shopLabel);

		if (requested == null) {
			message.send(plugin.shop_tag + "{ERR}Unrecognized item name or ID.");
			return false;
		}
		
		MarketItem prevData = plugin.db.data(requested, shopLabel);

		if (prevData == null) {
			message.send(plugin.shop_tag + "{ERR}" + itemString.split(" ")[0] + " not found in market.");
			message.send(plugin.shop_tag + "{ERR}Use {CMD}/shop add{ERR} instead.");
			return false;
		}
		
		// If no :count is input, insert it into itemString. 
		if (!(itemString.split(" ")[0].contains(":")))
		{
			String[] itemSubStrings = itemString.split(" ", 2);
			itemSubStrings[0] += ":" + prevData.count;
			if (itemSubStrings.length > 1)
				itemString = itemSubStrings[0] + " " + itemSubStrings[1];
					else
						itemString = itemSubStrings[0];
				}
				
				MarketItem updated = new MarketItem(itemString, prevData, plugin.db, shopLabel);
 
				if ((updated.count < 1) || (updated.count > plugin.max_per_sale)) {
					message.send(plugin.shop_tag + "{ERR}Invalid amount. (Range: 1.." + plugin.max_per_sale + ")");
					return false;
				}
 
				if (plugin.db.update(updated, shopLabel))
				{
					message.send(plugin.shop_tag + "Item {PRM}" + updated.getName() + "{} updated:");
			//message.send(updated.infoStringBuy());
			//message.send(updated.infoStringSell());
			ArrayList<String> thisList = updated.infoStringFull();
			for (String thisLine : thisList)
				message.send(thisLine);
			return true;
		}
		else
		{
			message.send(plugin.shop_tag + "Item {PRM}" + updated.getName() + "{} update {ERR}failed.");
					return false;
				}
			}
			
			private boolean shopRemoveItem(String itemString, Messaging message, String shopLabel)
			{
				ItemClump removed = new ItemClump(itemString, plugin.db, shopLabel);
				String removedItemName = null;
 
				if (removed.itemId == -1) {
					message.send(plugin.shop_tag + "{ERR}Unrecognized item name or ID.");
			return false;
		}
		
		MarketItem itemToRemove = plugin.db.data(removed, shopLabel);
		
		if (itemToRemove == null)
		{
			message.send(plugin.shop_tag + "{ERR}Item {PRM}" + removed.getName(plugin.db, shopLabel) + "{ERR} not found in market.");
			return false;
		}

		removedItemName = itemToRemove.getName();
		if (removedItemName == null)
			removedItemName = "<Unknown>";
		
		if (plugin.db.remove(removed, shopLabel))
		{
			message.send(plugin.shop_tag + "Item " + removedItemName + " was removed.");
			return true;
		}	
		else
		{
			message.send(plugin.shop_tag + "Item " + removedItemName + " {ERR}could not be removed.");
			return false;
		}
	}


	public boolean shopReset(CommandSender sender, String confirmString, String shopLabel)
	{
		Messaging message = new Messaging(sender);
		if (confirmString.isEmpty())
		{
			message.send("{ERR} Warning!{} This will DELETE AND REBUILD the shop DB.");
			message.send("{} If you're sure, type: {CMD}/shop reset confirm");
			return true;
		}
		if (confirmString.equalsIgnoreCase("confirm"))
		{
			if (plugin.db.resetDatabase(shopLabel))
			{
				message.send("{} Database successfully reset.");
				return true;
			}
			else
			{
				message.send("{} Database {ERR}not{} successfully reset.");
				return false;
			}
		}
		message.send("{ERR} Incorrect confirmation keyword.");
		return false;
	}

	public void onPlayerCommand(PlayerChatEvent event)
	{
		// Is this truly deprecated or not?
		plugin.log.info("OnPlayerCommand called with: " + event.getMessage());
		String base;
		String[] args;
		String[] msg = event.getMessage().split(" ",2);
		if (msg.length > 0)
		{
			base = msg[0];
			if (base.startsWith("/"))
				base = base.substring(1);
		}
		else
			return;
		if (msg.length > 1)
			args = msg[1].split(" ");
		else
			args = new String[] {};
		Player player = event.getPlayer();
		if (!onCommand(player, base, "", args, ""))
		{
			// event.setCancelled(true)?
		}
	}

	public boolean onCommand(CommandSender sender, String cmd, String commandLabel, String[] args, String shopLabel) 
	{
		
		//String commandName = cmd.getName().toLowerCase();
		
		//TODO: Show helpful errors for inappropriate numbers of arguments.
		
		//Player player = (Player) sender;
		Messaging message = new Messaging(sender);

		//if (commandName.equals("shop")) {
		//if (cmd.getName().toLowerCase().equals("shop")) {
		if (cmd.toLowerCase().equals("shop")) {

			if (!hasPermission(sender, "access"))
			{
				message.send("{ERR}You do not have permission to access the shop.");
				return false;
			}
			
			if (args.length == 0) {
				showHelp(sender, "");
				return true;
			}

			String subCommand = args[0];

			if ((args.length == 1) && (!(Misc.isAny(subCommand, new String[] {"list", "-l", "help", "-?", "idata", "reset", "reload", "exportdb", "importdb"})))) {				
				return shopShowItemInfo(args[0], message, false, shopLabel);
			}
			
			if ((Misc.isEither(subCommand, "info", "-i")) && (args.length <= 2))
						if (args.length == 2)
							return (shopShowItemInfo(args[1], message, true, shopLabel));
 
					if ((Misc.isEither(subCommand, "help", "-?")) && (args.length <= 3)) {
				if (args.length == 3)
					return showHelp(sender, args[1] + " " + args[2]);
				//else
				if (args.length == 2)
					return showHelp(sender, args[1]);
				//else
				return showHelp(sender, "");
			}

			if ((Misc.isEither(subCommand, "buy", "-b")) && (args.length >= 2)) {
				if (!message.isPlayer())
				{
					message.send("{ERR}Cannot purchase items without being logged in.");
					return false;
				}
				if (!(hasPermission(sender, "buy"))) {
					message.send("{ERR}You do not have permission to buy from the shop.");
					return false;
				}
				return shopBuyItem((Player)sender, args[1], shopLabel);
      		}

			if ((Misc.isEither(subCommand, "sell", "-s")) && (args.length >= 2)) {
				if (!message.isPlayer())
				{
					message.send("{ERR}Cannot sell items without being logged in.");
					return false;
				}
				if (!(hasPermission(sender,"sell"))) {
					message.send("{ERR}You do not have permission to sell to the shop.");
					return false;
				}
				return shopSellItem((Player)sender, args[1], shopLabel);
			}

			if (subCommand.equalsIgnoreCase("reload")) {
				if (!(hasPermission(sender,"admin"))) {
					message.send("{ERR}You do not have permission to reload the shop plugin.");
					return false;
				}
				// CHANGED: Call onDisable before calling onEnable, in case important stuff gets added to onDisable later.
				plugin.onDisable();
				plugin.onEnable();
				return true;
				//TODO: Make this return a more sensible success value than blind-true.
			}

			if (subCommand.equalsIgnoreCase("reset")) {
				if (!(hasPermission(sender,"admin"))) {
					message.send("{ERR}You do not have permission to reset the shop DB.");
					return false;
				}
				if (args.length >= 2)
					return shopReset(sender, args[1], shopLabel);
				else
					return shopReset(sender, "", shopLabel);
			}

			if (subCommand.equalsIgnoreCase("exportDB")) {
				if (!(hasPermission(sender,"admin"))) {
					message.send("{ERR}You do not have permission to export the shop DB.");
					return false;
				}
				if (plugin.db.dumpToCSV(DynamicMarket.csvFilePath + shopLabel + DynamicMarket.csvFileName, shopLabel))
				{
					message.send("{} Database export to {PRM}"+shopLabel+DynamicMarket.csvFileName+"{} successful.");
					return true;
				}
				else
				{
					message.send("{ERR} Database export to {PRM}"+shopLabel+DynamicMarket.csvFileName+"{ERR} NOT successful.");
					message.send("{ERR} See Bukkit console for details.");
					return false;
				}
			}
			
			if (subCommand.equalsIgnoreCase("importDB")) {
				if (!(hasPermission(sender,"admin"))) {
					message.send("{ERR}You do not have permission to import the shop DB.");
					return false;
				}
				if (plugin.db.inhaleFromCSV(DynamicMarket.csvFilePath + shopLabel + DynamicMarket.csvFileName, shopLabel))
				{
					message.send("{} Database import from {PRM}"+shopLabel+DynamicMarket.csvFileName+"{} successful.");
					return true;
				}
				else
				{
					message.send("{ERR} Database import from {PRM}"+shopLabel+DynamicMarket.csvFileName+"{ERR} NOT successful.");
					message.send("{ERR} See Bukkit console for details.");
							return false;
						}
       				}
 
					if ((Misc.isEither(subCommand, "add", "-a")) && (args.length > 2)) {
			// /shop add [id](:count) [buy] [sell]
				if (!(hasPermission(sender, "items.add"))) {
					message.send("{ERR}You do not have permission to add items to the shop.");
					return false;
				}
				return shopAddItem(Misc.combineSplit(1, args, " "), message, shopLabel);
			}

			if ((Misc.isEither(subCommand, "update", "-u")) && (args.length >= 2)) {
				if (!(hasPermission(sender, "items.update"))) {
					message.send("{ERR}You do not have permission to update shop items.");
					return false;
				}
				return shopUpdateItem(Misc.combineSplit(1, args, " "), message, shopLabel);
					}
 
					if ((Misc.isEither(subCommand, "remove", "-r")) && (args.length == 2)) {
			// CHANGED: Remove shortcut changed to -v, to prevent collision with Reload shortcut.
				if (!(hasPermission(sender, "items.remove"))) {
					message.send("{ERR}You do not have permission to remove shop items.");
							return false;
						}
						return shopRemoveItem(args[1], message, shopLabel);
					}
 
					if (Misc.isEither(subCommand, "list", "-l")) {
				// Possible inputs:
				// none (default first page, unfiltered)
				// pageNum
				// nameFilter
				// nameFilter pageNum
				int pageSelect = 1;
				String nameFilter = null;
				if (args.length == 2)
				{
					try
					{
						pageSelect = Integer.valueOf(args[1]).intValue();
					}
					catch (NumberFormatException ex)
					{
						nameFilter = args[1];
					}
				}
				if (args.length == 3)
				{
					nameFilter = args[1];
					try
					{
						pageSelect = Integer.valueOf(args[2]).intValue();
					}
					catch (NumberFormatException ex)
					{
						pageSelect = 1;
					}
				}
				ArrayList<MarketItem> list = plugin.db.list(pageSelect, nameFilter, shopLabel);
				ArrayList<MarketItem> listToCount = plugin.db.list(0, nameFilter, shopLabel);
				int numPages = (listToCount.size()/8 + (listToCount.size() % 8 > 0 ? 1 : 0));
				if (listToCount.isEmpty())
				{
					message.send(plugin.shop_tag + "{ERR}No items are set up in the shop yet...");
					return false;
				}
				if (pageSelect > numPages)
				{
					message.send(plugin.shop_tag + "{ERR}The shop only has " + numPages + " pages of items.");
					return false;
				}
				if (list.isEmpty())
				{
					message.send(plugin.shop_tag + "{ERR}Horrors! The page calculation made a mistake!");
					return false;
				}
				
				else {
					message.send("{}Shop Items: Page {BKT}[{PRM}" + pageSelect + "{BKT}]{} of {BKT}[{PRM}" + numPages + "{BKT}]");
					for (MarketItem data : list) {
						message.send(data.infoStringShort());
					}
				}
				return true;
			}
			
			// If this point is reached, no subcommand was matched...
			message.send("{ERR}Unknown or mangled shop command. Try {CMD}/shop help{ERR}.");
			return false;
		}
		
		// /shop not matched.
			return false;
		}
}
