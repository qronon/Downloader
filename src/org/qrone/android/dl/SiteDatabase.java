package org.qrone.android.dl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.qrone.android.BranchInputStream;
import org.qrone.android.Util;
import org.qrone.android.dl.DownloaderActivity.ImageRowData;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

public class SiteDatabase{
	private static Map<String, Bitmap> cache;
	private static Pattern urlRegex = Pattern.compile("http://([a-zA-Z0-9/\\._\\-=\\+&#\\?\\*]+)");
	private static Pattern titleRegex = Pattern.compile("<title>(.*)</title>");

    private static final String DB_DIRECTORY =
            Environment.getExternalStorageDirectory() +
            "/Android/data/org.qrone.dl/";

    /** データベースファイルのフルパス */
    private static final String DB_NAME =
        DB_DIRECTORY + "url.db";
    
    private SQLiteDatabase db = null;
	private DefaultHttpClient dhc = new DefaultHttpClient();
    
    private static final int CURRENT_VER = 1;
    private static final String CREATE_TABLE =
            "create table if not exists URLDB (" +
            "_id integer primary key autoincrement, " +
            "TEXT url, " +
            "TEXT contentType, " +
            "TEXT cache, " +
            "TEXT domain, " +
            "INTEGER parseVer, " + 
            "INTEGER date" +
            ");";

    private static final String CREATE_TABLE_B =
            "create table if not exists DOMAINDB (" +
            "_id integer primary key autoincrement, " +
            "TEXT domain, " +
            "TEXT title, " +
            "INTEGER access, " + 
            "INTEGER parseVer, " + 
            "INTEGER date" +
            ");";
    
    private static final String CREATE_TABLE_C =
            "create table if not exists HREFDB (" +
            "_id integer primary key autoincrement, " +
            "TEXT url, " +
            "TEXT domain, " +
            "TEXT refurl, " +
            "TEXT refdomain, " +
            "INTEGER date" +
            ");";
    
	/**
     * SDカード上のデータベースを開く。もしデータベースが開けない、または
     * 作成できない場合は例外を投げる。
     * 
     * @return true if successful
     * @throws SQLException if the database is unable to be opened or created
     */
    public boolean openDatabase() throws SQLException {
         
        if(db != null && db.isOpen()) {
            return true;
        } else {
            if(!new File(DB_DIRECTORY).exists()) {
                new File(DB_DIRECTORY).mkdirs();
            }
             
            try {
                db = SQLiteDatabase.openOrCreateDatabase(DB_NAME, null);
                db.execSQL(CREATE_TABLE);
                db.execSQL(CREATE_TABLE_B);
                db.execSQL(CREATE_TABLE_C);
            } catch (SQLException e) {
                throw e;
            }
        }
        return true;
    }
    
    public void crowlSite(String domain, SiteCursor cursor){
    	addDomain(domain);
    	
    	
    }
    

    
    public void addURL(String url){
    	String[] args = {url};
    	Cursor c = db.rawQuery("SELECT parseVer FROM URLDB WHERE url = ?", args);
    	if(!c.moveToFirst()){
    		ContentValues v = new ContentValues();
    		v.put("url", url);
    		v.put("domain", getDomainByURL(url));
    		v.put("parseVer", 0);
    		v.put("date", System.currentTimeMillis() / 1000L);
    		db.insert("URLDB", null, v);
    	}
    }
    
    private void addDomain(String domain){
    	String url = "http://" + domain;
    	String[] args = {domain};
    	Cursor c = db.rawQuery("SELECT title, parseVer FROM DOMAINDB WHERE domain = ?", args);
    	if(c.moveToFirst()){
    		int parseVer = c.getInt(1);
    		if(parseVer >= CURRENT_VER){
    			return;
    		}else{
    			addDomainRecord(domain);
    			addURL(url);
    		}
    	}else{
    		addDomainRecord(domain);
    		addURL(url);
    	}
    }
    
    private void addDomainRecord(String domain){
		ContentValues v = new ContentValues();
		v.put("domain", domain);
		v.put("access", 0);
		v.put("parseVer", 0);
		v.put("date", System.currentTimeMillis() / 1000L);
		db.insert("DOMAINDB", null, v);
		
    }
    
    private String getDomainRecord(String domain){
    	String url = "http://" + domain;

    	String source = wget(url);
    	List<String> links = getLinkFromSource(url, source);
    	String title = getTitleFromSource(source);
    	
    	return title;
    }
    
    private String getDomainByURL(String url){
    	return null;
    }
    
    private String wget(String url){
    	try{
        	
			HttpResponse httpResponse = dhc.execute(new HttpGet( url ));
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				HttpEntity httpEntity = httpResponse.getEntity();
				InputStream in = httpEntity.getContent();
				return new String( Util.read(in), "utf8" );
			}
			
		}catch(IOException e){
			
		}
    	return null;
    }
    
    private void foundNewLink(List<String> urls){
    	
    }

    private String getTitleFromSource( String source){
		Matcher m = titleRegex.matcher(source);
		if(m.find()){
			MatchResult mr = m.toMatchResult();
			return source.substring(mr.start(), mr.end());
		}
		return null;
    }
    
    private List<String> getLinkFromSource(String base, String source){
		Matcher m = urlRegex.matcher(source);
    	List<String> urls = new ArrayList<String>();
		while(m.find()){
			MatchResult mr = m.toMatchResult();
			urls.add(source.substring(mr.start(), mr.end()));
		}
		return urls;
    }
    
    private String getCacheFilename(String url){
    	return null;
    }
    
    public Bitmap request(String url){
    	InputStream inf = Util.loadFile("Android/data/org.qrone.dl", getCacheFilename(url));
		if(inf == null){
			try{
				DefaultHttpClient dhc = new DefaultHttpClient();
				HttpResponse httpResponse = dhc.execute(new HttpGet(url));
				if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					Header[] headers = httpResponse.getHeaders("Content-Type");
					HttpEntity httpEntity = httpResponse.getEntity();
					InputStream in = httpEntity.getContent();
					
					if(headers[0].getValue().startsWith("image/")){
						return memoryProcess(in, url, true);
						
					}else if(headers[0].getValue().startsWith("text/")){
						foundNewLink(getLinkFromSource(url, new String(Util.read(in))));
					}
					
				}
			}catch(IOException e){
				e.printStackTrace();
			}
		}else{
			return memoryProcess(inf, url, false);
		}
		
		return null;
    }
    
	private synchronized Bitmap memoryProcess(InputStream in, String url, boolean save){
		Bitmap bitm = null;
		if(save){
			try{
				OutputStream out = Util.saveFile("Android/data/org.qrone.dl", getCacheFilename(url));
				
				InputStream inf = new BranchInputStream(in, out);
				bitm = BitmapFactory.decodeStream(inf);
				inf.close();
				cache.put(url, bitm);
				
			}catch(IOException e){
				e.printStackTrace();
			}
		}else{
			bitm = BitmapFactory.decodeStream(in);
			cache.put(url, bitm);
		}
		return bitm;
	}
    
    public List<Domain> getDomains(){
    	return null;
    }
    
    public List<String> getImages(String domain){
    	return null;
    }
    
    public class Domain{
    	public String title;
    	public String domain;
    }
}
