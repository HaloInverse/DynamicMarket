package com.gmail.haloinverse.DynamicMarket;

/*     */ //package com.nijikokun.bukkit.SimpleShop;
/*     */ 
/*     */ import java.io.BufferedReader;
/*     */ import java.io.File;
/*     */ import java.io.FileInputStream;
/*     */ import java.io.FileOutputStream;
/*     */ import java.io.FileReader;
/*     */ import java.io.IOException;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.logging.Level;
		  import java.util.logging.Logger;
/*     */ 
/*     */ public final class iProperty
/*     */ {
/*  42 */   private static final Logger log = Logger.getLogger("Minecraft");
/*     */   private Properties properties;
/*     */   private String fileName;
/*     */ 
/*     */   public iProperty(String fileName)
/*     */   {
/*  47 */     this.fileName = fileName;
/*  48 */     this.properties = new Properties();
/*  49 */     File file = new File(fileName);
/*     */ 
/*  51 */     if (file.exists())
/*  52 */       load();
/*     */     else
/*  54 */       save();
/*     */   }
/*     */ 
/*     */   public void load()
/*     */   {
/*     */     try {
/*  60 */       this.properties.load(new FileInputStream(this.fileName));
/*     */     } catch (IOException ex) {
/*  62 */       log.log(Level.SEVERE, "Unable to load " + this.fileName, ex);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void save() {
/*     */     try {
/*  68 */       this.properties.store(new FileOutputStream(this.fileName), "Minecraft Properties File");
/*     */     } catch (IOException ex) {
/*  70 */       log.log(Level.SEVERE, "Unable to save " + this.fileName, ex);
/*     */     }
/*     */   }
/*     */ 
/*     */   public Map<String, String> returnMap() throws Exception {
/*  75 */     Map<String, String> map = new HashMap<String, String>();
/*  76 */     BufferedReader reader = new BufferedReader(new FileReader(this.fileName));
/*     */ 
/*     */     String line;
/*  78 */     while ((line = reader.readLine()) != null)
/*     */     {
/*  79 */       if (line.trim().length() == 0) {
/*     */         continue;
/*     */       }
/*  82 */       if (line.charAt(0) == '#') {
/*     */         continue;
/*     */       }
/*  85 */       int delimPosition = line.indexOf(61); // '='
/*  86 */       String key = line.substring(0, delimPosition).trim();
/*  87 */       String value = line.substring(delimPosition + 1).trim();
/*  88 */       map.put(key, value);
/*     */     }
/*  90 */     reader.close();
/*  91 */     return map;
/*     */   }
/*     */ 
/*     */   public void removeKey(String key) {
/*  95 */     this.properties.remove(key);
/*  96 */     save();
/*     */   }
/*     */ 
/*     */   public boolean keyExists(String key) {
/* 100 */     return this.properties.containsKey(key);
/*     */   }
/*     */ 
/*     */   public String getString(String key) {
/* 104 */     if (this.properties.containsKey(key)) {
/* 105 */       return this.properties.getProperty(key);
/*     */     }
/*     */ 
/* 108 */     return "";
/*     */   }
/*     */ 
/*     */   public String getString(String key, String value) {
/* 112 */     if (this.properties.containsKey(key)) {
/* 113 */       return this.properties.getProperty(key);
/*     */     }
/* 115 */     setString(key, value);
/* 116 */     return value;
/*     */   }
/*     */ 
/*     */   public void setString(String key, String value) {
/* 120 */     this.properties.setProperty(key, value);
/* 121 */     save();
/*     */   }
/*     */ 
/*     */   public int getInt(String key) {
/* 125 */     if (this.properties.containsKey(key)) {
/* 126 */       return Integer.parseInt(this.properties.getProperty(key));
/*     */     }
/*     */ 
/* 129 */     return 0;
/*     */   }
/*     */ 
/*     */   public int getInt(String key, int value) {
/* 133 */     if (this.properties.containsKey(key)) {
/* 134 */       return Integer.parseInt(this.properties.getProperty(key));
/*     */     }
/*     */ 
/* 137 */     setInt(key, value);
/* 138 */     return value;
/*     */   }
/*     */ 
/*     */   public void setInt(String key, int value) {
/* 142 */     this.properties.setProperty(key, String.valueOf(value));
/* 143 */     save();
/*     */   }
/*     */ 
/*     */   public double getDouble(String key) {
/* 147 */     if (this.properties.containsKey(key)) {
/* 148 */       return Double.parseDouble(this.properties.getProperty(key));
/*     */     }
/*     */ 
/* 151 */     return 0.0D;
/*     */   }
/*     */ 
/*     */   public double getDouble(String key, double value) {
/* 155 */     if (this.properties.containsKey(key)) {
/* 156 */       return Double.parseDouble(this.properties.getProperty(key));
/*     */     }
/*     */ 
/* 159 */     setDouble(key, value);
/* 160 */     return value;
/*     */   }
/*     */ 
/*     */   public void setDouble(String key, double value) {
/* 164 */     this.properties.setProperty(key, String.valueOf(value));
/* 165 */     save();
/*     */   }
/*     */ 
/*     */   public long getLong(String key) {
/* 169 */     if (this.properties.containsKey(key)) {
/* 170 */       return Long.parseLong(this.properties.getProperty(key));
/*     */     }
/*     */ 
/* 173 */     return 0L;
/*     */   }
/*     */ 
/*     */   public long getLong(String key, long value) {
/* 177 */     if (this.properties.containsKey(key)) {
/* 178 */       return Long.parseLong(this.properties.getProperty(key));
/*     */     }
/*     */ 
/* 181 */     setLong(key, value);
/* 182 */     return value;
/*     */   }
/*     */ 
/*     */   public void setLong(String key, long value) {
/* 186 */     this.properties.setProperty(key, String.valueOf(value));
/* 187 */     save();
/*     */   }
/*     */ 
/*     */   public boolean getBoolean(String key) {
/* 191 */     if (this.properties.containsKey(key)) {
/* 192 */       return Boolean.parseBoolean(this.properties.getProperty(key));
/*     */     }
/*     */ 
/* 195 */     return false;
/*     */   }
/*     */ 
/*     */   public boolean getBoolean(String key, boolean value) {
/* 199 */     if (this.properties.containsKey(key)) {
/* 200 */       return Boolean.parseBoolean(this.properties.getProperty(key));
/*     */     }
/*     */ 
/* 203 */     setBoolean(key, value);
/* 204 */     return value;
/*     */   }
/*     */ 
/*     */   public void setBoolean(String key, boolean value) {
/* 208 */     this.properties.setProperty(key, String.valueOf(value));
/* 209 */     save();
/*     */   }
/*     */ }

/* Location:           C:\Program Files\eclipse\Bukkit\SimpleShop.jar
 * Qualified Name:     com.nijikokun.bukkit.SimpleShop.iProperty
 * Java Class Version: 5 (49.0)
 * JD-Core Version:    0.5.3
 */