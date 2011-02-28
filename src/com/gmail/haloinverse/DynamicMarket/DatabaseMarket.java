package com.gmail.haloinverse.DynamicMarket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

		  
public class DatabaseMarket extends DatabaseCore
{
 	//public Type database = null;
 	public Items itemsReference = null;

	public DatabaseMarket(Type database, String tableAccessed, Items itemsRef, String thisEngine, DynamicMarket pluginRef)
	{
		super(database, tableAccessed, thisEngine, pluginRef);  // default table name: "Market"
		checkNewFields();
		this.itemsReference = itemsRef;
	}
	
	@Deprecated
	public DatabaseMarket(Type database, String tableAccessed, String thisEngine, DynamicMarket pluginRef)
	{
		super(database, tableAccessed, thisEngine, pluginRef);
		this.itemsReference = null;
	}
	
	protected void checkColumn(String columnName, String columnDef)
	{
		SQLHandler myQuery = new SQLHandler(this);
		if (!myQuery.checkColumnExists(tableName, columnName))
		{
			myQuery.prepareStatement("ALTER TABLE " + tableName + " ADD ? ?");
			myQuery.inputList.add(columnName);
			myQuery.inputList.add(columnDef);
			myQuery.executeUpdates();
		}
	}
	
	protected void checkNewFields()
	{
		checkColumn("shoplabel", (this.database.equals(Type.SQLITE)?
				"TEXT NOT NULL DEFAULT ''; CREATE INDEX shoplabelIndex ON Market (shoplabel)"
				: "CHAR(20) NOT NULL DEFAULT ''; CREATE INDEX shopLabelIndex ON Market (shopLabel)"));
	}
	
	protected boolean createTable(String shopLabel)
	{
		SQLHandler myQuery = new SQLHandler(this);
		if (!myQuery.checkTable(tableName))
			createTable();
		else
			return false;
		return add(new MarketItem("-1,-1 n:Default", null, this, shopLabel));
	}
	
	protected boolean createTable() {
		SQLHandler myQuery = new SQLHandler(this);
		if (this.database.equals(Type.SQLITE))
			myQuery.executeStatement("CREATE TABLE " + tableName + " ( id INTEGER PRIMARY KEY AUTOINCREMENT, " +
					"item INT NOT NULL, " +
					"subtype INT NOT NULL, " +
					"name TEXT NOT NULL, " +
					"count INT NOT NULL, " +
					"baseprice INT NOT NULL, " +
					"canbuy INT NOT NULL, " +
					"cansell INT NOT NULL, " +
					"stock INT NOT NULL, " +
					"volatility INT NOT NULL, " +
					"salestax INT NOT NULL, " +
					"stockhighest INT NOT NULL, " +
					"stocklowest INT NOT NULL, " +
					"stockfloor INT NOT NULL, " +
					"stockceil INT NOT NULL, " +
					"pricefloor INT NOT NULL, " +
					"priceceil INT NOT NULL, " +
					"jitterperc INT NOT NULL, " +
					"driftout INT NOT NULL, " +
					"driftin INT NOT NULL, " +
					"avgstock INT NOT NULL, " +
					"class INT NOT NULL, " +
					"shoplabel TEXT NOT NULL DEFAULT '');" +
					"CREATE INDEX itemIndex ON Market (item);" +
					"CREATE INDEX subtypeIndex ON Market (subtype);" +
					"CREATE INDEX nameIndex ON Market (name);" +
					"CREATE INDEX shoplabelIndex ON Market (shoplabel)");
		else
			myQuery.executeStatement("CREATE TABLE " + tableName + " ( id INT( 255 ) NOT NULL AUTO_INCREMENT, " +
					"item INT NOT NULL, " +
					"subtype INT NOT NULL, " +
					"name CHAR(20) NOT NULL, " +
					"count INT NOT NULL, " +
					"baseprice INT NOT NULL, " +
					"stock INT NOT NULL, " +
					"canbuy INT NOT NULL, " +
					"cansell INT NOT NULL, " +
					"volatility INT NOT NULL, " +
					"salestax INT NOT NULL, " +
					"stocklowest INT NOT NULL, " +
					"stockhighest INT NOT NULL, " +
					"stockfloor INT NOT NULL, " +
					"stockceil INT NOT NULL, " +
					"pricefloor INT NOT NULL, " +
					"priceceil INT NOT NULL, " +
					"jitterperc INT NOT NULL, " +
					"driftout INT NOT NULL, " +
					"driftin INT NOT NULL, " +
					"avgstock INT NOT NULL, " +
					"class INT NOT NULL, " +
					"shoplabel CHAR(20) NOT NULL DEFAULT '', " +
					"PRIMARY KEY ( id ), INDEX ( item, subtype, name, shoplabel )) ENGINE = "+ engine + ";");
		myQuery.close();
		
		if (!myQuery.isOK)
			return false;
		
		// Add default record.
		
		return add(new MarketItem("-1,-1 n:Default", null, this, ""));
	}
	
	public boolean add(Object newItem)
	{
		if (newItem instanceof MarketItem)
			return add((MarketItem)newItem);
		return false;
	}
	
	public boolean add(MarketItem newItem) {
	  	if (hasRecord(newItem, newItem.shopLabel))
	  		return false;	// Don't add if a record already exists.

		SQLHandler myQuery = new SQLHandler(this);

		myQuery.inputList.add(newItem.itemId);
		myQuery.inputList.add(newItem.subType);
		myQuery.inputList.add(newItem.count);
		myQuery.inputList.add(newItem.getName());
		myQuery.inputList.add(newItem.basePrice);
		myQuery.inputList.add(newItem.stock);
		myQuery.inputList.add(newItem.canBuy? 1 : 0);
		myQuery.inputList.add(newItem.canSell? 1 : 0);
		myQuery.inputList.add(newItem.getVolatility());
		myQuery.inputList.add(newItem.salesTax);
		myQuery.inputList.add(newItem.stockLowest);
		myQuery.inputList.add(newItem.stockHighest);
		myQuery.inputList.add(newItem.stockFloor);
		myQuery.inputList.add(newItem.stockCeil);
		myQuery.inputList.add(newItem.priceFloor);
		myQuery.inputList.add(newItem.priceCeil);
		myQuery.inputList.add(newItem.shopLabel);
		
		myQuery.prepareStatement("INSERT INTO " + tableName + " (item, subtype, count, name, baseprice, stock, canbuy, cansell, volatility, " +
		"salestax, stocklowest, stockhighest, stockfloor, stockceil, pricefloor, priceceil, class, jitterperc, driftin, driftout, avgstock, shoplabel) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,0,0,0,0,0,?)");
		
		myQuery.executeUpdates();
		
		myQuery.close();
		return (myQuery.isOK);
	}
	
	public boolean update(MarketItem updated) {
		SQLHandler myQuery = new SQLHandler(this);

		myQuery.inputList.add(updated.count);
		myQuery.inputList.add(updated.getName());
		myQuery.inputList.add(updated.basePrice);
		myQuery.inputList.add(updated.stock);
		myQuery.inputList.add(updated.canBuy? 1 : 0);
		myQuery.inputList.add(updated.canSell? 1 : 0);
		myQuery.inputList.add(updated.getVolatility());
		myQuery.inputList.add(updated.salesTax);
		myQuery.inputList.add(updated.stockLowest);
		myQuery.inputList.add(updated.stockHighest);
		myQuery.inputList.add(updated.stockFloor);
		myQuery.inputList.add(updated.stockCeil);
		myQuery.inputList.add(updated.priceFloor);
		myQuery.inputList.add(updated.priceCeil);

		myQuery.inputList.add(updated.itemId);
		myQuery.inputList.add(updated.subType);
		myQuery.inputList.add(updated.shopLabel);
		
		myQuery.prepareStatement("UPDATE " + tableName + " SET count = ?, name = ?, baseprice = ?, stock = ?, canbuy = ?, cansell = ?, volatility = ?, " +
				"salestax = ?, stocklowest = ?, stockhighest = ?, stockfloor = ?, stockceil = ?, pricefloor = ?, priceceil = ? WHERE item = ? AND subtype = ? AND shoplabel = ? " + ((this.database.equals(Type.SQLITE)) ? "" : " LIMIT 1"));

		myQuery.executeUpdates();
		
		myQuery.close();
		return (myQuery.isOK);
	}
	
	@Deprecated
	public boolean update(Object updateRef)
	{
		if (updateRef instanceof MarketItem)
			return update((MarketItem)updateRef);
		return false;
	}
	
	@Deprecated
	public ArrayList<MarketItem> list(int pageNum)
	{
		return list(pageNum, null, "");
	}
	
	public ArrayList<MarketItem> list(int pageNum, String nameFilter, String shopLabel) {
		//CHANGED: This now spits out a list of MarketItems, instead of a list of arrays of ints.
	  //If pageNum=0, return the entire list.
		// Optionally filters by partial-string matching of names.

	  //TODO: Add option for name-sorting.
		SQLHandler myQuery = new SQLHandler(this);
		ArrayList<MarketItem> data = new ArrayList<MarketItem>();
	  	int startItem = 0;
	  	int numItems = 9999999;
	  	MarketItem newItem;
	  	if (pageNum > 0)
	  	{
	  		startItem = (pageNum - 1) * 8;
	  		numItems = 8;
	  	}
	  
	  	if ((nameFilter == null) || (nameFilter.isEmpty()))
	  	{
	  		myQuery.inputList.add(shopLabel);		
	  		myQuery.inputList.add(startItem);
	  		myQuery.inputList.add(numItems);
	  		myQuery.prepareStatement("SELECT * FROM " + tableName + " WHERE (item >= 0 AND shoplabel = ?) ORDER BY item ASC, subtype ASC LIMIT ?, ?");
	  	}
	  	else
	  	{
	  		myQuery.inputList.add("%" + nameFilter + "%");
	  		myQuery.inputList.add(shopLabel);
	  		myQuery.inputList.add(startItem);
	  		myQuery.inputList.add(numItems);
	  		myQuery.prepareStatement("SELECT * FROM " + tableName + " WHERE (item >= 0 AND name LIKE ? AND shoplabel = ?) ORDER BY item ASC, subtype ASC LIMIT ?, ?");
	  	}	  	
		
		myQuery.executeQuery();
		
		if (myQuery.isOK)
			try {
				while (myQuery.rs.next())
				{
					//data.add(new ShopItem(myQuery.rs.getInt("item"),myQuery.rs.getInt("type"), myQuery.rs.getInt("buy"), myQuery.rs.getInt("sell"), myQuery.rs.getInt("per") ));
					newItem = new MarketItem(myQuery);
					newItem.thisDatabase = this;
					data.add(newItem);
				}
			} catch (SQLException ex) {
				logSevereException("SQL Error during ArrayList fetch: " + dbTypeString(), ex);
			}
		
		myQuery.close();

		return data;
	}
	
	@Deprecated
	public MarketItem data(ItemClump thisItem)
	{
		return data(thisItem, "");
	}
	
	public MarketItem data(ItemClump thisItem, String shopLabel) {
	  //CHANGED: Returns MarketItems now.
		SQLHandler myQuery = new SQLHandler(this);
		MarketItem fetchedData = null;

		myQuery.inputList.add(thisItem.itemId);
		myQuery.inputList.add(thisItem.subType);
  		myQuery.inputList.add(shopLabel);
		myQuery.prepareStatement("SELECT * FROM " + tableName + " WHERE (item = ? AND subtype = ? AND shoplabel = ?) LIMIT 1");

		myQuery.executeQuery();

		try {
			if (myQuery.rs != null)
				if (myQuery.rs.next())
					//data = new ShopItem(myQuery.rs.getInt("item"), myQuery.rs.getInt("type"), myQuery.rs.getInt("buy"), myQuery.rs.getInt("sell"), myQuery.rs.getInt("per"));
					fetchedData = new MarketItem(myQuery);
					fetchedData.shopLabel = shopLabel;
					fetchedData.thisDatabase = this;
					// TODO: Change constructor to take a ResultSet and throw SQLExceptions.
		} catch (SQLException ex) {
			logSevereException("Error retrieving shop item data with " + dbTypeString(), ex);
			fetchedData = null;
		}
		
		// Temp until constructor throws SQLExceptions
		if (!(myQuery.isOK))
			fetchedData = null;
		
		myQuery.close();

		//if (data == null) { data = new MarketItem(); }
		//Return null if no matching data found.
		
		return fetchedData;
	}
	
	public boolean hasRecord(MarketItem thisItem)
	{
		return hasRecord((ItemClump)thisItem, thisItem.shopLabel);
	}
	
	public boolean hasRecord(ItemClump thisItem, String shopLabel)
	{
		// Checks if a given ItemClump has a database entry.
		SQLHandler myQuery = new SQLHandler(this);
		boolean returnVal = false;

		myQuery.inputList.add(thisItem.itemId);
		myQuery.inputList.add(thisItem.subType);
  		myQuery.inputList.add(shopLabel);
		myQuery.prepareStatement("SELECT * FROM " + tableName + " WHERE (item = ? AND subtype = ? AND shoplabel = ?) LIMIT 1");

		myQuery.executeQuery();
		
		try {
			if (myQuery.rs != null)
				if (myQuery.rs.next())
					returnVal = true;
		} catch (SQLException ex) {
			logSevereException("Error retrieving shop item data with " + dbTypeString(), ex);
		}
		
		return returnVal;
	}
		
	public boolean addStock(ItemClump thisItem, String shopLabel)
	{
		SQLHandler myQuery = new SQLHandler(this);

		myQuery.inputList.add(thisItem.count);
		myQuery.inputList.add(thisItem.itemId);
		myQuery.inputList.add(thisItem.subType);
  		myQuery.inputList.add(shopLabel);
		if (this.database.equals(Type.SQLITE))
			myQuery.prepareStatement("UPDATE " + tableName + " SET stock = min(stock + ?, stockceil) WHERE (item = ? AND subtype = ? AND shoplabel = ?) " + ((this.database.equals(Type.SQLITE)) ? "" : " LIMIT 1"));
		else
			myQuery.prepareStatement("UPDATE " + tableName + " SET stock = LEAST(stock + ?, stockceil) WHERE (item = ? AND subtype = ? AND shoplabel = ?) " + ((this.database.equals(Type.SQLITE)) ? "" : " LIMIT 1"));
			

		myQuery.executeUpdates();
		
		myQuery.close();

		return myQuery.isOK;
	}
	
	public boolean removeStock(ItemClump thisItem, String shopLabel)
	{
		SQLHandler myQuery = new SQLHandler(this);

		myQuery.inputList.add(thisItem.count);
		myQuery.inputList.add(thisItem.itemId);
		myQuery.inputList.add(thisItem.subType);
  		myQuery.inputList.add(shopLabel);
		if (this.database.equals(Type.SQLITE))
			myQuery.prepareStatement("UPDATE " + tableName + " SET stock = max(stock - ?, stockfloor) WHERE (item = ? AND subtype = ? AND shoplabel = ?) " + ((this.database.equals(Type.SQLITE)) ? "" : " LIMIT 1"));
		else
			myQuery.prepareStatement("UPDATE " + tableName + " SET stock = GREATEST(stock - ?, stockfloor) WHERE (item = ? AND subtype = ? AND shoplabel = ?) " + ((this.database.equals(Type.SQLITE)) ? "" : " LIMIT 1"));
		
		myQuery.executeUpdates();
		
		myQuery.close();

		return myQuery.isOK;
	}
	
	public MarketItem getDefault(String shopLabel)
	{
		return data(new ItemClump(-1, -1), shopLabel);
	}
	
	public ItemClump nameLookup(String searchName, String shopLabel)
	{
		// Parses a name string into an ItemClump with referenced itemId, subtype
		// If name lookup fails, returns null
		// Tries market database names, and if it fails, tries itemsReference.
		ItemClump returnData = null;
		
		if (searchName.equalsIgnoreCase("default"))
			return (new ItemClump(-1,-1));
		
		SQLHandler myQuery = new SQLHandler(this);

		myQuery.inputList.add(searchName);
  		myQuery.inputList.add(shopLabel);
		myQuery.prepareStatement("SELECT item, subtype FROM " + tableName + " WHERE (name = ? AND shopLabel = ?) LIMIT 1");

		myQuery.executeQuery();

		try
		{
			if (myQuery.rs != null)
				if (myQuery.rs.next())
					returnData = new ItemClump(myQuery.rs.getInt("item"), myQuery.rs.getInt("subtype"));
		} catch (SQLException ex) {
			logSevereException("Error retrieving item name with " + dbTypeString(), ex);
			returnData = null;
		}
		
		// Temp until constructor throws SQLExceptions
		if (!(myQuery.isOK))
			returnData = null;
		
		myQuery.close();
		
		if (returnData != null)
			return returnData;
		
		// If straight lookup failed, try partial lookup.
		myQuery = new SQLHandler(this);

		myQuery.inputList.add("%" + searchName + "%");
  		myQuery.inputList.add(shopLabel);
		myQuery.prepareStatement("SELECT item, subtype FROM " + tableName + " WHERE (name LIKE ? AND shoplabel = ?) LIMIT 1");

		myQuery.executeQuery();

		try
		{
			if (myQuery.rs != null)
				if (myQuery.rs.next())
					returnData = new ItemClump(myQuery.rs.getInt("item"), myQuery.rs.getInt("subtype"));
		} catch (SQLException ex) {
			logSevereException("Error retrieving item name with " + dbTypeString(), ex);
			returnData = null;
		}
		
		// Temp until constructor throws SQLExceptions
		if (!(myQuery.isOK))
			returnData = null;
		
		myQuery.close();
		
		// On lookup failure, try the Items flatfile.
		if (returnData == null)
			if (itemsReference != null)
				returnData = itemsReference.nameLookup(searchName);

		return returnData;
	}
	
	public String getName(ItemClump itemData, String shopLabel)
	{
		String returnedName = null;
		SQLHandler myQuery = new SQLHandler(this);
		
		myQuery.inputList.add(itemData.itemId);
		myQuery.inputList.add(itemData.subType);
  		myQuery.inputList.add(shopLabel);

		myQuery.prepareStatement("SELECT name FROM " + tableName + " WHERE (item = ? AND subtype = ? AND shoplabel = ?) LIMIT 1");
		myQuery.executeQuery();

		try
		{
			if (myQuery.rs != null)
				if (myQuery.rs.next())
					returnedName = myQuery.getString("name");
		} catch (SQLException ex) {
			logSevereException("Error retrieving item name with " + dbTypeString(), ex);
			returnedName = null;
		}
		
		myQuery.close();
		
		if (returnedName == null)
			returnedName = itemsReference.name(itemData);
		
		if ((returnedName == null) && (itemData.isDefault()))
			returnedName = "DEFAULT";
		
		if (returnedName == null)
			return "UNKNOWN";
		
		return returnedName;
		
	}
	
	@Deprecated
	public boolean remove(ItemClump removed)
	{
		return remove(removed, "");
	}
	
	public boolean remove(ItemClump removed, String shopLabel) {
		  // CHANGED: Now accepts an itemType (through use of the ItemClump class). 
			SQLHandler myQuery = new SQLHandler(this);

			myQuery.inputList.add(removed.itemId);
			myQuery.inputList.add(removed.subType);
	  		myQuery.inputList.add(shopLabel);
			myQuery.prepareStatement("DELETE FROM " + tableName + " WHERE (item = ? AND subtype = ? AND shoplabel = ?) " + ((this.database.equals(Type.SQLITE)) ? "" : " LIMIT 1"));

			myQuery.executeUpdates();
			
			myQuery.close();
			return myQuery.isOK;
		}

	public boolean dumpToCSV(String fileName, String shopLabel)
	{
		// Dumps this database into a .csv file.
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(fileName, false));
		} catch (IOException ex) {
			logSevereException("Error opening .csv for writing: " + fileName, ex);
			return false;
		}

		String line;
		
		line = MarketItem.csvHeaderLine(); 
		try {
			writer.write(line);
		} catch (IOException ex) {
			logSevereException("I/O error writing .csv header:" + fileName, ex);
			return false;
		}
		
		ArrayList<MarketItem> itemsToWrite = list(0, null, shopLabel);
		
		for (MarketItem thisItem : itemsToWrite)
		{
			// Write a line.
			line = thisItem.csvLine();
			try {
				writer.newLine();
				writer.write(line);
			} catch (IOException ex) {
				logSevereException("Error writing output line to .csv: " + fileName, ex);
				return false;
			}
		}
		try {
			writer.flush();
		} catch (IOException ex) {
			logSevereException("Error flushing output after writing .csv:" + fileName, ex);
		}
		try {
			writer.close();
		} catch (IOException ex) {
			logSevereException("Error closing file after writing .csv:" + fileName, ex);
		}
		return true;
	}
	
	public boolean inhaleFromCSV(String fileName, String shopLabel)
	{
		// Sucks a .csv file into this database.
		BufferedReader reader;
		MarketItem importItem;
		try {
			reader = new BufferedReader(new FileReader(fileName));
		} catch (FileNotFoundException ex) {
			logSevereException("File not found while importing .csv: " + fileName, ex);
			return false;
		}

		String line;
		try {
			line = reader.readLine();
			//if (!line.equalsIgnoreCase(MarketItem.csvHeaderLine()))
			//{
			//	plugin.log.severe("[" + plugin.name + "]: Bad header line reading .csv: " + fileName);
			//	return false;
			//}
			line = reader.readLine();
		} catch (IOException ex) {
			logSevereException("I/O error importing .csv:" + fileName, ex);
			return false;
		}
		while (line != null)
		{
			if (line.trim().length() == 0)
			{
				continue;
			}
			// Parse a line.
			//plugin.log.info(line);
			line = line.replace("'","").replace("\"","");
			importItem = new MarketItem(line, null, shopLabel, true);
			if (hasRecord(importItem, shopLabel))
			{
				importItem = new MarketItem(line, data(importItem, shopLabel), shopLabel, true);
				update(importItem);
			}
			else
			{
				add(importItem);
			}
			try {
				line = reader.readLine();
			} catch (IOException ex) {
				logSevereException("I/O error reading .csv:" + fileName, ex);
				break;
			}
		}
		try {
			reader.close();
		} catch (IOException ex) {
			logSevereException("Error closing file after importing .csv: " + fileName, ex);
		}
		return true;
	}

	private void updateAll(SQLHandler myQuery, String fieldName, Object newValue, String shopLabel)
	{
		// Update fieldName to newValue in all records matching shopLabel.
		myQuery.inputList.add(newValue);
		myQuery.inputList.add(shopLabel);
		myQuery.prepareStatement("UPDATE " + tableName + " SET " + fieldName + " = ? WHERE shoplabel = ?");
	}
	
	private void updateAllExpr(SQLHandler myQuery, String fieldName, String updExpr, String shopLabel)
	{
		// Update fieldName to expression updExpr in all records matching shopLabel.
		myQuery.inputList.add(shopLabel);
		myQuery.prepareStatement("UPDATE " + tableName + " SET " + fieldName + " = " + updExpr + " WHERE shoplabel = ?");
	}
	
	private void updateAllExprWhere(SQLHandler myQuery, String fieldName, String updExpr, String whereClause, String shopLabel)
	{
		// Update fieldName to expression updExpr in all records matching whereClause and shopLabel.
		myQuery.inputList.add(shopLabel);
		myQuery.prepareStatement("UPDATE " + tableName + " SET " + fieldName + " = " + updExpr + " WHERE (shoplabel = ? AND " + whereClause + ")");
	}
	
	public boolean updateAllFromTags(String initData[], String shopLabel)
	{
		// Update table data from a list of tags.
		// Applies changes to every element of the table matching shopLabel.
		// Mirrors MarketItem.parseTags.
		// Element [0] is used only to extract count, if provided.
		String curTag;
		Integer curVal;
		String stringVal;
		String[] curParams;
		boolean setUntaggedBase = false;
		SQLHandler myQuery = new SQLHandler(this);
		
		if (initData[0].contains(":"))
		{
			// Count detected. Pull it out, if it's valid.
			ItemClump countCheck = new ItemClump(initData[0], null, "");
			// Update all counts, rescaling stock levels and base prices.
			//basePrice = Math.round((float)prev.basePrice * new.count / prev.count);
			//stockItems = stock * count;
			//new.stockItems = prev.stockItems;
			// new.stock * new.count = prev.stock * old.count;
			// new.stock = prev.stock * prev.count / new.count;
			updateAllExpr(myQuery, "baseprice", "ROUND(baseprice * " + countCheck.count + " / count, 0)", shopLabel);
			updateAllExpr(myQuery, "stock", "ROUND((stock * count / " + countCheck.count + ")-0.5,0)", shopLabel);
			updateAll(myQuery, "count", countCheck.count, shopLabel);
		}
		
		if (initData.length > 1)
		{
			for (int i = 1; i <= initData.length - 1; i++)
			{
				stringVal = null;
				// Get current tag and value

				if (initData[i].contains(":"))
				{
					curParams = initData[i].split(":");
					curTag = curParams[0];
					try
					{
						curVal = Integer.parseInt(curParams[1]);
					}
					catch (NumberFormatException ex)
					{
						if (curParams[1].equalsIgnoreCase("+INF"))
							curVal = Integer.MAX_VALUE;
						else if (curParams[1].equalsIgnoreCase("-INF"))
							curVal = Integer.MIN_VALUE;
						else
						{
							curVal = null;
							stringVal = curParams[1];
						}
					}
				}
				else
				{
					try
					{	// Try to parse it as a plain integer.
						curTag = null;
						curVal = Integer.parseInt(initData[i]);
					}
					catch (NumberFormatException ex)
					{	// Didn't work? Just use it as a string.
						curVal = null;
						curTag = initData[i];
					}
				}
				
				// If first param is an untagged value, make it the basePrice.
				if (i == 1)
				{
					if (curTag == null)
					{
						if (curVal != -1)
						{
							//this.basePrice = curVal;
							updateAll(myQuery, "baseprice", curVal, shopLabel);
							//maskData.basePrice = 1;
						}
						else
						{
							// Base price set to -1. Disable buying.
							//this.basePrice = 0;
							updateAll(myQuery, "baseprice", 0, shopLabel);
							//maskData.basePrice = 1;
							//this.canBuy = false;
							updateAll(myQuery, "canbuy", 0, shopLabel);
						}
						setUntaggedBase = true;
						continue;
					}
				}
				
				// If second param is an untagged value, make the sellPrice equal to it after sales tax is applied. 
				if (i == 2)
				{
					if ((setUntaggedBase) && (curTag == null))
					{
						if (curVal != -1)
						{
							// if salePrice = basePrice * (1 - (salesTax / 100))
							// then (1 - (salePrice / basePrice)) * 100 = salesTax
							/*
							if (this.basePrice != 0)
							{
								this.salesTax = Math.round((1 - ((float)curVal / basePrice)) * 100);
								//maskData.salesTax = 1;
							}
							else
							{
								this.basePrice = curVal;
								this.salesTax = 0;
								//maskData.salesTax = 1;
							}
							*/
							updateAllExprWhere(myQuery, "salestax", "ROUND((1 - (" + curVal + " / baseprice)) * 100)", "baseprice > 0", shopLabel);
							updateAllExprWhere(myQuery, "salestax", "0", "baseprice = 0", shopLabel);
							updateAllExprWhere(myQuery, "baseprice", curVal.toString(), "baseprice = 0", shopLabel);
						}
						else
						{
							// Sale price set to -1. Disable selling.
							//this.canSell = false;
							updateAll(myQuery, "cansell", 0, shopLabel);
						}
						continue;
					}
				}
				
				// Handle remaining items in split.
				
				if (curTag != null)
				{
					if (curVal != null)
					{
						if (curTag.equalsIgnoreCase("bp") || curTag.equalsIgnoreCase("baseprice"))
						{
							//this.basePrice = curVal;
							updateAll(myQuery, "baseprice", curVal, shopLabel);
							continue;
						}
						if (curTag.equalsIgnoreCase("s") || curTag.equalsIgnoreCase("stock"))
						{
							//this.stock = curVal;
							updateAll(myQuery, "stock", curVal, shopLabel);
							continue;
						}			
						if (curTag.equalsIgnoreCase("v") || curTag.equalsIgnoreCase("volatility") || curTag.equalsIgnoreCase("vol"))
						{
							//this.setVolatility(curVal);
							updateAll(myQuery, "volatility", curVal, shopLabel);
							continue;
						}
						if (curTag.equalsIgnoreCase("iv") || curTag.equalsIgnoreCase("invvolatility") || curTag.equalsIgnoreCase("ivol"))
						{
							//this.setInverseVolatility(curVal);
							updateAll(myQuery, "volatility", MarketItem.iVolToVol(curVal), shopLabel);
							continue;
						}				
						if (curTag.equalsIgnoreCase("st") || curTag.equalsIgnoreCase("salestax"))
						{
							//this.salesTax = rangeCrop(curVal, 0, 100);
							updateAll(myQuery, "salestax", MarketItem.rangeCrop(curVal, 0, 100), shopLabel);
							continue;
						}
						if (curTag.equalsIgnoreCase("sl") || curTag.equalsIgnoreCase("stocklowest"))
						{
							//this.stockLowest = curVal;
							updateAll(myQuery, "stocklowest", curVal, shopLabel);
							continue;
						}
						if (curTag.equalsIgnoreCase("sh") || curTag.equalsIgnoreCase("stockhighest"))
						{
							//this.stockHighest = curVal;
							updateAll(myQuery, "stockhighest", curVal, shopLabel);
							continue;
						}
						if (curTag.equalsIgnoreCase("sf") || curTag.equalsIgnoreCase("stockfloor"))
						{
							//this.stockFloor = curVal;
							updateAll(myQuery, "stockfloor", curVal, shopLabel);
							continue;
						}
						if (curTag.equalsIgnoreCase("sc") || curTag.equalsIgnoreCase("stockceiling"))
						{
							//this.stockCeil = curVal;
							updateAll(myQuery, "stockceil", curVal, shopLabel);
							continue;
						}
						if (curTag.equalsIgnoreCase("pf") || curTag.equalsIgnoreCase("pricefloor"))
						{
							//this.priceFloor = curVal;
							updateAll(myQuery, "pricefloor", curVal, shopLabel);
							continue;
						}
						if (curTag.equalsIgnoreCase("pc") || curTag.equalsIgnoreCase("priceceiling"))
						{
							//this.priceCeil = curVal;
							updateAll(myQuery, "priceceil", curVal, shopLabel);
							continue;
						}
					}
				}
				if (curTag.equalsIgnoreCase("flat"))
				{
					//this.stock = 0;
					updateAll(myQuery, "stock", 0, shopLabel);
					//this.stockLowest = Integer.MIN_VALUE;
					updateAll(myQuery, "stocklowest", Integer.MIN_VALUE, shopLabel);
					//this.stockHighest = Integer.MAX_VALUE;
					updateAll(myQuery, "stockhighest", Integer.MAX_VALUE, shopLabel);
					//this.stockFloor = Integer.MIN_VALUE;
					updateAll(myQuery, "stockfloor", Integer.MIN_VALUE, shopLabel);
					//this.stockCeil = Integer.MAX_VALUE;
					updateAll(myQuery, "stockceil", Integer.MAX_VALUE, shopLabel);
					//this.priceFloor = 0;
					updateAll(myQuery, "pricefloor", 0, shopLabel);
					//this.priceCeil = Integer.MAX_VALUE;
					updateAll(myQuery, "priceceil", Integer.MAX_VALUE, shopLabel);
					//this.volatility = 0;
					updateAll(myQuery, "volatility", 0, shopLabel);
					continue;
				}
				if (curTag.equalsIgnoreCase("fixed"))
				{
					//this.stock = 0;
					updateAll(myQuery, "stock", 0, shopLabel);
					//this.stockLowest = Integer.MIN_VALUE;
					updateAll(myQuery, "stocklowest", Integer.MIN_VALUE, shopLabel);
					//this.stockHighest = Integer.MAX_VALUE;
					updateAll(myQuery, "stockhighest", Integer.MAX_VALUE, shopLabel);
					//this.stockFloor = 0;
					updateAll(myQuery, "stockfloor", 0, shopLabel);
					//this.stockCeil = 0;
					updateAll(myQuery, "stockceil", 0, shopLabel);
					//this.priceFloor = 0;
					updateAll(myQuery, "pricefloor", 0, shopLabel);
					//this.priceCeil = Integer.MAX_VALUE;
					updateAll(myQuery, "priceceil", Integer.MAX_VALUE, shopLabel);
					//this.volatility = 0;
					updateAll(myQuery, "volatility", 0, shopLabel);
					continue;
				}
				if (curTag.equalsIgnoreCase("float"))
				{
					//this.stockFloor = Integer.MIN_VALUE;
					updateAll(myQuery, "stockfloor", Integer.MIN_VALUE, shopLabel);
					//this.stockCeil = Integer.MAX_VALUE;
					updateAll(myQuery, "stockceil", Integer.MAX_VALUE, shopLabel);
					//this.stockLowest = Integer.MIN_VALUE;
					updateAll(myQuery, "stocklowest", Integer.MIN_VALUE, shopLabel);
					//this.stockHighest = Integer.MAX_VALUE;
					updateAll(myQuery, "stockhighest", Integer.MAX_VALUE, shopLabel);
					//this.priceFloor = 0;
					updateAll(myQuery, "pricefloor", 0, shopLabel);
					//this.priceCeil = Integer.MAX_VALUE;
					updateAll(myQuery, "priceceil", Integer.MAX_VALUE, shopLabel);
					//if (this.volatility == 0)
					//	this.volatility = 100;
					updateAllExprWhere(myQuery, "volatility", "100", "volatility = 0", shopLabel);
				}
				if (curTag.equalsIgnoreCase("finite"))
				{
					//this.stockFloor = Integer.MIN_VALUE;
					updateAll(myQuery, "stockfloor", Integer.MIN_VALUE, shopLabel);
					//this.stockCeil = Integer.MAX_VALUE;
					updateAll(myQuery, "stockceil", Integer.MAX_VALUE, shopLabel);
					//this.stockLowest = 0;
					updateAll(myQuery, "stocklowest", 0, shopLabel);
					//this.stockHighest = Integer.MAX_VALUE;
					updateAll(myQuery, "stockhighest", Integer.MAX_VALUE, shopLabel);
				}
				if ((curTag.equalsIgnoreCase("buyok")) || (curTag.equalsIgnoreCase("!nobuy")))
				{
					//this.canBuy = true;
					updateAll(myQuery, "canbuy", 1, shopLabel);
					continue;
				}
				if ((curTag.equalsIgnoreCase("sellok")) || (curTag.equalsIgnoreCase("!nosell")))
				{
					//this.canSell = true;
					updateAll(myQuery, "cansell", 1, shopLabel);
					continue;
				}
				if ((curTag.equalsIgnoreCase("!buyok")) || (curTag.equalsIgnoreCase("nobuy")) || (curTag.equalsIgnoreCase("!cb")))
				{
					//this.canBuy = false;
					updateAll(myQuery, "canbuy", 0, shopLabel);
					continue;
				}
				if ((curTag.equalsIgnoreCase("!sellok")) || (curTag.equalsIgnoreCase("nosell")) || (curTag.equalsIgnoreCase("!cs")))
				{
					//this.canSell = false;
					updateAll(myQuery, "cansell", 0, shopLabel);
					continue;
				}
				if (curTag.equalsIgnoreCase("cb") || curTag.equalsIgnoreCase("canbuy"))
				{
					if ((stringVal != null) && (!(stringVal.isEmpty())))
					{
						//this.canBuy = ((stringVal.toLowerCase().startsWith("y")) || (stringVal.toLowerCase().startsWith("t")));
						if ((stringVal.toLowerCase().startsWith("y")) || (stringVal.toLowerCase().startsWith("t")))
							updateAll(myQuery, "canbuy", 1, shopLabel);
						else
							updateAll(myQuery, "canbuy", 0, shopLabel);
					}
					else
						//this.canBuy = true;
						updateAll(myQuery, "canbuy", 1, shopLabel);
					continue;
				}
				if (curTag.equalsIgnoreCase("cs") || curTag.equalsIgnoreCase("cansell"))
				{
					if ((stringVal != null) && (!(stringVal.isEmpty())))
					{
						//this.canSell = ((stringVal.toLowerCase().startsWith("y")) || (stringVal.toLowerCase().startsWith("t")));
						if ((stringVal.toLowerCase().startsWith("y")) || (stringVal.toLowerCase().startsWith("t")))
							updateAll(myQuery, "cansell", 1, shopLabel);
						else
							updateAll(myQuery, "cansell", 0, shopLabel);
					}
					else
						//this.canSell = true;
						updateAll(myQuery, "cansell", 1, shopLabel);
					continue;
				}
				/*
				// Renaming everything to the same value doesn't make sense.
				if ((curTag.equalsIgnoreCase("name")) || (curTag.equalsIgnoreCase("n")))
				{
					//setName(stringVal);
					updateAll("name", stringVal, shopLabel);
					continue;
				}
				*/
				if (curTag.equalsIgnoreCase("renorm"))
				{				
					//double oldBuyPrice = getStockPrice(this.stock);
					//getStockPrice(stockLevel) = rangeCrop(this.basePrice * Math.pow(getVolFactor(), -rangeCrop(stockLevel, stockFloor, stockCeil)), priceFloor, priceCeil)
					//getVolFactor = (1 + volatility/intScale)
					//rangeCrop(double value, double minVal, double maxVal) = (Math.min(Math.max(value, minVal), maxVal))
					// oldBuyPrice = LEAST(GREATEST(baseprice * POW((1 + volatility/INTSCALE), -LEAST(GREATEST(stock, stockfloor), stockceil)), pricefloor), priceceil)
					int newStock;
					if (curVal == null)
						newStock = 0;
					else
						newStock = curVal;
					//this.basePrice = ROUND(oldBuyPrice / POW(getVolFactor(), -rangeCrop(this.stock, stockFloor, stockCeil)));
					//this.basePrice = ROUND(oldBuyPrice / POW((1 + volatility/INTSCALE), -LEAST(GREATEST(stock, stockFloor), stockCeil)));
					//this.basePrice = ROUND(LEAST(GREATEST(baseprice * POW((1 + volatility/INTSCALE), -LEAST(GREATEST(stock, stockfloor), stockceil)), pricefloor), priceceil) / POW((1 + volatility/INTSCALE), -LEAST(GREATEST(stock, stockFloor), stockCeil)));
					//blerg! all this to do the update within a single MySQL statement!
					//And it's not even translatable to SQLite!
					//Maybe use this later. Simple & crude method for right now.
					
					// This is ugly and nasty, but necessary for SQLite compatibility.
					// SQLite doesn't understand ^, POW(), ln(), or EXP() without a tricky-to-install add-on.
					ArrayList<MarketItem> itemsToEdit = list(0, null, shopLabel);
					if (!itemsToEdit.isEmpty())
					{
						myQuery.prepareBatchStatement("UPDATE " + tableName + " SET basePrice = ? WHERE (shoplabel = ? AND item = ? AND subtype = ?");
						for (MarketItem thisItem : itemsToEdit)
						{
							myQuery.inputList.add(Integer.toString(thisItem.getRenormedPrice(newStock)));
							myQuery.inputList.add(shopLabel);
							myQuery.inputList.add(thisItem.itemId);
							myQuery.inputList.add(thisItem.subType);
							myQuery.addToBatch();
						}
						updateAll(myQuery, "stock", newStock, shopLabel);
					}
				}
			}
			//TODO: Log and report invalid tags somehow.
		}
		// Finished parsing tags. Do the update.
		
		myQuery.executeUpdates();
		
		myQuery.close();

		return myQuery.isOK;
		
	}

	
	/*
	private boolean updateAll(String[] fieldName, Object[] newValue, String shopLabel)
	{
		// Update several fieldNames to newValues in all records matching shopLabel.
		return false;
	}
	*/
}
