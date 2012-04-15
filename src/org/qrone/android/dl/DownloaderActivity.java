package org.qrone.android.dl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.qrone.android.Util;

import com.actionbarsherlock.app.SherlockActivity;


import android.app.Activity;
import android.content.Context;
import android.content.res.Resources.Theme;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DownloaderActivity extends SherlockActivity{
	private static final String TAG = "Activity";
	
	private int page = 0;
	private ListView listView;
	private List<ImageRowData> list;
	private View llcache;
	
	private DefaultHttpClient dhc;
	
	
	private int getScreenWidth(){
		WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
		Display disp = wm.getDefaultDisplay();
		return disp.getWidth();
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setTheme(R.style.Theme_Sherlock);
        setContentView(R.layout.imglist);
        
        dhc = new DefaultHttpClient();
        

        list = new ArrayList<ImageRowData>();
        dhc = new DefaultHttpClient();
        loadPage(++page);
        
        listView = (ListView)findViewById(R.id.listView);
        listView.setAdapter(new BaseAdapter() {
			
			public View getView(int position, View convertView, ViewGroup parent) {
				if(convertView == null){
					LayoutInflater inf = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					convertView = inf.inflate(R.layout.imglist_row, null);
			        Log.d(TAG, "Create ImgView");  
				}
				
				if(position == list.size()){
					convertView.findViewById(R.id.imgView).setVisibility(View.GONE);
					convertView.findViewById(R.id.imgView_last).setVisibility(View.VISIBLE);
				}else{
					convertView.findViewById(R.id.imgView).setVisibility(View.VISIBLE);
					convertView.findViewById(R.id.imgView_last).setVisibility(View.GONE);
					
					ImageRowData rd = list.get(position);
					LazyImageView v = (LazyImageView)convertView.findViewById(R.id.imgView);
					
					
					v.setWidth(listView.getWidth());
					v.setURL(rd.url, rd.filename);
				}
					
				return convertView;
			}
			
			public long getItemId(int position) {
				return position;
			}
			
			public Object getItem(int position) {
				return list.get(position);
			}
			
			public int getCount() {
				return list.size() + 1;
			}
		});
        
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View v, int i,
					long arg3) {
				if(i == list.size()){
			        loadPage(++page);
					Toast.makeText(DownloaderActivity.this, getString(R.string.loading_page_x).replace("%", String.valueOf(page)), 3).show();
				}
			}
		});
    }
    
    public void loadPage(final int page){

        AsyncTask<Integer, Integer, String> task = new AsyncTask<Integer, Integer, String>(){

			@Override
			protected String doInBackground(Integer... params) {
		        
				try{
					HttpResponse httpResponse;
					if(page == 1){
						httpResponse = dhc.execute(new HttpGet(
								"http://4u-beautyimg.com/"));
					}else{

						httpResponse = dhc.execute(new HttpGet(
								"http://4u-beautyimg.com/?page=" + page));
					}
					
					if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
						HttpEntity httpEntity = httpResponse.getEntity();
						final InputStream in = httpEntity.getContent();
						return Util.convertStreamToString(in);
						
					}
				}catch(IOException e){
					e.printStackTrace();
				}
				
				return null;
			}
			
			@Override
			protected void onPostExecute(String result){
				if(result == null) return;

				Matcher m = Pattern.compile("http://4u-beautyimg.com/thumb/l/(l[a-zA-Z0-9/\\._\\-=\\+&#]+)").matcher(result);
				while(m.find()){
					MatchResult mr = m.toMatchResult();
					String sub = result.substring(mr.start(), mr.end());

			        ImageRowData d = new ImageRowData();
			        d.url = sub;
			        d.filename = mr.group(1);
			        list.add(d);
				}
				
		        BaseAdapter ba = (BaseAdapter)listView.getAdapter();
		        ba.notifyDataSetChanged();
				
				if(page == 1){
					LinearLayout ll = (LinearLayout)findViewById(R.id.fullscreen_loading_indicator);
			        ll.setVisibility(View.GONE);
	
			        listView.setVisibility(View.VISIBLE);
				}
				//listView.invalidate();
			}
        	
        };
        task.execute(page);
    	
    }
    
    public class ImageRowData{
    	public String url;
    	public String filename;
    }

	@Override  
    public void onLowMemory() {  
        super.onLowMemory();  
        LazyImageView.cleanMemory();
        Log.d(TAG, "application memory warning!!!");  
    } 
}