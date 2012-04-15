package org.qrone.android.dl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.qrone.android.BranchInputStream;
import org.qrone.android.Util;
import org.qrone.android.dl.DownloaderActivity.ImageRowData;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class LazyImageView extends ImageView{

	private static Map<String, Bitmap> cache;

	public LazyImageView(Context context) {
		super(context);
		init();
	}
	
    public LazyImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
		init();
    }
    
    public LazyImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
		init();
    }
    
    public void init(){
    	cache = new WeakHashMap<String, Bitmap>(10);
    }
    
    public static void cleanMemory(){
    	cache.clear();
    }
    
    private int width = -1;
    public void setWidth(int w){
    	this.width = w;
    }
    
    
    @Override
    public void setImageBitmap(Bitmap bitm){
		super.setImageBitmap(bitm);
		if(width >= 0 && bitm != null){
			float iw = bitm.getWidth();
			float ih = bitm.getHeight();
			float factor = ((float)width) / iw;
			Matrix m = getImageMatrix();
			m.reset();
			m.postScale(factor, factor);
			
			getLayoutParams().width = width;
			getLayoutParams().height = (int)(ih * factor);
		}
    }
	
	public void setURL(String src, final String filename){

		Bitmap bitm = cache.get(filename);
		if(bitm != null){
			setImageBitmap(bitm);
			invalidate();
			return;
		}
		
		setImageBitmap(null);
		invalidate();
		
		AsyncTask<String, Integer, Bitmap> task = new AsyncTask<String, Integer, Bitmap>(){

			@Override
			protected Bitmap doInBackground(String... params) {
				Bitmap bitm = null;
				if(!Util.exsitsFile("Android/data/org.qrone.dl", filename)){
					try{
						DefaultHttpClient dhc = new DefaultHttpClient();
						HttpResponse httpResponse = dhc.execute(new HttpGet(params[0]));
						if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
							HttpEntity httpEntity = httpResponse.getEntity();
							InputStream in = httpEntity.getContent();
							return memoryProcess(in);
						}
					}catch(IOException e){
						e.printStackTrace();
					}catch(OutOfMemoryError e){
						cache.clear();
						return null;
					}
				}else{
					return memoryProcess(filename);
				}
				
				return null;
			}

			private synchronized Bitmap memoryProcess(InputStream in){
				Bitmap bitm = null;
				try{
					OutputStream out = Util.saveFile("Android/data/org.qrone.dl", filename);
					
					InputStream inf = new BranchInputStream(in, out);
					bitm = BitmapFactory.decodeStream(inf);
					inf.close();
					cache.put(filename, bitm);
					
				}catch(IOException e){
					e.printStackTrace();
				}catch(OutOfMemoryError e){
					cache.clear();
					return null;
				}
				return bitm;
			}
			
			private synchronized Bitmap memoryProcess(String filename){
				Bitmap bitm = BitmapFactory.decodeStream(Util.loadFile("Android/data/org.qrone.dl", filename));
				cache.put(filename, bitm);
				return bitm;
			}
			
			@Override
			protected void onPostExecute(Bitmap bitm){
				if(bitm == null) return;
				
				setImageBitmap(bitm);
				invalidate();
			}
        	
        };
        task.execute(src);
	}
	
}
