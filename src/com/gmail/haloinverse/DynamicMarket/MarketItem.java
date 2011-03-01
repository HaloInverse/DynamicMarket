package com.gmail.haloinverse.DynamicMarket;

import com.nijikokun.bukkit.iConomy.iConomy;
import java.lang.Math;
import java.util.ArrayList;
//import java.sql.ResultSet;

public class MarketItem extends ItemClump {

	private String name;		//n | name // name used for item
	public int basePrice;  		//bp	// base purchase price of item
	public int stock;      	//s		// current stock level of item
	public boolean canBuy;     //cb	// true if can be purchased, false if not
	public boolean canSell;    //cs	// true if can be sold, false if not
	private int volatility; 	//v		// % change in price per 1 stock bought/sold, * intScale
								//iv	// inverse volatility; units bought/sold per doubling/halving of price
	public int salesTax;   		//st	// basePrice * (1 - (salesTax/100)) = selling price
	public int stockLowest;		//sl	// minimum stock at which purchases will fail (hard limit)
	public int stockHighest;	//sh	// maximum stock at which sales will fail (hard limit)
	public int stockFloor;		//sf	// minimum stock level possible (soft limit)
	public int stockCeil;		//sc	// maximum stock level possible (soft limit)
	public int priceFloor;		//pf	// minimum price <- applies to basePrice, without salesTax
	public int priceCeil;		//pc	// maximum price
	public int jitterPerc;
	public int driftOut;
	public int driftIn;
	public int avgStock;
	public int itemClass;
	public String shopLabel = "";	// Shop DB table this is a member of. 
			// Other input tags:
								//fixed // stock = 0
										// stockLowest = Integer.MIN_VALUE
										// stockHighest = Integer.MAX_VALUE
										// stockFloor = 0
										// stockCeil = 0
								// buyok   // canBuy = 1
	                            // sellok  // canSell = 1
								// !nobuy   // canBuy = 1
    							// !nosell  // canSell = 1
								// !buyok  // canBuy = 0
								// !sellok // canSell = 0
								// nobuy  // canBuy = 0
								// nosell // canSell = 0	
	public static int intScale = 10000; // Scale factor used in integerization of calculations.
	public DatabaseMarket thisDatabase = null; // Database object this item is a member of.
	
	/*	diamond: 10 items -> 10% price change, 1 item -> 1% price change, 0.01 * 10000 = 1000
	 *  
	 *  cobble: 640 items -> 10% price change, 1 item -> 0.016% price change, 0.00016 * 10000 = 16
	 * 
	 *  ?inelastic?: v=1 -> 0.001% price change, 1000 items -> 1% price change
	 * 
	 *  ?elastic?: v=10000, 1 item -> +100% | -50% price change
	 * 
	 *  volatility: % change in price per unit bought/sold, * intScale / 100   (2% per item = 2 * intScale / 100)
	 *  inverse volatility: units bought/sold per doubling/halving of price
	 *  
	 *  ivol = ln(2) / ln(1+(vol/intScale))
	 *  vol  = (2^(1/iVol)-1) * intScale
	 * 
	 */
	
	public MarketItem(String thisShopLabel) 
	{
		// Default constructor with no parameters.
		super();
		setBaseDefaults();
		shopLabel = thisShopLabel;
	}
	
	public MarketItem()
	{
		// Default constructor with no parameters.
		super();
		setBaseDefaults();
	}
	
	public MarketItem(String initString, MarketItem defaults, DatabaseMarket thisDB, String thisShopLabel)
	{
		//Valid input string formats:
		//"[ID(,Type)](:Count) (buyprice (sellPrice)) (field:val (field:val (...)))"
		//"[ItemName](:Count) (buyprice (sellPrice)) (field:val (field:val (...)))"
		//TODO: Add alternate input format for setting low/high ranges with a single tag.
		
		super(initString.split(" ",2)[0], thisDB, thisShopLabel); // Parse id/name, type, and count from first split.
		
		thisDatabase = thisDB;
		shopLabel = thisShopLabel;
		
		//SimpleMarket.log.info("[1] InitString: [" + initString + "]");
		
		String[] initData = initString.split(" ");
		 		
		// Load defaults, if available.
		if (defaults != null)
		{
			this.basePrice = Math.round((float)defaults.basePrice * this.count / defaults.count);
			this.stock = Math.round(((float)defaults.stock * defaults.count / this.count) - 0.5f);
			this.canBuy = defaults.canBuy;
			this.canSell = defaults.canSell;
			this.volatility = defaults.volatility;
			this.salesTax = defaults.salesTax;
			this.stockLowest = defaults.stockLowest;
			this.stockHighest = defaults.stockHighest;
			this.stockFloor = defaults.stockFloor;
			this.stockCeil = defaults.stockCeil;
			this.priceFloor = defaults.priceFloor;
			this.priceCeil = defaults.priceCeil;
		}
		else
		{
			setBaseDefaults();
		}
		
		//Load data from remaining items in split.
		//If first one or two items have no tag, assume they are the basePrice and the sellPrice.
		
		parseTags(initData);
		
		sanityCheck();
	}
	
	private void parseTags(String initData[])
	{
		// Load data from a list of tags.
		// Element [0] is ignored.
		String curTag;
		Integer curVal;
		String stringVal;
		String[] curParams;
		boolean setUntaggedBase = false;
		
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
							this.basePrice = curVal;
							//maskData.basePrice = 1;
						}
						else
						{
							// Base price set to -1. Disable buying.
							this.basePrice = 0;
							//maskData.basePrice = 1;
							this.canBuy = false;
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
						}
						else
						{
							// Sale price set to -1. Disable selling.
							this.canSell = false;
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
							this.basePrice = curVal;
							continue;
						}
						if (curTag.equalsIgnoreCase("s") || curTag.equalsIgnoreCase("stock"))
						{
							this.stock = curVal;
							continue;
						}			
						if (curTag.equalsIgnoreCase("v") || curTag.equalsIgnoreCase("volatility") || curTag.equalsIgnoreCase("vol"))
						{
							this.setVolatility(curVal);
							continue;
						}
						if (curTag.equalsIgnoreCase("iv") || curTag.equalsIgnoreCase("invvolatility") || curTag.equalsIgnoreCase("ivol"))
						{
							this.setInverseVolatility(curVal);
							continue;
						}				
						if (curTag.equalsIgnoreCase("st") || curTag.equalsIgnoreCase("salestax"))
						{
							this.salesTax = rangeCrop(curVal, 0, 100);
							continue;
						}
						if (curTag.equalsIgnoreCase("sl") || curTag.equalsIgnoreCase("stocklowest"))
						{
							this.stockLowest = curVal;
							continue;
						}
						if (curTag.equalsIgnoreCase("sh") || curTag.equalsIgnoreCase("stockhighest"))
						{
							this.stockHighest = curVal;
							continue;
						}
						if (curTag.equalsIgnoreCase("sf") || curTag.equalsIgnoreCase("stockfloor"))
						{
							this.stockFloor = curVal;
							continue;
						}
						if (curTag.equalsIgnoreCase("sc") || curTag.equalsIgnoreCase("stockceiling"))
						{
							this.stockCeil = curVal;
							continue;
						}
						if (curTag.equalsIgnoreCase("pf") || curTag.equalsIgnoreCase("pricefloor"))
						{
							this.priceFloor = curVal;
							continue;
						}
						if (curTag.equalsIgnoreCase("pc") || curTag.equalsIgnoreCase("priceceiling"))
						{
							this.priceCeil = curVal;
							continue;
						}
					}
				}
				if (curTag.equalsIgnoreCase("flat"))
				{
					this.stock = 0;
					this.stockLowest = Integer.MIN_VALUE;
					this.stockHighest = Integer.MAX_VALUE;
					this.stockFloor = Integer.MIN_VALUE;
					this.stockCeil = Integer.MAX_VALUE;
					this.priceFloor = 0;
					this.priceCeil = Integer.MAX_VALUE;
					this.volatility = 0;
					continue;
				}
				if (curTag.equalsIgnoreCase("fixed"))
				{
					this.stock = 0;
					this.stockLowest = Integer.MIN_VALUE;
					this.stockHighest = Integer.MAX_VALUE;
					this.stockFloor = 0;
					this.stockCeil = 0;
					this.priceFloor = 0;
					this.priceCeil = Integer.MAX_VALUE;
					this.volatility = 0;
					continue;
				}
				if (curTag.equalsIgnoreCase("float"))
				{
					this.stockFloor = Integer.MIN_VALUE;
					this.stockCeil = Integer.MAX_VALUE;
					this.stockLowest = Integer.MIN_VALUE;
					this.stockHighest = Integer.MAX_VALUE;
					this.priceFloor = 0;
					this.priceCeil = Integer.MAX_VALUE;
					if (this.volatility == 0)
						this.volatility = 100;
				}
				if (curTag.equalsIgnoreCase("finite"))
				{
					this.stockFloor = Integer.MIN_VALUE;
					this.stockCeil = Integer.MAX_VALUE;
					this.stockLowest = 0;
					this.stockHighest = Integer.MAX_VALUE;
				}
				if ((curTag.equalsIgnoreCase("buyok")) || (curTag.equalsIgnoreCase("!nobuy")))
				{
					this.canBuy = true;
					continue;
				}
				if ((curTag.equalsIgnoreCase("sellok")) || (curTag.equalsIgnoreCase("!nosell")))
				{
					this.canSell = true;
					continue;
				}
				if ((curTag.equalsIgnoreCase("!buyok")) || (curTag.equalsIgnoreCase("nobuy")) || (curTag.equalsIgnoreCase("!cb")))
				{
					this.canBuy = false;
					continue;
				}
				if ((curTag.equalsIgnoreCase("!sellok")) || (curTag.equalsIgnoreCase("nosell")) || (curTag.equalsIgnoreCase("!cs")))
				{
					this.canSell = false;
					continue;
				}
				if (curTag.equalsIgnoreCase("cb") || curTag.equalsIgnoreCase("canbuy"))
				{
					if ((stringVal != null) && (!(stringVal.isEmpty())))
					{
						this.canBuy = ((stringVal.toLowerCase().startsWith("y")) || (stringVal.toLowerCase().startsWith("t")));
					}
					else
						this.canBuy = true;
					continue;
				}
				if (curTag.equalsIgnoreCase("cs") || curTag.equalsIgnoreCase("cansell"))
				{
					if ((stringVal != null) && (!(stringVal.isEmpty())))
					{
						this.canSell = ((stringVal.toLowerCase().startsWith("y")) || (stringVal.toLowerCase().startsWith("t")));
					}
					else
						this.canSell = true;
					continue;
				}
				if ((curTag.equalsIgnoreCase("name")) || (curTag.equalsIgnoreCase("n")))
				{
					setName(stringVal);
					continue;
				}
				if (curTag.equalsIgnoreCase("renorm"))
				{
					//double oldBuyPrice = getStockPrice(this.stock);
					int newStock;
					if (curVal == null)
						newStock = 0;
					else
						newStock = curVal;
					this.basePrice = getRenormedPrice(newStock);
					this.stock = newStock;
					// basePrice = inverse of stockPrice = rangeCrop(this.basePrice * Math.pow(getVolFactor(), -rangeCrop(stockLevel, stockFloor, stockCeil)), priceFloor, priceCeil)
					/*
					 * oldstockPrice = this.basePrice * (getVolFactor() ^ -stockLevel)
					 * oldstockPrice / (getVolFactor() ^ -stockLevel) = basePrice 
					 */
					//this.basePrice = (int)Math.round(oldBuyPrice / Math.pow(getVolFactor(), -rangeCrop(this.stock, stockFloor, stockCeil)));
				}
			}
			//TODO: Log and report invalid tags somehow.
		}
	}
	
	public int getRenormedPrice(int newStock)
	{
		// Calculate what the renormalized base price would be when shifting from the current stock level to newStock.
		double oldBuyPrice = getStockPrice(this.stock);
		return (int)Math.round(oldBuyPrice / Math.pow(getVolFactor(), -rangeCrop(newStock, stockFloor, stockCeil)));
	}
	
	public MarketItem(SQLHandler myQuery)
	{
		// Initialize a new MarketItem from an SQLHandler ResultSet.
		super();
		//thisDatabase.plugin.log.info("Creating from SQL...");
		this.itemId = myQuery.getInt("item");
		this.subType = myQuery.getInt("subtype");
		this.name = myQuery.getString("name");
		this.count = myQuery.getInt("count");
		this.basePrice = myQuery.getInt("baseprice");
		this.canBuy = (myQuery.getInt("canbuy") == 1);
		this.canSell = (myQuery.getInt("cansell") == 1);
		this.stock = myQuery.getInt("stock");
		this.volatility = myQuery.getInt("volatility");
		this.salesTax = myQuery.getInt("salestax");
		this.stockHighest = myQuery.getInt("stockhighest");
		this.stockLowest = myQuery.getInt("stocklowest");
		this.stockFloor = myQuery.getInt("stockfloor");
		this.stockCeil = myQuery.getInt("stockceil");
		this.priceFloor = myQuery.getInt("pricefloor");
		this.priceCeil = myQuery.getInt("priceceil");
		this.shopLabel = myQuery.getString("shoplabel");
		sanityCheck();
	}
	
	private void sanityCheck()
	{
		// Check and fix possible chaos-inducing data.
		// Mirrors DatabaseMarket.sanityCheckAll.
		
		this.salesTax = rangeCrop(this.salesTax, 0, 100);
		
		if (this.stockHighest < this.stock)
			this.stockHighest = this.stock;
		
		if (this.stockLowest > this.stock)
			this.stockLowest = this.stock;		

		if (this.stockCeil < this.stock)
			this.stockCeil = this.stock;

		if (this.stockFloor > this.stock)
			this.stockFloor = this.stock;
		
		if (this.priceCeil < this.priceFloor)
			this.priceCeil = this.priceFloor;
		
		this.basePrice = Math.max(0, this.basePrice);
	}
	
	private void setBaseDefaults()
	{
		name = null;
		basePrice = 0;
		stock = 0;
		canBuy = true;
		canSell = true;
		volatility = 1;
		salesTax = 0;
		stockLowest = Integer.MIN_VALUE;
		stockHighest = Integer.MAX_VALUE;
		stockFloor = 0;
		stockCeil = 0;
		priceFloor = 0;
		priceCeil = Integer.MAX_VALUE;
	}
	
	//public int sellPrice()
	//{
	//	return (rangeCrop(Math.round(basePrice * (1 - (salesTax / 100))), priceFloor, priceCeil));
	//}
	
	public static int rangeCrop(int value, int minVal, int maxVal)
	{
		return (Math.min(Math.max(value, minVal), maxVal));
	}
	
	public static double rangeCrop(double value, double minVal, double maxVal)
	{
		return (Math.min(Math.max(value, minVal), maxVal));
	}
	
	public String getName()
	{
		if ((this.name == null) || (this.name.isEmpty()))
			this.name = super.getName(thisDatabase, shopLabel);
		return this.name;
	}

	public void setName(String newName)
	{
		if ((newName != null) && (!(newName.isEmpty())))
		{
			this.name = newName;
		}
		else
		{
			this.name = super.getName(thisDatabase, shopLabel);
		}
	}
	
	public double getBatchPrice(int startStock, int endStock)
	{
		// Gets the current base price for the items at stock levels from startStock to endStock.
		// NOTE: Does not check stockLowest/stockHighest transaction limits.
		
		int lowStock = Math.min(startStock, endStock);
		int highStock = Math.max(startStock, endStock);
		int numTerms = highStock - lowStock + 1;
		double lowStockPrice;
		double highStockPrice;
		int fixedStockLimit;
		
		// End calculation if volatility == 0. Price does not change, so all items are the same value.
		if (volatility == 0)
			return (numTerms * getStockPrice(stock));
		
		// End calculation if highStock <= stockFloor (All below floor)
		if (highStock <= stockFloor)
			return (numTerms * getStockPrice(stockFloor));
		
		// End calculation if lowStock >= stockCeil (All above ceiling)
		if (lowStock >= stockCeil)
			return (numTerms * getStockPrice(stockCeil));
		
		// Split calculation if stockFloor reached by lowStock (Some below floor)
		if (lowStock < stockFloor)
			return (((stockFloor-lowStock)*getStockPrice(stockFloor)) + getBatchPrice(stockFloor, highStock));
		
		// Split calculation if stockCeil reached by highStock (Some above ceiling)
		if (highStock > stockCeil)
			return (((highStock-stockCeil)*getStockPrice(stockCeil)) + getBatchPrice(lowStock, stockCeil));
		
		lowStockPrice = getStockPrice(lowStock); // highest price in range
		highStockPrice = getStockPrice(highStock); // lowest price in range
		
		// WARNING in this section: Highest stock level corresponds to lowest price,
		//                       and lowest stock level corresponds to highest price.
		
		// End calculation if lowStockPrice <= priceFloor (All below floor)
		if (lowStockPrice <= priceFloor)
			return (numTerms * priceFloor);
		
		// End calculation if highStockPrice >= priceCeil (All above ceiling)
		if (highStockPrice >= priceCeil)
			return (numTerms * priceCeil);
		
		// Split calculation if highStockPrice < priceFloor (Some below floor)
		if (highStockPrice < priceFloor)
		{
			fixedStockLimit = (int)Math.round(Math.floor(stockAtPrice(priceFloor)));
			return (((highStock - fixedStockLimit) * priceFloor) + getBatchPrice(lowStock, fixedStockLimit));
		}
		
		// Split calculation if lowStockPrice > priceCeil (Some above ceiling)
		if (lowStockPrice > priceCeil)
		{
			fixedStockLimit = (int)Math.round(Math.ceil(stockAtPrice(priceCeil)));
			return (((fixedStockLimit - lowStock) * priceCeil) + getBatchPrice(fixedStockLimit, highStock));
		}
		
		
		// All range limits handled? Find the sum of terms of a finite geometric series.
		//return Math.round(this.basePrice * Math.pow(getVolFactor(),-lowStock) * (Math.pow(getVolFactor(),numTerms) - 1) / (getVolFactor()-1));
		//return math.round(firstTerm * (1 - (ratio ^ terms)) / (1 - ratio));
		return Math.round(lowStockPrice * (1 - (Math.pow(1/getVolFactor(), numTerms))) / (1 - (1/getVolFactor())));
	}
	
	public int getBuyPrice(int numBundles)
	{
		// Return the purchase price of the given number of bundles.
		getBatchPrice(stock, stock + numBundles - 1);
		return (int)Math.round(Math.ceil(getBatchPrice(stock, stock - numBundles + 1)));
	}
	
	public int getSellPrice(int numBundles)
	{
		// Return the selling price of the given number of bundles.
		return (int)Math.round(Math.floor(deductTax(getBatchPrice(stock + numBundles, stock + 1))));
	}
	
	private double deductTax(double basePrice)
	{
		// Returns the given price minus the sales tax.
		return (basePrice * (1 - ((double)salesTax/100)));
	}
	
	private double getStockPrice(int stockLevel)
	{
		// Crops result to stockFloor/stockCeil/priceFloor/priceCeil.
		return rangeCrop(this.basePrice * Math.pow(getVolFactor(), -rangeCrop(stockLevel, stockFloor, stockCeil)), priceFloor, priceCeil);
	}
	
	public int getVolatility()
	{
		return this.volatility;
	}
	
	public double getVolFactor()
	{
		return (1 + (double)volatility/intScale);
	}
	
	public void setVolatility(int newVol)
	{
		this.volatility = rangeCrop(newVol, 0, intScale); 
	}
	
	public void setVolatility(double newVol)
	{
		setVolatility((int)Math.round(newVol));
	}
	
	public int getInverseVolatility()
	{
		if(volatility == 0)
			return Integer.MAX_VALUE;
		else
			return (int)Math.round(Math.log(2) / Math.log(getVolFactor()));
	}
	
	public void setInverseVolatility(int newIVol)
	{
		//if (newIVol == Integer.MAX_VALUE)
		//	setVolatility(0);
		//else
		//	setVolatility((Math.pow(2,(1/(double)newIVol))-1) * intScale);
		setVolatility(iVolToVol(newIVol));
	}
	
	public static int iVolToVol(int invVol)
	{
		// Converts inverse volatility to volatility.
		if (invVol == Integer.MAX_VALUE)
			return 0;
		else
			return (int)Math.round((Math.pow(2,(1/(double)invVol))-1) * intScale);
	}
	
	private double stockAtPrice(int targPrice)
	{
		// Returns the stock level at which price == targPrice.
		if (volatility == 0)
		{
			// If price doesn't change, the stock level is effectively +/-INF.
			if (targPrice > basePrice)
				return Integer.MIN_VALUE;
			if (targPrice < basePrice)
				return Integer.MAX_VALUE;
			// targPrice == basePrice
				return stock;
		}
		return -(Math.log((double)targPrice/basePrice) / Math.log(getVolFactor()));
	}
	
	public String formatBundleCount(int numBundles)
	{
		if (numBundles == 1)
			return (Integer.toString(count));
		if (count == 1)
			return (Integer.toString(numBundles));
		return (Integer.toString(count) + "x" + Integer.toString(numBundles));
	}
	
	public String infoStringBuy(int numBundles)
	{
		if (!isValid())
			return ("{ERR}Invalid or uninitialized item.");
		if (!canBuy)
			return ("{PRM}"+getName() + "{ERR} is unavailable for purchase.");
		if (!getCanBuy(1))
			return ("{PRM}"+getName() + "{ERR} is out of stock: no more can be bought right now.");
		if (!getCanBuy(numBundles))
			return ("{PRM}"+getName() + "{ERR} has only {PRM}" + formatBundleCount(leftToBuy()) + " {ERR}left for sale.");
		// Display count as [<bundle>(x<numbundles>)]
		return ("{}Buy: {BKT}[{PRM}" + formatBundleCount(numBundles) + "{BKT}]{} for {PRM}" + getBuyPrice(numBundles) + " " + iConomy.currency);
		//TODO: Abstract currency name from iConomy reference.
	}
	
	public String infoStringSell(int numBundles)
	{
		if (!isValid())
			return ("{ERR}Invalid or uninitialized item.");
		if (!canSell)
			return ("{PRM}"+getName() + "{ERR} is unavailable for purchase.");
		if (!getCanSell(1))
			return ("{PRM}"+getName() + "{ERR} is overstocked: no more can be sold right now.");
		if (!getCanSell(numBundles))
			return ("{PRM}"+getName() + "{ERR} is overstocked, only {PRM}" + formatBundleCount(leftToSell()) + " {ERR}can be sold.");
		// Display count as [<bundle>(x<numbundles>)]
		return ("{}Sell: {BKT}[{PRM}" + formatBundleCount(numBundles) + "{BKT}]{} for {PRM}" + getSellPrice(numBundles) + " " + iConomy.currency);
		//TODO: Abstract currency name from iConomy reference.
	}

	public int leftToBuy()
	{
		return (stock - stockLowest);
	}
	
	public int leftToSell()
	{
		return (stockHighest - stock);
	}
	
	public String infoStringBuy()
	{
		return infoStringBuy(1);
	}
	
	public String infoStringSell()
	{
		return infoStringSell(1);
	}
	
	public boolean getCanBuy(int numBundles)
	{
		// Report if the requested number of bundles can be bought.
		if (canBuy == false)
			return false;
		if ((stock - numBundles) < stockLowest)
			return false;
		return true;
	}

	public boolean getCanSell(int numBundles)
	{
		// Report if the requested number of bundles can be sold.
		if (canSell == false)
			return false;
		if ((stock + numBundles) > stockHighest)
			return false;
		return true;
	}
	
	public String infoStringShort()
	{
		return("{BKT}[{}" + itemId + (subType != 0? ","+subType : "") + "{BKT}]{} " + getName() + "{BKT}[{}" + count + "{BKT}]{} Buy:{BKT}[{}" + (getCanBuy(1)? getBuyPrice(1) : "-") + "{BKT}]{} Sell:{BKT}[{}" + (getCanSell(1)? getSellPrice(1) : "-") + "{BKT}]");
	}
	
	public ArrayList<String> infoStringFull()
	{
		// More detailed info, more useful to shop admins.
		// Switch to select line number?
		ArrayList<String> returnList = new ArrayList<String>();
		returnList.add("{}ID:{BKT}[{PRM}" + itemId + (subType != 0? ","+subType : "") + "{BKT}]{} Name:{PRM}" + getName() + "{} BundleSize:{BKT}[{PRM}" + count + "{BKT}]");
		returnList.add("{}BasePrice:{BKT}[{PRM}" + basePrice + "{BKT}]{} SalesTax:{BKT}[{PRM}" + salesTax + "{BKT}]{} Vol:{BKT}[{PRM}" + volatility + "{BKT}]{} IVol:{BKT}[{PRM}" + getInverseVolatility() + "{BKT}]");
		returnList.add("{}Stock:{BKT}[{PRM}" + stock + "{BKT}]{} CanBuy:{BKT}[{PRM}" + (getCanBuy(1)? getBuyPrice(1) : "-") + "{BKT}]{} CanSell:{BKT}[{PRM}" + (getCanSell(1)? getSellPrice(1) : "-") + "{BKT}]");
		returnList.add("{}StockLowest:{BKT}[{PRM}" + intFormat(stockLowest) + "{BKT}]{} StockHighest:{BKT}[{PRM}" + intFormat(stockHighest) + "{BKT}]");
		returnList.add("{}StockFloor:{BKT}[{PRM}" + intFormat(stockFloor) + "{BKT}]{} StockCeiling:{BKT}[{PRM}" + intFormat(stockCeil) + "{BKT}]");
		returnList.add("{}PriceFloor:{BKT}[{PRM}" + intFormat(priceFloor) + "{BKT}]{} PriceCeiling:{BKT}[{PRM}" + intFormat(priceCeil) + "{BKT}]");
		return returnList;
		
		/*
		return("&d[&f" + itemId + (subType != 0? ","+subType : "") + "&d]&f " + getName() + "&d[&f" + count + "&d]&f " +
				"bp:&d[&f" + basePrice + "&d]&f st:&d[&f" + salesTax + "&d]&f v:&d[&f" + volatility + "&d]&f " +
				"iv:&d[&f" + getInverseVolatility() + "&d]&f s:&d[&f" + stock + "&d]&f " +
				"cb:&d[&f" + (getCanBuy(1)? getBuyPrice(1) : "-") + "&d]&f cs:&d[&f" + (getCanSell(1)? getSellPrice(1) : "-") + "&d]&f " +
				"sl:&d[&f" + intFormat(stockLowest) + "&d]&f sh:&d[&f" + intFormat(stockHighest) + "&d]&f " +
				"sf:&d[&f" + intFormat(stockFloor) + "&d]&f sc:&d[&f" + intFormat(stockCeil) + "&d]&f " +
				"pf:&d[&f" + intFormat(priceFloor) + "&d]&f pc:&d[&f" + intFormat(priceCeil) + "&d]");
		*/
	}
	
	public String intFormat(int thisInt)
	{
		// Returns the string value of an int, with Integer.MAX_VALUE and Integer.MIN_VALUE converted to shorthand.
		if (thisInt == Integer.MAX_VALUE)
			return "+INF";
		if (thisInt == Integer.MIN_VALUE)
			return "-INF";
		return Integer.toString(thisInt);
	}
	
	public String csvLine()
	{
		// Spits out a line corresponding to a line in a .csv file.
		return (itemId + "," + subType + "," + count + "," + name + "," + basePrice + "," + stock + "," + (canBuy? "Y" : "N") + "," + (canSell? "Y" : "N") + "," + volatility + "," + salesTax + "," + intFormat(stockLowest) + "," + intFormat(stockHighest) + "," + intFormat(stockFloor) + "," + intFormat(stockCeil) + "," + intFormat(priceFloor) + "," + intFormat(priceCeil) + "," + jitterPerc + "," + driftOut + "," + driftIn + "," + avgStock + "," + itemClass);
	}
	
	public static String csvHeaderLine()
	{
		// Spits out an appropriate header line for the output spreadsheet.
		return ("itemId,subType,count,name,basePrice,stock,canBuy,canSell,volatility,salesTax,stockLowest,stockHighest,stockFloor,stockCeil,priceFloor,priceCeil,jitterPerc,driftOut,driftIn,avgStock,itemClass");		
	}
	
	private int quickParse(String intString)
	{
		// Parses strings into ints, throwing away errors and replacing them with 0s.
		if (intString.equalsIgnoreCase("-inf"))
			return Integer.MIN_VALUE;
		if (intString.equalsIgnoreCase("+inf"))
			return Integer.MAX_VALUE;
		try
		{
			return Integer.parseInt(intString);
		}
		catch (NumberFormatException ex)
		{
			return 0;
		}
	}
	
	public MarketItem(String csvString, MarketItem defaults, String shopLabel, boolean isCSV)
	{
		super();
		
		if (defaults != null)
		{
			this.itemId = defaults.itemId;
			this.subType = defaults.subType;
			this.count = defaults.count;
			this.basePrice = defaults.basePrice;
			this.stock = defaults.stock;
			this.canBuy = defaults.canBuy;
			this.canSell = defaults.canSell;
			this.volatility = defaults.volatility;
			this.salesTax = defaults.salesTax;
			this.stockLowest = defaults.stockLowest;
			this.stockHighest = defaults.stockHighest;
			this.stockFloor = defaults.stockFloor;
			this.stockCeil = defaults.stockCeil;
			this.priceFloor = defaults.priceFloor;
			this.priceCeil = defaults.priceCeil;
			this.jitterPerc = defaults.jitterPerc;
			this.driftOut = defaults.driftOut;
			this.driftIn = defaults.driftIn;
			this.avgStock = defaults.avgStock;
			this.itemClass = defaults.itemClass;
		}
		else
			setBaseDefaults();
		
		String[] inputParams = csvString.split(",");
		
		if (inputParams.length < 21)
		{
			return;
		}
		
		if (!inputParams[0].isEmpty())
			itemId = quickParse(inputParams[0]);
		if (!inputParams[1].isEmpty())
			subType = quickParse(inputParams[1]);
		if (!inputParams[2].isEmpty())
			count = quickParse(inputParams[2]);
		if (!inputParams[3].isEmpty())
			name = inputParams[3];
		if (!inputParams[4].isEmpty())
			basePrice = quickParse(inputParams[4]); 
		if (!inputParams[5].isEmpty())
			stock = quickParse(inputParams[5]);
		if (!inputParams[6].isEmpty())
			canBuy = inputParams[6].equalsIgnoreCase("Y");
		if (!inputParams[7].isEmpty())
			canSell = inputParams[7].equalsIgnoreCase("Y");
		if (!inputParams[8].isEmpty())
			volatility = quickParse(inputParams[8]);
		if (!inputParams[9].isEmpty())
			salesTax = quickParse(inputParams[9]);
		if (!inputParams[10].isEmpty())
			stockLowest = quickParse(inputParams[10]);
		if (!inputParams[11].isEmpty())
			stockHighest = quickParse(inputParams[11]);
		if (!inputParams[12].isEmpty())
			stockFloor = quickParse(inputParams[12]);
		if (!inputParams[13].isEmpty())
			stockCeil = quickParse(inputParams[13]);
		if (!inputParams[14].isEmpty())
			priceFloor = quickParse(inputParams[14]);
		if (!inputParams[15].isEmpty())
			priceCeil = quickParse(inputParams[15]);
		if (!inputParams[16].isEmpty())
			jitterPerc = quickParse(inputParams[16]);
		if (!inputParams[17].isEmpty())
			driftOut = quickParse(inputParams[17]);
		if (!inputParams[18].isEmpty())
			driftIn = quickParse(inputParams[18]);
		if (!inputParams[19].isEmpty())
			avgStock = quickParse(inputParams[19]);
		if (!inputParams[20].isEmpty())
			itemClass = quickParse(inputParams[20]);
		this.shopLabel = shopLabel; 
		sanityCheck();
	}
	
	public void setEmptyMask()
	{
		// Wipes all fields to prepare this MarketItem to be used as a data mask.
		this.name = null;
		this.basePrice = 0;
		this.stock = 0;
		this.canBuy = false;
		this.canSell = false;
		this.volatility = 0;
		this.salesTax = 0;
		this.stockLowest = 0;
		this.stockHighest = 0;
		this.stockFloor = 0;
		this.stockCeil = 0;
		this.priceFloor = 0;
		this.priceCeil = 0;
		this.jitterPerc = 0;
		this.driftOut = 0;
		this.driftIn = 0;
		this.avgStock = 0;
		this.itemClass = 0;
		this.shopLabel = null;
	}
	
	/*
	public void copyMasked(MarketItem data, MarketItem mask)
	{
		// Copies in the fields from data, for only the fields set non-zero/non-null/true in mask.
		if (mask.name != null) 		this.name = data.name;
		if (mask.basePrice != 0) 	this.basePrice = data.basePrice;
		if (mask.stock != 0) 		this.stock = data.stock;
		if (mask.canBuy) 			this.canBuy = data.canBuy;
		if (mask.canSell) 			this.canSell = data.canSell;
		if (mask.volatility != 0) 	this.volatility = data.volatility;
		if (mask.salesTax != 0) 	this.salesTax = data.salesTax;
		if (mask.stockLowest != 0) 	this.stockLowest = data.stockLowest;
		if (mask.stockHighest != 0) this.stockHighest = data.stockHighest;
		if (mask.stockFloor != 0) 	this.stockFloor = data.stockFloor;
		if (mask.stockCeil != 0) 	this.stockCeil = data.stockCeil;
		if (mask.priceFloor != 0) 	this.priceFloor = data.priceFloor;
		if (mask.priceCeil != 0) 	this.priceCeil = data.priceCeil;
		if (mask.jitterPerc != 0) 	this.jitterPerc = data.jitterPerc;
		if (mask.driftOut != 0) 	this.driftOut = data.driftOut;
		if (mask.driftIn != 0) 		this.driftIn = data.driftIn;
		if (mask.avgStock != 0) 	this.avgStock = data.avgStock;
		if (mask.itemClass != 0) 	this.itemClass = data.itemClass;
		if (mask.shopLabel != null) this.shopLabel = data.shopLabel;
	}
	*/
}
