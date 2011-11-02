package org.sbt.android;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

/**
 * This class exposes the remote service to the client
 */
public class AIDLService extends Service {

  @Override
  public void onCreate() {
    super.onCreate();
  }

  @Override
  public IBinder onBind(Intent intent) {

    return new IAdditionService.Stub() {
      public int add(int value1, int value2) throws RemoteException {
        return value1 + value2;
      }

    };
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
  }
}