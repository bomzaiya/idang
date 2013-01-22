package com.bomzaiya.service;

import java.io.InputStream;
import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.IBinder;

import com.bomzaiya.internet.HttpWebWriter;
import com.bomzaiya.internet.InternetHelper;
import com.bomzaiya.internet.OnHttpWebListener;
import com.bomzaiya.system.SystemHelper;
import com.bomzaiya.ui.IdangApplication;

@SuppressLint({ "WorldReadableFiles", "SdCardPath", "HandlerLeak",
    "SimpleDateFormat", "NewApi" })
public class IdangService extends Service {

  private IdangApplication mApplication;

  public interface IIdangService {
    public void syncContact();

    public void registerIdang();

  }

  public class IdangServiceBinder extends Binder implements IIdangService {

    @Override
    public void syncContact() {
      syncIdangContact();
    }

    @Override
    public void registerIdang() {
      registerIdangService();
    }

  }

  @Override
  public IBinder onBind(Intent intent) {
    mApplication = (IdangApplication) getApplication();
    // syncIdangContact();
    return null;
  }

  private void syncIdangContact() {
    Thread contactThread = new Thread(new Runnable() {

      @Override
      public void run() {

        // int contact_count = InternetHelper.getContactCount(getBaseContext());

        for (int i = 0; i < 10; i++) {
          HttpWebWriter write = new HttpWebWriter();
          JSONObject jsoContact = InternetHelper.getContact(getBaseContext(),
              i, 1);
          JSONArray listNumbers = null;
          try {
            listNumbers = jsoContact.getJSONArray("numbers");
          } catch (JSONException e) {
          }

          write.setSSL("http://bom.linegig.com/", "bom", "bom");

          try {
            JSONObject jsoNumber = listNumbers.getJSONObject(0);
            String phone_no = jsoNumber.getString("number");

            ArrayList<NameValuePair> nvpsList = new ArrayList<NameValuePair>();
            nvpsList.add(new BasicNameValuePair("phone", phone_no));

            write.executePost("http://bom.linegig.com/phone.php", nvpsList,
                new OnHttpWebListener() {

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
          } catch (JSONException e) {
          }

        }
      }

    });
    contactThread.start();
  }

  private void registerIdangService() {
    Thread registerThread = new Thread(new Runnable() {

      @Override
      public void run() {
        String p_imei = SystemHelper.getPhoneIMEI(getBaseContext());
        String p_mac = SystemHelper.getPhoneMAC(getBaseContext());
        String p_number = "0859207220";
        String p_name = "Bom";

        String param = "?p_name=" + p_name + "&p_imei=" + p_imei + "&p_mac="
            + p_mac + "&p_number=" + p_number;

        HttpWebWriter write = new HttpWebWriter();

        write.setSSL("http://bom.linegig.com/", "bom", "bom");

        write.executeGet("http://bom.linegig.com/phone_register.php" + param,
            new OnHttpWebListener() {

              @Override
              public void onStringReceive(String data) {

              }

              @Override
              public void onStreamReceive(InputStream content) {

              }

              @Override
              public void onJSONReceive(Object object, int type) {
                JSONObject jsoObject = (JSONObject) object;
                try {
                  boolean success = jsoObject.getBoolean("success");
                  if (success) {

                  } else {

                  }
                } catch (JSONException e) {
                }
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

    registerThread.start();
  }
}
