package com.gmail.haloinverse.DynamicMarket;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.sql.Statement;

public class SQLHandler {
	
	// A wrapper class to handle the repeated grunt-work of SQL queries and the errors they throw.

	public Connection conn;
	public PreparedStatement ps;
	public ResultSet rs;
	public DatabaseCore connDB;
	public ArrayList<Object> inputList;
	public boolean isOK;	// Default true, set to false when errors occur.
	
	public SQLHandler(DatabaseCore thisDB)
	{
		isOK = true;
		inputList = new ArrayList<Object>();
		connDB = thisDB;
		try {
			conn = connDB.connection();
		} catch (ClassNotFoundException ex) {
			connDB.logSevereException("Database connector not found for " + connDB.dbTypeString(), ex);
			conn = null;
			isOK = false;
		} catch (SQLException ex) {
			connDB.logSevereException("SQL Error connecting to " + connDB.dbTypeString() + "database", ex);
			conn = null;
			isOK = false;
		}
		ps = null;
		rs = null;
	};
	
	public void prepareStatement(String sqlString) 
	{
		try {
			ps = null;
			if (conn != null)
			{
				ps = conn.prepareStatement(sqlString);
				for(int i = 1; i <= inputList.size(); ++i)
				{
					ps.setObject(i, inputList.get(i-1));
				}
			}
		} catch (SQLException ex) {
			// TODO Auto-generated catch block
			connDB.logSevereException("Error preparing query statement [" + sqlString + "] for " + connDB.dbTypeString(), ex);	
			ps = null;
			isOK = false;
		}
	}
	
	public void executeQuery()
	{
		try {
			rs = null;
			if (ps != null)
				rs = ps.executeQuery();
		} catch (SQLException ex) {
			connDB.logSevereException("Error executing query statement [" + ps.toString() + "] with " + connDB.dbTypeString(), ex);
			rs = null;
			isOK = false;
		}
	}
	
	public void executeUpdate()
	{
		try {
			if (ps != null)
				ps.executeUpdate();
		} catch (SQLException ex) {
			connDB.logSevereException("Error executing update statement [" + ps.toString() + "] with " + connDB.dbTypeString(), ex);
			isOK = false;
		}
	}
	
	public void executeStatement(String sqlStatement)
	{
		Statement st = null;
		if (conn != null)
		{
			try {
				st = conn.createStatement();
				st.executeUpdate(sqlStatement);
			} catch (SQLException ex) {
				connDB.logSevereException("Error executing statement [" + sqlStatement + "] with " + connDB.dbTypeString(), ex);
				isOK = false;
			} finally {
				if (st != null)
					try {
						st.close();
					} catch (SQLException ex) {
						connDB.logSevereException("Error closing statement [" + sqlStatement + "] with " + connDB.dbTypeString(), ex);
						isOK = false;
					}
			}
		}
	}
	
	public boolean checkTable(String tableName)
	{
		boolean bool = false;
		if (conn != null)
			try {
				DatabaseMetaData dbm = conn.getMetaData();
				rs = dbm.getTables(null, null, tableName, null);
				bool = rs.next();
				return bool;
			}
			catch (SQLException ex)
			{
				connDB.logSevereException("Table check for " + connDB.dbTypeString() + " Failed", ex);
				isOK = false;
				bool = false;
				return bool;
			}
		return bool;
	}
	
	public int getInt(String fieldName)
	{
		try {
			return rs.getInt(fieldName);
		} catch (SQLException ex) {
			connDB.logSevereException("getInt '" + fieldName + "' with " + connDB.dbTypeString() + " Failed", ex);
			isOK = false;
			return 0;
		}
	}
	
	public String getString(String fieldName)
	{
		try {
			return rs.getString(fieldName);
		} catch (SQLException ex) {
			connDB.logSevereException("getString '" + fieldName + "' with " + connDB.dbTypeString() + " Failed", ex);
			isOK = false;
			return null;
		}
	}
	
	public void close()
	{
		if (rs != null)
			try {
				rs.close();
			} catch (SQLException ex) {
				connDB.logSevereException("SQL error closing resultset for " + connDB.dbTypeString() + "database", ex);
				isOK = false;
			}
		if (ps != null)
			try {
				ps.close();
			} catch (SQLException ex) {
				connDB.logSevereException("SQL error closing prepared statement for " + connDB.dbTypeString() + "database", ex);
				isOK = false;
			}
		if (conn != null)
			try {
				conn.close();
			} catch (SQLException ex) {
				connDB.logSevereException("SQL error closing connection for " + connDB.dbTypeString() + "database", ex);
				isOK = false;
			}
	}
	
	protected void finalize() throws Throwable {
		// Just in case close() doesn't get called...
	    try {
	        close();
	    } finally {
	        super.finalize();
	    }
	}
}
