package com.bomzaiya.system;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;

import com.bomzaiya.app.idang.ProductConfig;
import com.bomzaiya.internet.InternetHelper;

public class SystemHelper {
  public static final String PREFERENCES = "PREFERENCES";
  public static final int PREFERENCE_MODE = 0;

  /**
   * Register for being notified of changes to the battery level. Note: After
   * registering, the callback method will be called immediately with the
   * current battery level
   * 
   * @param jsCallbackMethodName
   *          name of the JavaScript method to call on every change to the
   *          battery level. This method needs to have two parameter, the first
   *          receives the battery level as an integer value from 0 to 5, the
   *          second true when the battery is charging, false when not charging
   */
  public static BroadcastReceiver registerBatteryLevelListener(final Context context,
      final OnBatteryListener onBatteryListener) {
    BroadcastReceiver batteryChangedReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        onBatteryLevelIntent(intent, onBatteryListener);
      }
    };
    IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
    context.registerReceiver(batteryChangedReceiver, filter);
    onBatteryLevelIntent(context.registerReceiver(null, filter), onBatteryListener);
    return batteryChangedReceiver;
  }

  private static void onBatteryLevelIntent(Intent intent, OnBatteryListener onBatteryListener) {
    int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
    int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
    int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
    int scaled = (int) Math.round(((double) level / scale) * 5);
    int percent = (level * 100) / scale;

    boolean charged = false;
    if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
      charged = true;
    }

    onBatteryListener.onBatteryLevelChanged(percent, scaled, charged);
  }

  /**
   * Unregister any kind of broadcast receiver
   */
  public static void unregisterListener(Context context, BroadcastReceiver broadcastReceiver) {
    try {
      if (broadcastReceiver != null) {
        context.unregisterReceiver(broadcastReceiver);
        broadcastReceiver = null;
      }
    } catch (Exception e) {
      // e.printStackTrace();
    }
  }

  public static String getLogFileName(String prefix) {
    Calendar calendar = Calendar.getInstance();
    int day = calendar.get(Calendar.DAY_OF_MONTH);
    int month = calendar.get(Calendar.MONTH) + 1;
    int year = calendar.get(Calendar.YEAR);
    String filename = ProductConfig.IDANG_FOLDER + "/" + prefix + year + "-" + String.format("%02d", month) + "-"
        + String.format("%02d", day) + ".log";
    return filename;
  }

  public static String getLogLine(String text) {
    Calendar calendar = Calendar.getInstance();
    int day = calendar.get(Calendar.DAY_OF_MONTH);
    int month = calendar.get(Calendar.MONTH) + 1;
    int year = calendar.get(Calendar.YEAR);
    int hour = calendar.get(Calendar.HOUR_OF_DAY);
    int min = calendar.get(Calendar.MINUTE);
    int sec = calendar.get(Calendar.SECOND);
    int milisec = calendar.get(Calendar.MILLISECOND);
    String log = year + "-" + String.format("%02d", month) + "-" + String.format("%02d", day) + " "
        + String.format("%02d", hour) + ":" + String.format("%02d", min) + ":" + String.format("%02d", sec) + "_"
        + String.format("%04d", milisec) + "=" + text;
    return log;
  }

  public static void writeActivityLog(Context context, String tag, String log) {
    NetworkInfo networkInfo = InternetHelper.getActiveNetworkInfo(context);
    String networktype = "";
    if (networkInfo == null) {
      networktype = "OFFLINE";
    } else {
      if (networkInfo.getType() == -1) {
        networktype = "OFFLINE";
      } else {
        if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
          networktype = "WIFI";
        } else {
          networktype = "MOBILE," + networkInfo.getSubtypeName();
        }
      }
    }

    SystemHelper.appendRequiredLog("activity_", networktype + ":" + tag + ":" + log);
  }

  public static boolean isEpgTempExist(Context context) {
    File epgCurrentFile = new File(ProductConfig.IDANG_FOLDER + "/epg_json.txt");
    return epgCurrentFile.exists();
  }

  public static void writeEpgTemp(Context context, String epgJson) {

    try {
      JSONObject jsoEpg = new JSONObject(epgJson);

      File epgFile = new File(ProductConfig.IDANG_FOLDER + "/epg_json.txt");

      if (epgFile.exists()) {
        epgFile.delete();
      }

      try {
        epgFile.createNewFile();
      } catch (IOException e) {
      }

      try {
        // BufferedWriter for performance, true to set append to file flag
        BufferedWriter buf = new BufferedWriter(new FileWriter(epgFile, true));
        buf.write(jsoEpg.toString());
        buf.close();
      } catch (IOException e) {
      }
    } catch (JSONException e1) {
    }

  }

  public static String readEpgTemp(Context context) {
    File epgFile = new File(ProductConfig.IDANG_FOLDER + "/epg_json.txt");

    String epgJson = "";
    if (epgFile.exists()) {
      try {
        FileInputStream fis = new FileInputStream(epgFile);

        int c = 0;

        try {
          while ((c = fis.read()) != -1) {
            char ch = (char) c;
            epgJson += ch;
          }
        } catch (IOException e) {
        }
      } catch (FileNotFoundException e) {
      }
    }

    return epgJson;
  }

  /**
   * Append required log actually this is the same as appendLog, but will not be
   * hided when production version
   * 
   * @param prefix
   * @param text
   */
  public static void appendRequiredLog(String prefix, String text) {
    File logFile = new File(getLogFileName(prefix));

    String log = getLogLine(text);

    if (!logFile.exists()) {
      try {
        logFile.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    try {
      // BufferedWriter for performance, true to set append to file flag
      BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
      buf.append(log);
      buf.newLine();
      buf.close();
    } catch (IOException e) {
    }
  }

  public static void appendLog(String prefix, String text) {
    // production version will not write any log
    // if (ProductConfig.getProductVersion() ==
    // SiteConfig.PRODUCT_VERSION_PRODUCTION) {
    // return;
    // }

    File logFile = new File(getLogFileName(prefix));

    String log = getLogLine(text);

    if (!logFile.exists()) {
      try {
        logFile.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    try {
      // BufferedWriter for performance, true to set append to file flag
      BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
      buf.append(log);
      buf.newLine();
      buf.close();
    } catch (IOException e) {
    }
  }

  public static void clearLog(String prefix) {
    File logFile = new File(getLogFileName(prefix));

    long uptime = SystemClock.uptimeMillis();
    String log = getLogLine("LOGSTART:UPTIME," + uptime);

    if (!logFile.exists()) {
      try {
        logFile.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    try {
      // BufferedWriter for performance, true to set append to file flag
      BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, false));
      buf.write(log);
      buf.newLine();
      buf.close();
    } catch (IOException e) {
    }
  }

  public static String readLog(String prefix) {
    File logFile = new File(getLogFileName(prefix));
    String log = "";
    try {
      FileInputStream fis = new FileInputStream(logFile);

      int c = 0;

      try {
        while ((c = fis.read()) != -1) {
          char ch = (char) c;
          log += ch;
        }
      } catch (IOException e) {
      }
    } catch (FileNotFoundException e) {
    }

    return log;
  }

  /**
   * set shared preference value
   * 
   * @param prefId
   * @param prefValue
   */
  public static void setSharePreferenceValue(Context context, String prefId, String prefValue) {
    SharedPreferences pref = context.getSharedPreferences(PREFERENCES, PREFERENCE_MODE);
    SharedPreferences.Editor editor = pref.edit();

    editor.putString(prefId, prefValue);
    editor.commit();
  }

  public static String getSharePreferenceValue(Context context, String prefId) {
    SharedPreferences sharePrefs = context.getSharedPreferences(PREFERENCES, PREFERENCE_MODE);
    return sharePrefs.getString(prefId, "");
  }

  public static String getPhoneIMEI(Context context) {
    // get imsi (sim) and imei (phone) codes, for the asterisk integration
    TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    return telephonyManager.getDeviceId();
  }

  public static String getPhoneMAC(Context context) {
    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    WifiInfo wifiInf = wifiManager.getConnectionInfo();
    return wifiInf.getMacAddress();
  }

  public static int getSimState(Context context) {
    TelephonyManager telMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    return telMgr.getSimState();
  }

  public static void changeLocale(Context context, String localeText) {
    Locale locale = new Locale(localeText);
    Locale.setDefault(locale);
    Configuration config = new Configuration();
    config.locale = locale;
    context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
  }

  public static String getLocale(Context context) {
    Configuration config = context.getResources().getConfiguration();
    Locale locale = config.locale;
    return locale.getLanguage();
  }

  public static String getStringByResId(Context context, String resourceString) {
    int resId = context.getResources().getIdentifier(resourceString, "string", context.getPackageName());
    String text = "";
    try {
      text = context.getString(resId);
    } catch (android.content.res.Resources.NotFoundException e) {
    }
    return text;
  }

  public static String convertTextToLookup(String text) {
    String lookup = text.toLowerCase().trim();
    lookup = lookup.replace(" ", "_");
    return lookup;
  }

  public static String getIDANGAssetFolder(Context context) {
    float displayDensity = context.getResources().getDisplayMetrics().density;
    String IDANGFolder = "IDANG";

    // 0.75 - ldpi
    //
    // 1.0 - mdpi
    //
    // 1.5 - hdpi
    //
    // 2.0 - xhdpi
    //
    // 3.0 - xxdpi

    // if (displayDensity == 2.0) {
    // IDANGFolder = "IDANG";//-sw320dp";
    // } else {
    // IDANGFolder = "IDANG";
    // }

    return IDANGFolder;
  }

  public static Drawable readAssetDrawable(Context context, String filename, int imageLocation) {
    int width = getDynamicImageWidth(context, imageLocation);
    int height = getDynamicImageHeight(context, imageLocation);
    return _readAssetDrawable(context, filename, width, height);
  }
 
  public static Drawable _readAssetDrawable(Context context, String filename, int width, int height) {
    try {
      BitmapFactory.Options options = new BitmapFactory.Options();
      options.inPurgeable = true;

      DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
      float displayDensity = displayMetrics.density;

      if (displayDensity == 1.5) {
        options.inScaled = true;
        options.inDensity = Math.round(displayMetrics.densityDpi * displayDensity);
      } else if (displayDensity == 1.0) {
        options.inScaled = true;
        options.inDensity = Math.round(displayMetrics.densityDpi * 2);
      } else if (displayDensity == 0.75) {
        options.inScaled = true;
        options.inDensity = Math.round(Float.parseFloat(displayMetrics.densityDpi + "") * 2.5f);
      }

      InputStream is = context.getResources().getAssets().open(filename);
      Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);

      Bitmap resizedbitmap = null;
      int newWidth = 0;
      int newHeight = 0;
      int startX = 0;
      int startY = 0;
      int bitmapWidth = bitmap.getWidth();
      int bitmapHeight = bitmap.getHeight();
      if (width > 0 && height > 0) {

        if (bitmapWidth > width) {
          startX = (bitmapWidth - width) / 2;
          newWidth = width;
        } else {
          newWidth = bitmapWidth;
        }

        if (bitmapHeight > height) {
          startY = (bitmapHeight - height) / 2;
          newHeight = height;
        } else {
          newHeight = bitmapHeight;
        }

      } else {

        if (bitmapWidth > displayMetrics.widthPixels) {
          startX = (bitmapWidth - displayMetrics.widthPixels) / 2;
          newWidth = displayMetrics.widthPixels;
        } else {
          newWidth = bitmapWidth;
        }

        if (bitmapHeight > displayMetrics.heightPixels) {
          startY = (bitmapHeight - displayMetrics.heightPixels) / 2;
          newHeight = displayMetrics.heightPixels;
        } else {
          newHeight = bitmapHeight;
        }
      }

      resizedbitmap = Bitmap.createBitmap(bitmap, startX, startY, newWidth, newHeight);

      Drawable drawable = null;
      if (displayDensity < 2.0) {
        drawable = new BitmapDrawable(context.getResources(), resizedbitmap);
        return drawable;
      } else {
        if (bitmap != null) {
          drawable = new BitmapDrawable(context.getResources(), resizedbitmap);
          return drawable;
        }
      }

    } catch (IOException e) {
    }
    return null;
  }

  public static int getDynamicImageWidth(Context context, int imageLocation) {
    DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();

    if (displayMetrics.density == 1f) {
      return ProductConfig.DENSITY_WIDTH_1F.get(imageLocation);
    } else {
      return ProductConfig.DENSITY_WIDTH_1F.get(imageLocation);
    }
  }

  public static int getDynamicImageHeight(Context context, int imageLocation) {
    DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();

    if (displayMetrics.density == 1f) {
      return ProductConfig.DENSITY_HEIGHT_1F.get(imageLocation);
    } else {
      return ProductConfig.DENSITY_HEIGHT_1F.get(imageLocation);
    }
  }
}
