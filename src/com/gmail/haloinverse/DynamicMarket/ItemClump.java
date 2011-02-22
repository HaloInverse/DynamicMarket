package com.gmail.haloinverse.DynamicMarket;

public class ItemClump {

	// Implemented because bukkit's ItemStack methods don't handle item subtypes consistently.
	
	// Added in to be a convenient package for grabbing entire items worth of shop data at once,
	// instead of one int at at time or in an easily-confused array.
	// Also factorizes in some redundant string-parsing code, making it easier to change standard input formats
	// if necessary (and shrinking the compiled .jar significantly)
		public int itemId; 	// id of item
		public int subType; 	// Subtype of item (i.e. dye colour); 0 if undamaged single-typed item.
		public int count;		// Number of items in this clump.
		
		public ItemClump() 
		{
			// Default constructor with no parameters.
			itemId = -1;
			subType = 0;
			count = 1;
		}
		
		public ItemClump(int newId, int newType, int newCount)
		{
			itemId = newId;
			subType = newType;
			count = newCount;
		}
		
		public ItemClump(int newId, int newType)
		{
			itemId = newId;
			subType = newType;
			count = 1;
		}
		
		public ItemClump(String initString, DatabaseMarket namesDB, String shopLabel)
		{
			//Valid input string formats:
			//"[ID(,Type)](:Count) (ignored)"
			//"[ItemName](:Count) (ignored)"
			itemId = -1;
			subType = 0;
			count = 1;
			String[] idData;
			String[] initData = initString.split(" ");
			boolean subtypeParsed = false;
			 
			if (initData[0].contains(":"))
			{
				// Count detected. Pull it out, if it's valid.
				idData = initData[0].split(":");
				try
				{
					count = Integer.valueOf(idData[1]).intValue();
				}
				catch (NumberFormatException e)
				{
					count = 1;
				}
				initData[0] = idData[0];
			}
			
			if (initData[0].contains(","))
			{
				// Subtype detected. Pull it out, if it's valid.
				idData = initData[0].split(",");
				try
				{
					subType = Integer.valueOf(idData[1]).intValue();
					subtypeParsed = true;
				}
				catch (NumberFormatException e)
				{
					subType = 0;
				}
				initData[0] = idData[0];
			}
			
			// Parse stripped item name, if possible.

			try
			{
			  itemId = Integer.valueOf(initData[0]).intValue();
			} catch (NumberFormatException ex) {
				// It's trying to be a name. Find it.
				if (initData[0].equalsIgnoreCase("default"))
				{
					itemId = -1;
					subType = -1;
				}
				else
				{
					ItemClump foundItem = namesDB.nameLookup(initData[0], shopLabel);
					if (foundItem != null)
					{
						itemId = foundItem.itemId;
						if (!subtypeParsed)
							subType = foundItem.subType;
					}
				}
			}
			
			if (count < 1) count = 1;
		}
		
		public String idString() {
			return String.valueOf(itemId) + (subType == 0 ? "" : "," + String.valueOf(subType));
		}
		
		public String getName(DatabaseMarket namesDB, String shopLabel) 
		{
			// Returns the name of this item.
			// Really ought to use DatabaseMarket for name lookups...
			return namesDB.getName(this, shopLabel);
		}
		
		public boolean isValid()
		{
			if ((itemId != -1) && (subType != -1))
				return true;
			if (isDefault())
				return true;
			return false;
		}
		
		public boolean isDefault()
		{
			if ((itemId == -1) && (subType == -1))
				return true;
			return false;
		}
		
	}
