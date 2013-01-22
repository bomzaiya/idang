package com.bomzaiya.ui;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.bomzaiya.app.idang.ProductConfig;
import com.bomzaiya.app.idang.R;
import com.bomzaiya.service.IdangService;
import com.bomzaiya.service.IdangService.IdangServiceBinder;
import com.bomzaiya.system.SystemHelper;

//httpMethod = "POST",
@ReportsCrashes(formKey = "", formUri = "http://bom.linegig.com/bug/arca.php", formUriBasicAuthLogin = "bom", formUriBasicAuthPassword = "bom", customReportContent = {
    ReportField.APP_VERSION_CODE, ReportField.ANDROID_VERSION, ReportField.PHONE_MODEL, ReportField.CUSTOM_DATA, ReportField.DEVICE_ID,
    ReportField.STACK_TRACE, ReportField.LOGCAT }, mode = ReportingInteractionMode.TOAST, resToastText = R.string.crash_toast_text)
public class IdangApplication extends Application {
  private ProductConfig mProductConfig = null;
  private ServiceConnection mIdangServiceConnection = null;
  protected IdangServiceBinder mIdangServiceBinder = null;
  public Intent mIdangService = null;

  @Override
  public void onCreate() {
    ACRA.init(this);
    String imei = SystemHelper.getPhoneIMEI(getBaseContext());
    String mac = SystemHelper.getPhoneMAC(getBaseContext());
    ACRA.getErrorReporter().putCustomData("IMEI", imei);
    ACRA.getErrorReporter().putCustomData("MAC", mac);
    ACRA.getErrorReporter().putCustomData("FINGI_APP_NAME", ProductConfig.APP_NAME);
    ACRA.getErrorReporter().putCustomData("FINGI_VERSION_NAME", ProductConfig.PRODUCT_CONFIG_NAME);
    super.onCreate();

  }

  public void setProductConfig(ProductConfig productConfig) {
    mProductConfig = productConfig;
  }

  public ProductConfig getProductConfig() {
    return mProductConfig;
  }

  public void startIdangService() {
    if (mIdangServiceConnection == null) {
      mIdangServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
          try {
            mIdangServiceBinder = (IdangServiceBinder) service;
            Log.i("fingi_INFO", "Fingi Service bound ");
          } catch (ClassCastException e) {
          }

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
          Log.i("fingi_INFO", "Fingi Service Unbound ");
        }
      };

      if (mIdangService == null) {
        mIdangService = new Intent(getBaseContext(), IdangService.class);
        mIdangService.setFlags(Context.BIND_NOT_FOREGROUND);
        // mIdangService.setFlags(Context.BIND_IMPORTANT);
        startService(mIdangService);
      }
      bindService(new Intent("com.bomzaiya.service.IdangService"), mIdangServiceConnection, 0);
    }
  }

  public void unbindIdangService() {
    try {
      if (mIdangServiceConnection != null) {
        unbindService(mIdangServiceConnection);
      }
      if (mIdangService != null) {
        stopService(mIdangService);
      }
      mIdangServiceBinder = null;
      mIdangServiceConnection = null;
      mIdangService = null;
    } catch (IllegalArgumentException e) {
    }
  }
}
