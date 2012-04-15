package org.qrone.android;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class BaseService extends Service{

	public class BaseBinder extends Binder {
		BaseService getService() {
			return BaseService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return new BaseBinder();
	}
	

	@Override
	public void onRebind(Intent intent) {
	}
	

	@Override
	public boolean onUnbind(Intent intent) {
		return true;
	}
	
}
