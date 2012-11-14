package com.bomzaiya.app.idang;

import java.util.HashMap;

import android.content.Context;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.SparseArray;

public class ProductConfig {
  public static int SITE_ID = 1;

  public static final String PRODUCT_CONFIG_NAME = "eclipse/trunk";
  public static final String APP_NAME = "pre-debug";
  public static final String LAUNCHER_NAME = "pre-debug";
  public static final String VERSION_NAME = "pre-debug";
  public static final int VERSION_CODE = 0;
  public static final String BUILD_TIMESTAMP = "1234-56-78";
  public static final String BUILD_USERNAME = "pre-debug";
  public static final String SYNCHONIZE_HOST = "test.IDANG.com";

  // layout version
  public static int LAYOUT_VERSION = 1;
  public static final int VERSION_GENERIC = 1;
  public static final int VERSION_SPECIAL = 2;

  // is sample, sample have no data wipe
  public static final boolean DOWNLOADABLE = true;

  // version
  public static int PRODUCT_VERSION = 1; // SiteConfig.PRODUCT_VERSION_TEST;

  public static final String SYNCHONIZE_PROTOCOL = "https://";
  public static final String SYNCHONIZE_FOLDER = "/sync";

  public static long SYNCHRONIZE_TIME_INTERVAL = 120000;// 3600000;

  public static final String SYNCHONIZE_USERNAME = "IDANG";
  public static final String SYNCHONIZE_PASSWORD = "IDANG";

  // service
  public static final String SERVICE_REF_URL_SUFFIX = "/block/service/parent-ref";
  public static final String SERVICE_DETAIL_URL_SUFFIX = "/block/tabs/detail-item-native/sParentId/";

  // epg
  public static final String EPG_URL_SUFFIX = "/webservice/tvGuide/get-current-epg-by-channels";

  // site
  public static final String SITE_DATA_URL_SUFFIX = "/webservice/site/get-site";

  // check in
  public static final String CHECKIN_LIST_URL_SUFFIX = "/block/booking/get-checkin-room-no-phone/sites_id/1";
  public static final String CHECKIN_URL_SUFFIX = "/admin/automation/check-in-room";

  // user activity log
  public static final String ACTIVITY_LOG_URL_SUFFIX = "/webservice/activity/save";

  // socket log
  public static final String SOCKET_LOG_URL_SUFFIX = "/webservice/socket/save";

  /**
   * min = 15 secs, less than 10 secs cause dis/con problem
   */
  public static final int SOCKET_START_DELAY = 4500;
  public static final int SOCKET_RECONNECT_INTERVAL = 15000;
  public static final int SOCKET_FORCE_STOP_DELAY = 3000;

  public static final int SOCKET_PINGPONG_DELAY = 20000;
  public static final int SOCKET_PINGPONG_FAIL_DELAY = 10000;

  public static int SOCKET_PINGPONG_INTERVAL = 15000;
  public static final int SOCKET_PINGPONG_INTERVAL_3G = 5000;
  public static final int SOCKET_PINGPONG_INTERVAL_WIFI = 15000;

  // SOCKET BROADCAST
  public static final String INTENT_ACTION_IDANG_SOCKET_STATUS = "com.bomzaiya.app.idang.intent.action.IDANG_SOCKET_STATUS";

  // REQUEST IDANG SERVER
  public static final String INTENT_ACTION_IDANG_SERVICE = "com.bomzaiya.app.idang.intent.action.IDANG_SERVICE";

  // ADMINCOMMAND BROADCAST
  public static final String INTENT_ACTION_IDANG_ADMINCOMMAND = "com.bomzaiya.app.idang.intent.action.IDANG_ADMINCOMMAND";

  // CONFIGRUE BROADCAST
  public static final String INTENT_ACTION_IDANG_CONFIGURE = "com.bomzaiya.app.idang.intent.action.IDANG_CONFIGURE";

  public static final String IDANG_FOLDER = Environment.getExternalStorageDirectory() + "/IDANG";
  public static final String IDANG_DOWNLOAD_FOLDER = IDANG_FOLDER + "/download";
  public static final String SITE_FOLDER_EPG = "/epg";
  public static final String SITE_FOLDER_APPLICATION = "/application";
  public static final String SITE_FOLDER_MOOD = "/mood";
  public static final String SITE_FOLDER_CHANNEL_GROUP = "/channel_group";
  public static final String SITE_FOLDER_SERVICES = "/services";

  // SQLite database
  public static final String IDANG_DATABASE = "IDANG.sqlite";
  public static String APPLICATION_DATABASE = "IDANG.sqlite";

  // using default site or dedicated site
  public static boolean DEFAULT_SITE = true;

  // secure
  public static final int MODE_ANONYMOUS = 0;
  public static final int MODE_USER = 1;
  public static final int MODE_SECURE = 2;
  public static final int MODE_SUPPORT = 3;
  public static final int MODE_ADMIN = 4;

  // mode
  public static int CURRENT_MODE = MODE_USER;

  public static final String PASSWORD_BY_PERMISSION = "PASSWORD_BY_PERMISSION";
  public static final String DEFAULT_USER_PASSWORD = "user";
  public static final String DEFAULT_SECURE_PASSWORD = "secure";
  public static final String DEFAULT_SUPPORT_PASSWORD = "support";
  public static final String DEFAULT_ADMIN_PASSWORD = "jcmr";

  // pref
  public static final String PREF_USER_PASSWORD = "user_password";
  public static final String PREF_SECURE_PASSWORD = "secure_password";
  public static final String PREF_SUPPORT_PASSWORD = "support_password";
  public static final String PREF_ADMIN_PASSWORD = "admin_password";
  public static final String PREF_CURRENT_SITE = "current_site";

  // pref user
  public static final String PREF_USER_ROOM_NUMBER = "room_number";
  public static final String PREF_USER_ROOM_TYPE = "room_type";
  public static final String PREF_USER_PHONE_STATUS = "phone_status";
  public static final String PREF_USER_LANGUAGE = "user_language";

  // pref epg
  public static final String PREF_EPG_SYNC_TIME = "epg_sync_time";

  // pref csip
  public static final String PREF_CSIP_JSON = "csip_json";

  // pref preference
  public static final String PREF_PREFERENCE_USERDEFINED = "preference_userdefined";

  // user info (taken from node server or prefs)
  // public static UserInfo CURRENT_USERINFO = new UserInfo();

  // check in room
  public static final String ROOM_NONE = "";
  public static final String ROOM_CHECKOUT = "9999";
  public static final String ROOM_TEST = "9998";
  public static final String PHONE_STATUS_UNREGISTER = "UNREGISTER";

  // room type
  public static final String ROOM_TYPE_1 = "1";
  public static final String ROOM_TYPE_2 = "2";
  public static final String ROOM_TYPE_3 = "3";
  public static final String ROOM_TYPE_4 = "4";

  // switch
  public static final String SWITCH_SELECT_LIVINGROOM = "livingroom";
  public static final String SWITCH_SELECT_BEDROOM = "bedroom";
  public static final String SWITCH_SELECT = "SWITCH_SELECT";
  public static final String SWITCH_LINK_ROOMCONTROL = "ROOMCONTROL";
  public static final String SWITCH_LINK_ENTERTAINMENT = "ENTERTAINMENT";
  private Context mContext = null;

  public static final int SITE_IDANG = 0;
  public static final int SITE_DEMO = 1;
  public static final int SITE_ALOFT = 2;
  public static final int SITE_GLOWFISH = 3;

  public static SparseArray<Integer> mPortList = new SparseArray<Integer>();

  static {
    mPortList.put(0, 40081);
    mPortList.put(1, 40082);
    mPortList.put(2, 40083);
    mPortList.put(3, 40084);
  };

  public static SparseArray<String> mHostList = new SparseArray<String>();

  static {
    mHostList.put(SITE_IDANG, "test.fingi.com");
  };

  public ProductConfig(Context context) {
    mContext = context;
    // trick to set db
    // String currentSite = getCurrentSiteDb(context);
    // setCurrentSite(currentSite);
  }

  // public void setCurrentSite(String currentSite) {
  // SystemHelper.setSharePreferenceValue(mContext,
  // ProductConfig.PREF_CURRENT_SITE, currentSite);
  // SITE_ID = SiteConfig.getSiteId(currentSite.replace(".sqlite", ""));
  // }
  //
  // public String getCurrentSiteDb(Context context) {
  // String currentSite = SystemHelper.getSharePreferenceValue(context,
  // ProductConfig.PREF_CURRENT_SITE);
  // if (currentSite.equals("") || currentSite.indexOf(".sqlite") == -1) {
  // currentSite = ProductConfig.IDANG_DATABASE;
  // setCurrentSite(currentSite);
  // return currentSite;
  // } else {
  // return currentSite;
  // }
  // }
  //
  // public static int getProductVersion() {
  // return SiteConfig.getProductVersion(SITE_ID);
  // }
  //
  // public static String getCurrentSiteName() {
  // return SiteConfig.getSiteName(SITE_ID);
  // }
  //
  // public static String getHost() {
  // return SiteConfig.getHost(SITE_ID);
  // }
  //
  public String getSocketHost() {
    return mHostList.get(0);
  }

  //
  public int getSocketPort() {
    return mPortList.get(0);
  }

  public static int IMAGE_LOCATION_CATEGORY_ITEM = 1000;
  public static int IMAGE_LOCATION_STORY_TOP = 2000;

  public static SparseArray<Integer> DENSITY_WIDTH_1F = new SparseArray<Integer>();

  static {
    DENSITY_WIDTH_1F.put(IMAGE_LOCATION_CATEGORY_ITEM, 350);
    DENSITY_WIDTH_1F.put(IMAGE_LOCATION_STORY_TOP, 750);
  };

  public static SparseArray<Integer> DENSITY_HEIGHT_1F = new SparseArray<Integer>();

  static {
    DENSITY_HEIGHT_1F.put(IMAGE_LOCATION_CATEGORY_ITEM, 170);
    DENSITY_HEIGHT_1F.put(IMAGE_LOCATION_STORY_TOP, 200);
  };

  //
  // public static String getApplicationUrl() {
  // return SYNCHONIZE_PROTOCOL + getHost() + "/" +
  // SiteConfig.getApplicationFolder(SITE_ID);
  // }
  //
  // public static String getCheckInListUrl() {
  // return SYNCHONIZE_PROTOCOL + getHost() + "/" +
  // SiteConfig.getApplicationFolder(SITE_ID) + CHECKIN_LIST_URL_SUFFIX;
  // }
  //
  // public static String getCheckInUrl() {
  // return SYNCHONIZE_PROTOCOL + getHost() + "/" +
  // SiteConfig.getApplicationFolder(SITE_ID) + CHECKIN_URL_SUFFIX;
  // }
  //
  // public static String getServiceReferenceUrl() {
  // return SYNCHONIZE_PROTOCOL + getHost() + "/" +
  // SiteConfig.getApplicationFolder(SITE_ID) + SERVICE_REF_URL_SUFFIX;
  // }
  //
  // public static String getServiceDetailUrl() {
  // return SYNCHONIZE_PROTOCOL + getHost() + "/" +
  // SiteConfig.getApplicationFolder(SITE_ID) + SERVICE_DETAIL_URL_SUFFIX;
  // }
  //
  // public static String getSynchronizeUrl() {
  // return SYNCHONIZE_PROTOCOL + getHost() + "/" +
  // SiteConfig.getApplicationFolder(SITE_ID) + SYNCHONIZE_FOLDER;
  // }
  //
  // public static String getEpgUrl() {
  // return SYNCHONIZE_PROTOCOL + getHost() + "/" +
  // SiteConfig.getApplicationFolder(SITE_ID) + EPG_URL_SUFFIX;
  // }
  //
  // public static String getSiteDataUrl() {
  // return SYNCHONIZE_PROTOCOL + getHost() + "/" +
  // SiteConfig.getApplicationFolder(SITE_ID) + SITE_DATA_URL_SUFFIX;
  // }
  //
  // public static String getActivityLogUrl() {
  // return SYNCHONIZE_PROTOCOL + getHost() + "/" +
  // SiteConfig.getApplicationFolder(SITE_ID) + ACTIVITY_LOG_URL_SUFFIX;
  // }
  //
  // public static String getSocketLogUrl() {
  // return SYNCHONIZE_PROTOCOL + getHost() + "/" +
  // SiteConfig.getApplicationFolder(SITE_ID) + SOCKET_LOG_URL_SUFFIX;
  // }

}
