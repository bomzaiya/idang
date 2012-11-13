package com.bomzaiya.internet;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

public class InternetHelper {
  private static final Uri SMS_QUERY_URI = Uri.parse("content://sms");
  private static final Uri SMS_INBOX_QUERY_URI = Uri.parse("content://sms/inbox");
  private static final Uri SMS_SENT_QUERY_URI = Uri.parse("content://sms/sent");
  private static final Uri SMS_NOTIFICATION_URI = Uri.parse("content://mms-sms");

  public static final String SMS_COLUMN_NAME_ID = "_id";
  public static final String SMS_COLUMN_NAME_ADDRESS = "address";
  public static final String SMS_COLUMN_NAME_PERSON = "person";
  public static final String SMS_COLUMN_NAME_BODY = "body";
  public static final String SMS_COLUMN_NAME_TYPE = "type";
  public static final String SMS_COLUMN_NAME_DATE = "date";
  public static final String SMS_COLUMN_NAME_SMSC = "service_center";
  public static final String SMS_COLUMN_NAME_READ = "read";

  public static final int SMS_UNREAD = 0;
  public static final int SMS_READ = 1;
  public static final int SMS_ALL = 2;

  private static final String PREF_LAST_SEEN_SMS_ID = "PREF_LAST_SEEN_SMS_ID";
  private static final String PREF_LAST_SEEN_SMS_ID_SENT = "PREF_LAST_SEEN_SMS_ID_SENT";

  public static int getSMSCount(Context context, int read) {
    String filter = null;
    if (SMS_ALL != read) {
      filter = "read = " + read;
    }

    Cursor cursor = context.getContentResolver().query(SMS_INBOX_QUERY_URI, null, filter, null,
        SMS_COLUMN_NAME_DATE + " desc");
    int count = 0;
    if (cursor != null) {
      count = cursor.getCount();
    }
    return count;
  }

  /**
   * Get all SMS in the Inbox as a JSON array.
   * 
   * @return a JSON Object with a property <code>list</code> which is an array
   *         of all SMS on the device, ordered by date descending (i.e. newest
   *         first)
   * @see {@link AndroidJavaScriptGlue#registerIncomingSMSListener(String)} for
   *      a description of the JSON object for every single SMS
   */
  public static JSONObject getSMSInbox(Context context, int from, int length) {
    try {
      JSONObject result = new JSONObject();
      JSONArray list = new JSONArray();
      result.put("list", list);
      Cursor cursor = context.getContentResolver().query(SMS_INBOX_QUERY_URI, null, null, null,
          SMS_COLUMN_NAME_DATE + " desc");
      if (cursor != null) {
        int columnIndexId = cursor.getColumnIndex(SMS_COLUMN_NAME_ID);
        int columnIndexDate = cursor.getColumnIndex(SMS_COLUMN_NAME_DATE);
        int columnIndexAddress = cursor.getColumnIndex(SMS_COLUMN_NAME_ADDRESS);
        int columnIndexBody = cursor.getColumnIndex(SMS_COLUMN_NAME_BODY);
        int columnIndexSmsc = cursor.getColumnIndex(SMS_COLUMN_NAME_SMSC);
        int columnIndexRead = cursor.getColumnIndex(SMS_COLUMN_NAME_READ);

        if (length > 0) {
          int index = 0;
          while (cursor.moveToNext()) {
            index++;
            if (index >= from && index < from + length) {
              JSONObject jsoSMS = getSmsAsJsonFromCursor(cursor, columnIndexId, columnIndexDate, columnIndexAddress,
                  columnIndexBody, columnIndexSmsc, columnIndexRead);
              list.put(jsoSMS);
            }
          }
        } else {
          while (cursor.moveToNext()) {
            JSONObject jsoSMS = getSmsAsJsonFromCursor(cursor, columnIndexId, columnIndexDate, columnIndexAddress,
                columnIndexBody, columnIndexSmsc, columnIndexRead);
            list.put(jsoSMS);
          }
        }
        cursor.close();
        return result;
      } else {
        return null;
      }
    } catch (JSONException e) {
      return null;
    }

  }

  private static JSONObject getSmsAsJsonFromCursor(Cursor cursor, int columnIndexId, int columnIndexDate,
      int columnIndexAddress, int columnIndexBody, int columnIndexSmsc, int columnIndexRead) throws JSONException {
    JSONObject jsoSMS = new JSONObject();
    jsoSMS.put("id", cursor.getLong(columnIndexId));
    jsoSMS.put("date", cursor.getLong(columnIndexDate));
    jsoSMS.put("address", cursor.getString(columnIndexAddress));
    jsoSMS.put("body", cursor.getString(columnIndexBody));
    jsoSMS.put("smsc", cursor.getString(columnIndexSmsc));
    jsoSMS.put("read", cursor.getInt(columnIndexRead) == 1);
    return jsoSMS;
  }

  public static int getCallLogCount(Context context) {
    Cursor cursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null,
        CallLog.Calls.DATE + " DESC ");
    if (cursor != null) {
      return cursor.getCount();
    } else {
      return 0;
    }

  }

  /**
   * Get the complete Android call log
   * 
   * @return a JSON Object with a property <code>list</code> which is an array
   *         of all CallLog entries on the device, ordered by date descending
   *         (i.e. newest first).<br/>
   *         Every entry in that array has the following properties:<br/>
   *         <code>number:</code> phone number of the remote party</br>
   *         <code>cached_name:</code> name of the remote party (as per the
   *         addressbook at the time of the call)</br> <code>duration:</code>
   *         duration in seconds</br> <code>date:</code> date of the call</br>
   *         <code>type:</code> type of the call, 1 = INCOMING, 2 = OUTGOING, 3
   *         = MISSED</br>
   * 
   */
  public static JSONObject getCallLog(Context context, int from, int length) {
    JSONObject result = new JSONObject();
    try {

      JSONArray list = new JSONArray();
      result.put("list", list);
      Cursor cursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null,
          CallLog.Calls.DATE + " DESC ");
      if (cursor != null) {
        int colIndexDate = cursor.getColumnIndex(CallLog.Calls.DATE);
        int colIndexNumber = cursor.getColumnIndex(CallLog.Calls.NUMBER);
        int colIndexCachedName = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME);
        int colIndexDuration = cursor.getColumnIndex(CallLog.Calls.DURATION);
        int colIndexType = cursor.getColumnIndex(CallLog.Calls.TYPE);
        int index = 0;
        while (cursor.moveToNext()) {
          index++;

          if (index >= from && index < from + length) {
            JSONObject jsoCall = new JSONObject();
            jsoCall.put("number", cursor.getString(colIndexNumber));
            jsoCall.put("cached_name", cursor.getString(colIndexCachedName));
            jsoCall.put("duration", cursor.getInt(colIndexDuration));
            jsoCall.put("date", cursor.getString(colIndexDate));
            jsoCall.put("type", cursor.getInt(colIndexType));
            list.put(jsoCall);
          }

        }
        cursor.close();
      }
    } catch (JSONException e) {
    }
    return result;

  }

  public static int getContactCount(Context context) {
    Cursor cursor = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
    if (cursor != null) {
      return cursor.getCount();
    } else {
      return 0;
    }
  }

  /**
   * Get the complete Android addressbook
   * 
   * @return a JSON Object with a property <code>list</code> which is an array
   *         of all contact entries on the device, ordered by name <br/>
   *         Every entry in that array has the following properties:<br/>
   *         <code>id:</code> id</br> <code>display_name:</code> display
   *         name</br> <code>numbers:</code> All the phone numbers of that
   *         contact as an array of JSON objects, each having the following
   *         properties:<br/>
   *         <code>number:</code> the phone number</br> <code>type:</code>see
   *         http://developer.android.com/reference/android/provider/
   *         ContactsContract.CommonDataKinds.Phone.html for the constant
   *         valued</br>
   */
  public static JSONObject getContact(Context context, int from, int length) {
    JSONObject result = new JSONObject();
    try {

      JSONArray list = new JSONArray();
      result.put("list", list);
      Cursor cursor = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

      if (cursor != null) {
        int colIndexId = cursor.getColumnIndex(ContactsContract.Contacts._ID);
        int colIndexDisplayName = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
        int colIndexHasPhoneNumber = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);
        int index = 0;
        while (cursor.moveToNext()) {
          index++;

          if (index >= from && index < from + length) {
            JSONObject jsoContact = new JSONObject();
            String id = cursor.getString(colIndexId);
            jsoContact.put("id", id);
            jsoContact.put("display_name", cursor.getString(colIndexDisplayName));

            // phone numbers

            JSONArray listNumbers = new JSONArray();
            jsoContact.put("numbers", listNumbers);
            if (Integer.parseInt(cursor.getString(colIndexHasPhoneNumber)) > 0) {

              Cursor cursorNumbers = context.getContentResolver().query(
                  ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                  ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[] { id }, null);
              while (cursorNumbers.moveToNext()) {
                JSONObject jsoNumber = new JSONObject();
                jsoNumber.put("number", cursorNumbers.getString(cursorNumbers
                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                jsoNumber.put("type",
                    cursorNumbers.getString(cursorNumbers.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)));
                listNumbers.put(jsoNumber);
              }
              cursorNumbers.close();
            }

            list.put(jsoContact);
          }
        }
        cursor.close();
      }

    } catch (JSONException e) {
    }
    return result;

  }

  public static void openWifiManager(Context context) {
    Intent intent = new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(intent);
  }

  public static BroadcastReceiver registerWifiSignalLevelListener(Context context, final OnWifiListener onWifiListener) {

    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

    BroadcastReceiver wifiRssiReceiver = new BroadcastReceiver() {

      @Override
      public void onReceive(Context context, Intent intent) {
        int rssi = intent.getIntExtra(WifiManager.EXTRA_NEW_RSSI, 0);
        onWifiSignal(rssi, onWifiListener);
      }
    };

    context.registerReceiver(wifiRssiReceiver, new IntentFilter(WifiManager.RSSI_CHANGED_ACTION));

    WifiInfo conInfo = wifiManager.getConnectionInfo();
    if (conInfo != null) {
      onWifiSignal(conInfo.getRssi(), onWifiListener);
    } else {
      onWifiSignal(0, onWifiListener);
    }
    return wifiRssiReceiver;
  }

  private static void onWifiSignal(int rssi, OnWifiListener onWifiListener) {
    int scaled = WifiManager.calculateSignalLevel(rssi, 6);
    onWifiListener.onWifiLevelChanged(scaled);
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
    }
  }

  /**
   * register connectivity broadcast
   * 
   * @param context
   * @param onWifiListener
   */
  public static BroadcastReceiver registerConnectivityListener(Context context, final OnWifiListener onWifiListener) {

    final ConnectivityManager connectivityManager = (ConnectivityManager) context
        .getSystemService(Context.CONNECTIVITY_SERVICE);
    BroadcastReceiver connectivityBroadcastReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        onWifiListener.onWifiStatusChanged(networkInfo);
      }
    };
    context.registerReceiver(connectivityBroadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION),
        null, null);

    return connectivityBroadcastReceiver;
  }

  public static NetworkInfo getActiveNetworkInfo(Context context) {
    ConnectivityManager connectivityManager = (ConnectivityManager) context
        .getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

    return networkInfo;
  }

  /**
   * open dialer
   * 
   * @param context
   */
  public static void openDialer(Context context) {
    Intent dialIntent = new Intent(Intent.ACTION_DIAL);
    dialIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(dialIntent);
  }

  /**
   * open messaging list
   * 
   * @param context
   */
  public static void openMessaging(Context context) {
    Intent launch_intent = new Intent(Intent.ACTION_MAIN);
    launch_intent.setType("vnd.android-dir/mms-sms");
    launch_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(launch_intent);
  }

  /**
   * Register for being notified of changes to the GSM signal level. Note: After
   * registering, the callback method will be called immediately with the
   * current GSM signal level
   * 
   * @param jsCallbackMethodName
   *          name of the JavaScript method to call on every change to the GSM
   *          signal level. This method needs to have one parameter which
   *          receives the signal level as an integer value from 1 to 5, or "0"
   *          when there is no GSM signal
   */
  public static PhoneStateListener registerGSMSignalLevelListener(Context context, final OnGsmListener onGsmListener) {
    final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    PhoneStateListener phoneStateListener = new PhoneStateListener() {

      @Override
      public void onSignalStrengthsChanged(SignalStrength signalStrength) {
        int scaled = 0;
        if (telephonyManager.getNetworkType() != 0) {
          try {
            // Workaround for strange samsung galaxy SII
            // which
            // is always returning 99.
            // see
            // http://code.google.com/p/android/issues/detail?id=16862
            Field f = signalStrength.getClass().getDeclaredField("mGsmSignalBar");
            // if we get this field we probably are on such a
            // problem device

            f.setAccessible(true);
            scaled = 1 + (Integer) f.get(signalStrength);
          } catch (Exception e) {
            int si = signalStrength.getGsmSignalStrength();
            if (si == 99) {
              scaled = 0;
            }

            if (si > 0) {
              scaled = 1;
            }
            if (si >= 5) {
              scaled = 2;
            }
            if (si >= 8) {
              scaled = 3;
            }
            if (si >= 12) {
              scaled = 4;
            }

          }
        }

        onGsmListener.onGsmLevelChanged(scaled, telephonyManager);
      }
    };

    telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

    return phoneStateListener;
  }

  /**
   * Unregister for being notified of WiFi signal level events
   */
  public static void unregisterGSMSignalLevelListener(Context context, PhoneStateListener phoneStateListener) {
    try {
      if (phoneStateListener != null) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        phoneStateListener = null;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void dial(Context context, String number) {
    Intent dialIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number));
    dialIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(dialIntent);
  }

  public static String lookupNetworkSubTypeAbbreviate(NetworkInfo networkInfo) {
    String abbreviate = "";
    if (networkInfo != null) {
      String subtype = networkInfo.getSubtypeName();
      if (subtype.equals("HSPA") || subtype.equals("HSDPA")) {
        abbreviate = "H";
      } else if (subtype.equals("HSPA+")) {
        abbreviate = "H+";
      } else if (subtype.equals("UMTS")) {
        abbreviate = "3G";
      } else if (subtype.equals("GPRS")) {
        abbreviate = "G";
      } else if (subtype.equals("EDGE")) {
        abbreviate = "E";
      } else {
        abbreviate = subtype;
      }
    }

    return abbreviate;

  }

  /**
   * Enable or disable WiFi at all
   * 
   * @param enabled
   */
  public static void setWifiEnabled(Context context, boolean enabled) {
    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

    wifiManager.setWifiEnabled(enabled);
  }

  /**
   * @return <code>true</code> if WiFi is enabled
   */
  public static boolean isWifiEnabled(Context context) {
    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    return wifiManager.isWifiEnabled();
  }

  // ------------------------------------------------
  // NOTE: the following two methods are not guaranteed to work
  // in fact they do no work on an HTC Hero and on an HTC Legend
  // however they *MIGHT* work on a Samsung Galaxy S2

  /**
   * @return <code>true</code>if operation was successful
   */
  public static boolean disableWifiTethering(Context context) {
    try {
      WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
      Method[] wmMethods = wifiManager.getClass().getDeclaredMethods();
      for (Method method : wmMethods) {
        if (method.getName().equals("setWifiApEnabled")) {
          WifiConfiguration netConfig = new WifiConfiguration();
          Boolean res = (Boolean) method.invoke(wifiManager, netConfig, false);
          return res;
        }
      }

    } catch (Exception e) {
      // e.printStackTrace();
    }
    return false;
  }

  /**
   * Check if tethering is on/off
   * 
   * @return <code>true</true> if tethering is on
   */
  public static boolean isTetheringEnable(Context context) {
    try {
      WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
      Method[] wmMethods = wifiManager.getClass().getDeclaredMethods();
      for (Method method : wmMethods) {
        if (method.getName().equals("isWifiApEnabled")) {
          Boolean res = (Boolean) method.invoke(wifiManager);
          return res;
        }
      }
    } catch (Exception e) {
      // e.printStackTrace();
    }
    return false;
  }

  /**
   * @param ssid
   *          of the AP to create. Can either be an ASCII string, which must be
   *          enclosed in double quotation marks (e.g., "MyNetwork", or a string
   *          of hex digits,which are not enclosed in quotes (e.g., 01a243f405)
   * @param presharedKey
   *          pre-shared WPA secret key
   * @return <code>true</code>if operation was successful
   */
  public static boolean enableWifiTetheringWPA(Context context, String ssid, String presharedKey) {
    try {
      WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
      Method[] wmMethods = wifiManager.getClass().getDeclaredMethods();
      for (Method method : wmMethods) {
        if (method.getName().equals("setWifiApEnabled")) {
          WifiConfiguration netConfig = new WifiConfiguration();
          netConfig.SSID = ssid;
          netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
          netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
          netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
          netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
          netConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
          netConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
          netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
          netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
          netConfig.preSharedKey = presharedKey;
          Boolean res = (Boolean) method.invoke(wifiManager, netConfig, true);
          return res;
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;

  }

  /**
   * get current active wifi info
   * 
   * @param context
   * @return
   */
  public static WifiInfo getActiveWifiInfo(Context context) {
    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

    return wifiManager.getConnectionInfo();
  }

  /**
   * turn on/off 3g
   * 
   * @param context
   * @param enabled
   */
  @SuppressWarnings({ "rawtypes", "unchecked", "unused" })
  public static void setMobileDataEnabled(Context context, boolean toBeEnabled) {

    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

    Class c = null;
    try {
      c = Class.forName(cm.getClass().getName());
    } catch (ClassNotFoundException e2) {
      // TODO Auto-generated catch block
      e2.printStackTrace();
    }
    Method m = null;
    try {
      m = c.getDeclaredMethod("getMobileDataEnabled");
    } catch (SecurityException e2) {
      // TODO Auto-generated catch block
      e2.printStackTrace();
    } catch (NoSuchMethodException e2) {
      // TODO Auto-generated catch block
      e2.printStackTrace();
    }
    Object mobileDataEnabled = null;
    if (m != null) {
      m.setAccessible(true);
      Type res_of_m = m.getGenericReturnType();
      Type[] pars_of_m = m.getGenericParameterTypes();
      try {
        mobileDataEnabled = (m.invoke(cm));
        if (mobileDataEnabled != null)
          if (mobileDataEnabled.equals(!toBeEnabled)) {
            Method m2 = null;
            try {
              int index = 0;
              boolean method_found = false;
              Method[] available_methods = c.getDeclaredMethods();
              for (Method method : available_methods) {
                // following line doesn't work
                // method.getName()=="setMobileDataEnabled"
                if (method.getName().contains("setMobileDataEnabled")) {
                  method_found = true;
                }

                if (method_found == false)
                  index++;
              }
              // following line doesn't work
              // m2 = c.getDeclaredMethod("setMobileDataEnabled");
              m2 = (c.getDeclaredMethods())[index];
              if (m2 != null) {
                m2.setAccessible(true);
                m2.invoke(cm, toBeEnabled);
              }
            } catch (SecurityException e2) {
              // TODO Auto-generated catch block
              e2.printStackTrace();
            } catch (InvocationTargetException e2) {
              // TODO Auto-generated catch block
              e2.printStackTrace();
            }
          }
      } catch (IllegalArgumentException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  /**
   * @return null if unconfirmed
   */
  public static boolean isMobileDataEnabled(Context context) {
    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

    try {
      Class<?> c = Class.forName(cm.getClass().getName());
      Method m = c.getDeclaredMethod("getMobileDataEnabled");
      m.setAccessible(true);
      return (Boolean) m.invoke(cm);
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }


  public static JSONObject readSMS(Context context, long smsId) {
    String[] selectionArgs = { "" + smsId };
    Cursor cursor = context.getContentResolver().query(SMS_INBOX_QUERY_URI, null, SMS_COLUMN_NAME_ID + " = ?",
        selectionArgs, SMS_COLUMN_NAME_DATE + " desc");
    JSONObject jsoSms = null;
    if (cursor.moveToNext()) {
      int columnIndexId = cursor.getColumnIndex(SMS_COLUMN_NAME_ID);

      int columnIndexDate = cursor.getColumnIndex(SMS_COLUMN_NAME_DATE);
      int columnIndexAddress = cursor.getColumnIndex(SMS_COLUMN_NAME_ADDRESS);
      int columnIndexBody = cursor.getColumnIndex(SMS_COLUMN_NAME_BODY);
      int columnIndexSmsc = cursor.getColumnIndex(SMS_COLUMN_NAME_SMSC);
      int columnIndexRead = cursor.getColumnIndex(SMS_COLUMN_NAME_READ);

      try {
        jsoSms = getSmsAsJsonFromCursor(cursor, columnIndexId, columnIndexDate, columnIndexAddress, columnIndexBody,
            columnIndexSmsc, columnIndexRead);
      } catch (JSONException e) {
      }
    }
    cursor.close();
    return jsoSms;
  }

  /**
   * Delete an SMS from the device
   * 
   * @param id
   *          id of the SMS to delete
   */
  public static void markSMSasRead(Context context, long id, boolean read) {
    ContentValues values = new ContentValues();
    values.put(SMS_COLUMN_NAME_READ, read ? 1 : 0);
    int rows = context.getContentResolver().update(SMS_QUERY_URI, values, SMS_COLUMN_NAME_ID + "=?",
        new String[] { String.valueOf(id) });
  }

  /**
   * Delete an SMS from the device
   * 
   * @param id
   *          id of the SMS to delete
   */
  public static void deleteSMS(Context context, long id) {
    Uri smsDeleteUri = ContentUris.withAppendedId(SMS_QUERY_URI, id);
    int rows = context.getContentResolver().delete(smsDeleteUri, null, null);
  }

  /**
   * unregister to listen for incoming SMS
   */
  public static void unregisterIncomingSMSListener(ContentResolver contentResolver, ContentObserver contentObserver) {
    if (contentObserver != null) {
      contentResolver.unregisterContentObserver(contentObserver);
      contentObserver = null;
    }
  }
}
