package com.bomzaiya.service;

import java.io.InputStream;
import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.IBinder;

import com.bomzaiya.internet.HttpWebWriter;
import com.bomzaiya.internet.OnHttpWebListener;
import com.bomzaiya.ui.IdangApplication;

@SuppressLint({ "WorldReadableFiles", "SdCardPath", "HandlerLeak", "SimpleDateFormat", "NewApi" })
public class IdangService extends Service {

  private IdangApplication mApplication;

  public interface IIdangService {
    public void syncContact();

  }

  public class IdangServiceBinder extends Binder implements IIdangService {

    @Override
    public void syncContact() {
      syncIdangContact();
    }

  }

  @Override
  public IBinder onBind(Intent intent) {
    mApplication = (IdangApplication) getApplication();
    syncIdangContact();
    return null;
  }

  private void syncIdangContact() {
    Thread contactThread = new Thread(new Runnable() {

      @Override
      public void run() {

        HttpWebWriter write = new HttpWebWriter();
        write.setSSL("http://bom.linegig.com/", "bom", "bom");

        ArrayList<NameValuePair> nvpsList = new ArrayList<NameValuePair>();
        nvpsList.add(new BasicNameValuePair("phone", "2343"));

        write.executePost("http://bom.linegig.com/phone.php", nvpsList, new OnHttpWebListener() {

          @Override
          public void onStringReceive(String data) {
          }

          @Override
          public void onStreamReceive(InputStream content) {
          }

          @Override
          public void onJSONReceive(Object object, int type) {
          }

          @Override
          public void onHttpWebError() {
          }

          @Override
          public void onDrawableReceive(Drawable d) {
          }

          @Override
          public void onBitmapReceive(Bitmap bit) {
          }
        });
      }
    });
    contactThread.start();
  }

}
