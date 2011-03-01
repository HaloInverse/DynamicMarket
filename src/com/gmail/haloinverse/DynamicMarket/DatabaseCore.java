package com.gmail.haloinverse.DynamicMarket;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

		  
public abstract class DatabaseCore
{
		public Type databaseType = null;
		public String tableName; // default: SimpleMarket
		public DynamicMarket plugin = null;
		public String engine = "MyISAM";

		public DatabaseCore(Type databaseType, String tableAccessed, String thisEngine, DynamicMarket thisPlugin) {
     		this.databaseType = databaseType;
	  		this.tableName = tableAccessed;
	  		if (thisEngine != null)
	  			engine = thisEngine;
	  		plugin = thisPlugin;
	  		initialize();
		}
 
		protected boolean initialize()
		{
			return initialize("");
		}
		
		protected boolean initialize(String tableSuffix) {
			if (!(checkTable(tableSuffix))) {
				plugin.log.info("[" + plugin.name + "] Creating database.");
				if (createTable(tableSuffix))	
				{
					plugin.log.info("[" + plugin.name + "] Database Created.");
					return true;
				}
				else
				{
					plugin.log.severe("[" + plugin.name + "] Database creation *failed*.");
					return false;
				}
			}
		  	return false;
		}

		protected boolean deleteDatabase()
		{
			return deleteDatabase("");
		}

		protected boolean deleteDatabase(String tableSuffix)
		{
			SQLHandler myQuery = new SQLHandler(this);
			myQuery.executeStatement("DROP TABLE " + tableName+tableSuffix + ";");
			myQuery.close();
			if (myQuery.isOK)
				plugin.log.info("[" + plugin.name + "] Database table successfully deleted.");
			else
				plugin.log.severe("[" + plugin.name + "] Database table could not be deleted.");
			return myQuery.isOK;
		}
			
		public boolean resetDatabase()
		{
			return resetDatabase("");
		}
		
		public boolean resetDatabase(String tableSuffix)
		{
			deleteDatabase(tableSuffix);
			return initialize(tableSuffix);
		}

 
			protected Connection connection() throws ClassNotFoundException, SQLException {
				//CHANGED: Sets connections to auto-commit, rather than emergency commit-on-close behaviour.
				Connection newConn;
				if (this.databaseType.equals(Type.SQLITE)) {
/*  52 */       	Class.forName("org.sqlite.JDBC");
/*  53 */       	newConn = DriverManager.getConnection(DynamicMarket.sqlite);
					return newConn;
/*     */     	}
/*  55 */     	Class.forName("com.mysql.jdbc.Driver");
/*  56 */     	newConn = DriverManager.getConnection(DynamicMarket.mysql, DynamicMarket.mysql_user, DynamicMarket.mysql_pass);
				newConn.setAutoCommit(true);
				return newConn;
			}

		protected String dbTypeString()
		{
			return ((this.databaseType.equals(Type.SQLITE)) ? "sqlite" : "mysql");
		}
		
		protected void logSevereException(String exDesc, Exception exDetail)
		{
			plugin.log.severe("[" + plugin.name + "]: " + exDesc + ": " + exDetail);
		}
		
		protected void logSevereException(String exDesc)
		{
			plugin.log.severe("[" + plugin.name + "]: " + exDesc);
		}

		protected boolean checkTable(String tableSuffix)
		{
			SQLHandler myQuery = new SQLHandler(this);

			boolean bool;
		  	bool = myQuery.checkTable(tableName+tableSuffix);
		  	myQuery.close();
		  	return bool;
		}		
		
		protected boolean checkTable()
		{
			return checkTable("");
		}
					
		protected abstract boolean createTable(String tableSuffix);
		
		protected boolean createTable()
		{
			return createTable("");
		}
		/*
		{
			//SQLHandler myQuery = new SQLHandler(this);
			//if (this.database.equals(Type.SQLITE))
			//	myQuery.executeStatement("CREATE TABLE " + tableName+shopLabel + " ( id INT ( 255 ) PRIMARY KEY , item INT ( 255 ) NOT NULL, type INT ( 255 ) NOT NULL, buy INT ( 255 ) NOT NULL, sell INT ( 255 ) NOT NULL, per INT ( 255 ) NOT NULL);CREATE INDEX itemIndex on balances (item);CREATE INDEX typeIndex on balances (type);CREATE INDEX buyIndex on iBalances (buy);CREATE INDEX sellIndex on iBalances (sell);CREATE INDEX perIndex on iBalances (per);");
			//else
			//	myQuery.executeStatement("CREATE TABLE " + tableName+shopLabel + " ( id INT( 255 ) NOT NULL AUTO_INCREMENT, item INT( 255 ) NOT NULL, type INT( 255 ) NOT NULL, buy INT( 255 ) NOT NULL, sell INT( 255 ) NOT NULL, per INT( 255 ) NOT NULL ,PRIMARY KEY ( id ), INDEX ( item, type, buy, sell, per )) ENGINE = MYISAM;");
			//myQuery.close();
			//return myQuery.isOK;
			return false;
		}
		*/
			
		public abstract boolean add(Object newObject);
		/*
		{
			
			SQLHandler myQuery = new SQLHandler(this);
			myQuery.inputList.add(newItem.itemId);
			myQuery.inputList.add(newItem.subType);
			myQuery.inputList.add(newItem.buy);
			myQuery.inputList.add(newItem.sell);
			myQuery.inputList.add(newItem.count);
			myQuery.prepareStatement("INSERT INTO " + tableName + " (item,type,buy,sell,per) VALUES (?,?,?,?,?)");
			myQuery.executeUpdate();
			
			myQuery.close();
			return (myQuery.isOK);
			
			return false;
		}
		*/

		public abstract boolean update(Object updateRef);
		/*
		{
		//CHANGED: Shouldn't need to alter item subtypes in existing records. New item+type records should be added instead.
			SQLHandler myQuery = new SQLHandler(this);

			myQuery.inputList.add(updated.buy);
			myQuery.inputList.add(updated.sell);
			myQuery.inputList.add(updated.count);
			myQuery.inputList.add(updated.itemId);
			myQuery.inputList.add(updated.subType);
			myQuery.prepareStatement("UPDATE " + tableName + " SET buy = ?, sell = ?, per = ? WHERE item = ? AND type = ?" + ((this.database.equals(Type.SQLITE)) ? "" : " LIMIT 1"));

			myQuery.executeUpdate();
			
			myQuery.close();
			return (myQuery.isOK);
		}
		*/
		
		public abstract boolean remove(ItemClump removed);
		/*
		{
		  // CHANGED: Now accepts an itemType (through use of the ItemClump class). 
			SQLHandler myQuery = new SQLHandler(this);

			myQuery.inputList.add(removed.itemId);
			myQuery.inputList.add(removed.subType);
			myQuery.prepareStatement("DELETE FROM " + tableName + " WHERE item = ? AND type = ?" + ((this.database.equals(Type.SQLITE)) ? "" : " LIMIT 1"));

				myQuery.executeUpdate();
				
				myQuery.close();
				return myQuery.isOK;
			}
		*/

 

		public abstract ArrayList<?> list(int pageNum);
			/*
			{
			  //CHANGED: This now spits out a list of ShopItems, instead of a list of arrays of ints.
  		  //If pageNum=0, return the entire list.

		  //TODO: After incorporating items list into database, do text-matching.
			SQLHandler myQuery = new SQLHandler(this);
			ArrayList<ShopItem> data = new ArrayList<ShopItem>();
		  	int startItem = 0;
		  	int numItems = 9999999;
		  	if (pageNum > 0)
		  	{
		  		startItem = (pageNum - 1) * 8;
		  		numItems = 8;
		  	}
		  
		  	myQuery.inputList.add(startItem);
			myQuery.inputList.add(numItems);
			myQuery.prepareStatement("SELECT * FROM " + tableName + " ORDER BY item ASC, type ASC LIMIT ?, ?");
			
			myQuery.executeQuery();
			
			if (myQuery.isOK)
				try {
					while (myQuery.rs.next())
						data.add(new ShopItem(myQuery.rs.getInt("item"),myQuery.rs.getInt("type"), myQuery.rs.getInt("buy"), myQuery.rs.getInt("sell"), myQuery.rs.getInt("per") ));
				} catch (SQLException ex) {
					logSevereException("SQL Error during ArrayList fetch: " + dbTypeString(), ex);
				}
			
			myQuery.close();

			return data;
		}
		*/


		public abstract Object data(ItemClump thisItem);
		/*
		{
		  //CHANGED: Returns ShopItems now.
			SQLHandler myQuery = new SQLHandler(this);
			ShopItem data = null;

			myQuery.inputList.add(thisItem.itemId);
			myQuery.inputList.add(thisItem.subType);
			myQuery.prepareStatement("SELECT * FROM " + tableName + " WHERE item = ? AND type = ? LIMIT 1");

			myQuery.executeQuery();

			try {
				if (myQuery.rs != null)
					if (myQuery.rs.next())
						data = new ShopItem(myQuery.rs.getInt("item"), myQuery.rs.getInt("type"), myQuery.rs.getInt("buy"), myQuery.rs.getInt("sell"), myQuery.rs.getInt("per"));
			} catch (SQLException ex) {
				logSevereException("Error retrieving shop item data with " + dbTypeString(), ex);
				data = null;
			}
			
			myQuery.close();

			if (data == null) { data = new ShopItem(); }
			return data;
		}
		*/
				
		public static enum Type
		{
			SQLITE, MYSQL, FLATFILE;
		}
}

/* Location:           C:\Program Files\eclipse\Bukkit\SimpleShop.jar
 * Qualified Name:     com.nijikokun.bukkit.SimpleShop.Database
 * Java Class Version: 5 (49.0)
 * JD-Core Version:    0.5.3
 */