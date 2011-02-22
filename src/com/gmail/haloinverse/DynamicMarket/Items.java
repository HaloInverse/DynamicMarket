package com.gmail.haloinverse.DynamicMarket;

//CHANGED: This was changed into an instanced class.
//CHANGED: Untyped items have a subtype (stored as damage) field = 0, NOT -1!
//         This was completely breaking attempts to support typed items, i.e. logs/dyes/wool.
//TODO: Consider moving purely player-inventory-related methods into another class.
//TODO: Change "validate*" methods to something more descriptive.
//TODO: Add MySQL/SQLite support, with import from flat-file.

/*     */ //package com.nijikokun.bukkit.SimpleShop;
/*     */ 
/*     */ import java.util.HashMap;
		  import java.util.Iterator;
		  import java.util.Map;
		  import org.bukkit.Material;
/*     */ import org.bukkit.entity.Player;
/*     */ import org.bukkit.inventory.ItemStack;
		  import org.bukkit.inventory.PlayerInventory;
/*     */ 
/*     */ public class Items
/*     */ {

			private HashMap<String,String> itemsData;
			private iProperty ItemsFile;
			private DynamicMarket plugin;
	
			public Items(String itemFileName, DynamicMarket thisPlugin)
			{
				//TODO: Handle cases where the plugin loads, but items.db doesn't.
				//TODO: Add user command for reloading items.db.
				//TODO: Make item file format more generic.
				this.plugin = thisPlugin;
				ItemsFile = new iProperty(itemFileName);
				// Following code originally in setupItems from SimpleShop
/* 169 */     	Map<String, String> mappedItems = null;
/* 170 */     	itemsData = new HashMap<String, String>();
/*     */     	try
/*     */     	{
/* 173 */       	mappedItems = ItemsFile.returnMap();
/*     */     	} catch (Exception ex) {
/* 175 */       	plugin.log.info(Messaging.bracketize(new StringBuilder().append(plugin.name).append(" Flatfile").toString()) + " could not grab item list!");
/*     */     	}
/*     */     	Iterator<String> i$;
/* 178 */     	if (mappedItems != null)
/* 179 */       for (i$ = mappedItems.keySet().iterator(); i$.hasNext(); ) { Object item = i$.next();
/* 180 */         	String id = (String)item;
/* 181 */         	String itemName = mappedItems.get(item);
/* 182 */         	itemsData.put(id, itemName);
/*     */       }
/*     */   }
	
/*     */   public String name(String idString)
			// Fetches the item name given an id string.
			// Subtype, if present, is in the input format ("[id],[subtype]")
/*     */   {
/*  22 */     if (itemsData.containsKey(idString)) {
/*  23 */       return ((String)itemsData.get(idString));
/*     */     }
			  if ((!(idString.contains(","))) && (itemsData.containsKey(idString+",0")))
				  return((String)itemsData.get(idString+",0"));
/*     */ 	  // Fallback: Fetch the name from the bukkit Material class.
/*  26 */     //for (Material item : Material.values()) {
/*  27 */     //  if (item.getId() == id) {
/*  28 */     //    return item.toString();
/*     */     //  }
/*     */     //}
/*     */ 
/*  32 */     //return Misc.string(id);
				return ("UNKNOWN");
/*     */   }

			public String name(ItemClump itemData)
			{
				// Fetches the item name given an ItemClump.
				return name(Integer.toString(itemData.itemId) + (itemData.subType != 0 ? "," + Integer.toString(itemData.subType) : ""));
			}

/*     */ 
/*     */   public void setName(String id, String name)
/*     */   {
/*  42 */     itemsData.put(id, name);
/*  43 */     ItemsFile.setString(id, name);
/*     */   }
/*     */ 

			public static boolean has(Player player, ItemClump scanItem, int numBundles)
			{
				return (hasAmount(player, scanItem) >= (scanItem.count * numBundles));
			}

			public static int hasAmount(Player player, ItemClump scanItem)
			{
				PlayerInventory inventory = player.getInventory();
				ItemStack[] items = inventory.getContents();
				int amount = 0;
		 
				for (ItemStack item : items) {
					if ((item != null) && (item.getTypeId() == scanItem.itemId) && (item.getDurability() == (byte)scanItem.subType)) {
						amount += item.getAmount();
					}
				}
				return amount;
     		}

			public static boolean has(Player player, int itemId, int itemType, int amount)
			{
				return (hasAmount(player, new ItemClump(itemId, itemType)) >= amount);
			}

			public void remove(Player player, ItemClump item, int amount)
			{
				// TODO: Clean this up once Bukkit's inventory.removeItem(ItemStack[]) code handles subtypes.
				// Note that this removes (item.count * amount) items from the player's inventory.
				// This is to streamline removing multiples of bundle counts.
				PlayerInventory inventory = player.getInventory();
				ItemStack[] items = inventory.getContents();
				ItemStack thisItem;
				int toRemove = item.count*amount;

				for (int invSlot = 35; (invSlot >= 0) && (toRemove > 0); --invSlot)
				{
					thisItem = items[invSlot];
					if ((items[invSlot] != null) && (thisItem.getTypeId() == item.itemId) && (thisItem.getDurability() == (byte)item.subType))
					{
						toRemove -= thisItem.getAmount();
						inventory.clear(invSlot);
					}
				}

				if (toRemove < 0) // removed too many! put some back!
					inventory.addItem(new ItemStack[] { new ItemStack(item.itemId, -toRemove, (byte)item.subType) });
			}
			
			public ItemClump nameLookup(String item)
			{
				// Given an item name (with optional ",<subtype>", returns an ItemClump loaded with the id and subtype.
				// If name is not found, returns null.

				int itemId = -1;
				int itemSubtype = 0;
				ItemClump returnedItem = null;

				for (String id : itemsData.keySet()) {
					if (((String)itemsData.get(id)).equalsIgnoreCase(item)) {
						if (id.contains(",")) {
							itemId = Integer.valueOf(id.split(",")[0]).intValue();
							itemSubtype = Integer.valueOf(id.split(",")[1]).intValue();
						}
						else {
							itemId = Integer.valueOf(id).intValue();
							itemSubtype = 0;
						}
						returnedItem = new ItemClump(itemId, itemSubtype);
						break;
					} 
				}
				
				if (returnedItem != null)
					return returnedItem;
				
				// No exact match found: Try partial-name matching.
				String itemLower = item.toLowerCase();
				for (String id : itemsData.keySet()) {
					if (((String)itemsData.get(id)).toLowerCase().contains(itemLower)) {
						if (id.contains(",")) {
							itemId = Integer.valueOf(id.split(",")[0]).intValue();
							itemSubtype = Integer.valueOf(id.split(",")[1]).intValue();
						}
						else {
							itemId = Integer.valueOf(id).intValue();
							itemSubtype = 0;
						}
						returnedItem = new ItemClump(itemId, itemSubtype);
						break;
					} 
				}
				
				return returnedItem;
			}
			

			public static boolean validateType(int id, int type)
			// Given an id and a subtype, confirms that the selected subtype is valid.
			//CHANGED: Rewritten as a switch block. Cleaner & faster.
			{
				if (type == 0) 
					return true;
				if (type < 0)
					return false; 

				switch (id)
				{
					case 35:
					case 63:
					case 351:
						return (type <= 15);
					case 17:
						return (type <= 2);
					case 53:
					case 64:
					case 67:
					case 71:
					case 77:
					case 86:
					case 91:
						return (type <= 3);
					case 66:
						return (type <= 9);
					case 68:
						return ((type >= 2) && (type <= 5));
					default:
						return false;
				}
			}


/*     */   public static boolean checkID(int id)
			// ? Confirms that the given id is known by bukkit as an item id.
/*     */   {
/* 289 */     for (Material item : Material.values()) {
/* 290 */       if (item.getId() == id) {
/* 291 */         return true;
/*     */       }
/*     */     }
/*     */ 
/* 295 */     return false;
/*     */   }
/*     */ }

/* Location:           C:\Program Files\eclipse\Bukkit\SimpleShop.jar
 * Qualified Name:     com.nijikokun.bukkit.SimpleShop.Items
 * Java Class Version: 5 (49.0)
 * JD-Core Version:    0.5.3
 */