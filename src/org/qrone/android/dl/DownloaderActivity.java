package org.qrone.android.dl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.qrone.android.util.LazyImageView;
import org.qrone.android.util.WebAsyncTask;
import org.qrone.android.util.WebSource;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DownloaderActivity extends Activity {
	private static final String TAG = "Activity";
	private static final Pattern urlptn = Pattern.compile("http://4u-beautyimg.com/thumb/l/(l[a-zA-Z0-9/\\._\\-=\\+&#]+)");
	
	
	private int page = 0;
	private ListView listView;
	private List<ImageRowData> list;
	private View llcache;
	
	
	private int getScreenWidth(){
		WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
		Display disp = wm.getDefaultDisplay();
		return disp.getWidth();
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
	    
        super.onCreate(savedInstanceState);
        setContentView(R.layout.imglist);
        

        list = new ArrayList<ImageRowData>();
        loadPage(++page);
        
        final View.OnLongClickListener l = new View.OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				LazyImageView lv = (LazyImageView)v.findViewById(R.id.imgView);
				
				Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, lv.getURL());
                startActivity(intent);
                
				return false;
			}
		};
        
        listView = (ListView)findViewById(R.id.listView);
        listView.setAdapter(new BaseAdapter() {
			
			public View getView(int position, View convertView, ViewGroup parent) {
				if(convertView == null){
					LayoutInflater inf = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					convertView = inf.inflate(R.layout.imglist_row, null);
			        Log.d(TAG, "Create ImgView");
			        
			        LazyImageView v = (LazyImageView)convertView.findViewById(R.id.imgView);
			        v.setOnLongClickListener(l);
				}
				
				if(position == list.size()){
					convertView.findViewById(R.id.imgView).setVisibility(View.GONE);
					convertView.findViewById(R.id.imgView_last).setVisibility(View.VISIBLE);
			        loadPage(++page);
			        
			        TextView tv = (TextView)convertView.findViewById(R.id.textView1).findViewById(R.id.current_action);
			        tv.setText(getString(R.string.loading_page_x).replace("%", String.valueOf(page)));
				}else{
					convertView.findViewById(R.id.imgView).setVisibility(View.VISIBLE);
					convertView.findViewById(R.id.imgView_last).setVisibility(View.GONE);

			        
					ImageRowData rd = list.get(position);
					LazyImageView v = (LazyImageView)convertView.findViewById(R.id.imgView);
			        v.setLongClickable(true);
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
    }
    
    public void loadPage(final int page){
    	WebAsyncTask task = new WebAsyncTask() {
			
			@Override
			protected void onPostExecute(String[] results) {
				for (int i = 0; i < results.length; i++) {
					
					Matcher m = urlptn.matcher(results[i]);

					while(m.find()){
						MatchResult mr = m.toMatchResult();
						String sub = results[i].substring(mr.start(), mr.end());

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
				}
				
			}
		};

		if(page == 1){
			task.execute(new WebSource("http://4u-beautyimg.com/"));	
		}else{
			task.execute(new WebSource("http://4u-beautyimg.com/?page=" + page));
		}
    	
    }
    
    public class ImageRowData{
    	public String url;
    	public String filename;
    }
}