package org.qrone.android.dl;

import java.io.FileDescriptor;

import org.qrone.android.BaseService;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.IBinder.DeathRecipient;
import android.widget.Toast;

public class DownloaderService extends BaseService{

	@Override
    public void onCreate() {
        Toast.makeText(this, "create service", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStart(Intent intent, int StartId) {
        String message = intent.getStringExtra("Message");
        
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    //5ïbãxÇÒÇ≈èIóπ
                    sleep(5 * 1000);
                    stopSelf();
                } catch (InterruptedException e) {
                }
            }
        };
        t.start();
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "destroy service", Toast.LENGTH_SHORT).show();
    }
}
