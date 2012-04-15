package org.qrone.android;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

public class BaseActivity extends Activity{
	protected boolean isBound;
	protected BaseService service;
	protected ServiceConnection connection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder b) {
			service = (BaseService)(((BaseService.BaseBinder)b).getService());
			BaseActivity.this.onServiceConnected(service);
		}

		public void onServiceDisconnected(ComponentName className) {
			BaseActivity.this.onServiceDisconnected(service);
			service = null;
		}
	};
	
	protected void onServiceConnected(BaseService service){
	}
	
	protected void onServiceDisconnected(BaseService service){
	}
	
	protected void bindService(Intent intent) {
	    bindService(intent, connection, Context.BIND_AUTO_CREATE);
	    isBound = true;
	}
	 
	protected void unbindService() {
	    if (isBound) {
	        unbindService(connection);
	        isBound = false;
	    }
	}

}
