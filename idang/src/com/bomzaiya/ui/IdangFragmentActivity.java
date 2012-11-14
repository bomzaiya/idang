package com.bomzaiya.ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.R.color;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.bomzaiya.app.idang.IdangActivity;
import com.bomzaiya.app.idang.ProductConfig;
import com.bomzaiya.app.idang.R;
import com.bomzaiya.internet.InternetHelper;
import com.bomzaiya.internet.OnSocketServiceListener;
import com.bomzaiya.internet.OnWifiListener;
import com.bomzaiya.internet.SocketHelper;
import com.bomzaiya.service.SocketService;
import com.bomzaiya.system.SystemHelper;

public abstract class IdangFragmentActivity extends SherlockFragmentActivity implements OnSocketServiceListener,
    OnWifiListener {

  private static final String RESTART_APPLICATION_TAG = "RESTART_APPLICATION_TAG";

  private static final int COMMAND_PRIVACY_LIGHT = 1;

  // private QuickLauncher mQuickLauncher;
  private TextView mTvWifiSignal;
  // private DigitalClock mDcClock;
  // private TextView mTvPhoneSignal;

  private TextView mTvBatteryLevel;
  private int mLayoutType;
  private TextView mTvSocketStatus;
  private ActionBar mActionBar;
  private TextView mTvTitle;

  // listeners
  private BroadcastReceiver mWifiListener;
  private BroadcastReceiver mConnectivityListener;
  private BroadcastReceiver mBatteryListener;
  private BroadcastReceiver mSocketListener;
  private ProgressBar mPbConnecting;
  private boolean mActivityStart;

  private String MENU_CONFIGURE_TAG = "MENU_CONFIGURE_TAG";

  private boolean mPreloadingMode = false;
  private BroadcastReceiver mIDANGServiceListener;

  private boolean mClearTitle = false;

  private MenuItem mMenuCheckin;

  private TextView mTvPrivacy;

  private boolean mCheckIn;

  protected ArrayList<TabView> mTabViewList;

  protected ProductConfig mProductConfig;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // change to user language
    String locale = SystemHelper.getSharePreferenceValue(getBaseContext(), ProductConfig.PREF_USER_LANGUAGE);
    SystemHelper.changeLocale(getBaseContext(), locale);

    IdangApplication app = (IdangApplication) getApplication();
    mProductConfig = app.getProductConfig();

    if (mProductConfig == null) {
      mProductConfig = new ProductConfig(getBaseContext());
      app.setProductConfig(mProductConfig);
    }
  }

  public ActionBar getCurrentActionBar() {
    return mActionBar;
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (resultCode == Activity.RESULT_CANCELED) {
      super.onActivityResult(requestCode, resultCode, data);
    } else {
      try {
        String siteName = getSharePreferenceValue(ProductConfig.PREF_CURRENT_SITE);
        String className = getClass().getName();
        String backActivityName = data.getStringExtra("back_activity");
        boolean restart = data.getBooleanExtra("restart", false);
        if (restart) {
          if (className.equals(backActivityName)) {
            // Intent intent = new Intent();
            // intent.putExtra(IDANGService.SERVICE_EXTRA_REQUEST,
            // IDANGService.STOP_PROCESS);
            // intent.setAction(ProductConfig.INTENT_ACTION_IDANG_SERVICE);
            // sendBroadcast(intent);

            // LaunchActivity.restart(getBaseContext());

            // Intent launchIntent = new Intent(getBaseContext(),
            // LaunchActivity.class);
            // launchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            // launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            // startActivity(launchIntent);

            onStop();
            finish();
          } else {
            // Intent intent = getIntent();
            // intent.putExtra("back_activity", IdangActivity.class.getName());
            // intent.putExtra("restart", true);
            // setResult(Activity.RESULT_OK, intent);

            onStop();
            finish();
          }
        } else {
          if (!className.equals(backActivityName)) {
            onStop();
            finish();
          }
        }

      } catch (NullPointerException e) {
        finish();
      }
    }

  }

  public int checkUserPermission(String password) {
    if (password.equals(getSharePreferenceValue(ProductConfig.PREF_ADMIN_PASSWORD))) {
      ProductConfig.CURRENT_MODE = ProductConfig.MODE_ADMIN;
    } else if (password.equals(getSharePreferenceValue(ProductConfig.PREF_SUPPORT_PASSWORD))) {
      ProductConfig.CURRENT_MODE = ProductConfig.MODE_SUPPORT;
    } else if (password.equals(getSharePreferenceValue(ProductConfig.PREF_SECURE_PASSWORD))) {
      ProductConfig.CURRENT_MODE = ProductConfig.MODE_SECURE;
    } else if (password.equals(getSharePreferenceValue(ProductConfig.PREF_USER_PASSWORD))) {
      ProductConfig.CURRENT_MODE = ProductConfig.MODE_USER;
    } else {
      ProductConfig.CURRENT_MODE = ProductConfig.MODE_ANONYMOUS;
    }
    return ProductConfig.CURRENT_MODE;
  }

  /**
   * back to specified activity
   * 
   * @param cls
   */
  public void backToActivity(Class<?> cls) {
    String className = getClass().getName();
    String backActivityName = cls.getName();
    if (!className.equals(backActivityName)) {
      Intent intent = getIntent();
      intent.putExtra("back_activity", cls.getName());
      intent.putExtra("restart", false);
      setResult(Activity.RESULT_OK, intent);
      finish();
    }
  }

  public void backToMainAndRestart() {
    String className = getClass().getName();
    if (className.equals(IdangActivity.class.getName())) {
      // Intent intentProcess = new Intent();
      // intentProcess.putExtra(IDANGService.SERVICE_EXTRA_REQUEST,
      // IDANGService.STOP_PROCESS);
      // intentProcess.setAction(ProductConfig.INTENT_ACTION_IDANG_SERVICE);
      // sendBroadcast(intentProcess);
      //
      // LaunchActivity.restart(getBaseContext());

      finish();
    } else {
      Intent intent = getIntent();
      intent.putExtra("back_activity", IdangActivity.class.getName());
      intent.putExtra("restart", true);
      setResult(Activity.RESULT_OK, intent);
      finish();
    }
  }

  private MyHandler mUIHandler = new MyHandler(this);

  private static final int HANDLER_CONFIGURE_FOOTER = 0;

  private static class MyHandler extends Handler {
    private WeakReference<IdangFragmentActivity> mActivity = null;

    public MyHandler(IdangFragmentActivity activity) {
      mActivity = new WeakReference<IdangFragmentActivity>(activity);
    }

    @Override
    public void handleMessage(Message msg) {
      IdangFragmentActivity activity = mActivity.get();
      if (activity != null) {

        switch (msg.what) {
        case HANDLER_CONFIGURE_FOOTER:

          break;

        }
      }
    }
  }

  public String getCurrentSiteDb() {
    String currentSite = getSharePreferenceValue(ProductConfig.PREF_CURRENT_SITE);
    // if (currentSite.equals("") || currentSite.indexOf(".sqlite") == -1) {
    // // mProductConfig.setCurrentSite(currentSite);
    // return ProductConfig.IDANG_DATABASE;
    // } else {
    // return currentSite;
    // }
    return currentSite;
  }

  public String getCurrentSiteName() {
    String siteName = getCurrentSiteDb().replace(".sqlite", "");
    return siteName;
  }

  public String getSiteFilePath(String filename) {
    String path = ProductConfig.IDANG_FOLDER + "/" + getCurrentSiteName() + "/" + filename;
    return path;
  }

  public void configureUserInfo() {
    String roomNumber = getSharePreferenceValue(ProductConfig.PREF_USER_ROOM_NUMBER);
    String roomType = getSharePreferenceValue(ProductConfig.PREF_USER_ROOM_TYPE);
    String phoneStatus = getSharePreferenceValue(ProductConfig.PREF_USER_PHONE_STATUS);
    // ProductConfig.CURRENT_USERINFO = new UserInfo();
    // ProductConfig.CURRENT_USERINFO.setRoomNumber(roomNumber);
    // ProductConfig.CURRENT_USERINFO.setRoomType(roomType);
    // ProductConfig.CURRENT_USERINFO.setPhoneStatus(phoneStatus);
  }

  @Override
  protected void onPause() {
    Log.d("IDANG_event", "pause");
    try {
      super.onPause();
      if (mIDANGServiceListener != null) {
        unregisterReceiver(mIDANGServiceListener);
        mIDANGServiceListener = null;
      }
    } catch (Exception e) {
    }
  }

  @Override
  public void onSocketDataRequest(JSONObject data) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onSocketDataResponse(JSONObject data) {
    // TODO Auto-generated method stub

  }

  // @Override
  // protected void onResumeFragments() {
  // super.onResumeFragments();
  // // Log.d("IDANG_event", "resume fragments");
  // if (!mPreloadingMode) {
  // }
  // }

  @Override
  protected void onStop() {
    try {
      super.onStop();
      if (!mPreloadingMode) {
        unregisterAllReceivers();
      }
    } catch (NullPointerException e) {
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    unbindDrawables(findViewById(R.id.mainLayout));
    System.gc();
  }

  private void unbindDrawables(View view) {
    if (view.getBackground() != null) {
      view.getBackground().setCallback(null);
    }
    if (view instanceof ViewGroup) {
      for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
        unbindDrawables(((ViewGroup) view).getChildAt(i));
      }
      ((ViewGroup) view).removeAllViews();
    }

  }

  private void unregisterAllReceivers() {
    InternetHelper.unregisterListener(getBaseContext(), mConnectivityListener);
    InternetHelper.unregisterListener(getBaseContext(), mWifiListener);

    SystemHelper.unregisterListener(getBaseContext(), mBatteryListener);
    SocketHelper.unregisterListener(getBaseContext(), mSocketListener);

    try {
      if (mIDANGServiceListener != null) {
        unregisterReceiver(mIDANGServiceListener);
        mIDANGServiceListener = null;
      }
    } catch (Exception e) {
    }
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
  }

  @Override
  protected void onStart() {
    super.onStart();
    Log.d("IDANG_event", "start");
    mActivityStart = true;

    if (!mPreloadingMode) {
      unregisterAllReceivers();

      // register wifi
      registerWifi();

      // register socket
      registerSocket();

      // cannot put registerIDANGServiceReceiver here
      // will cause illegalstateexception

    }

  }

  @Override
  protected void onResume() {
    super.onResume();
    Log.d("IDANG_event", "resume");
    if (!mActivityStart) {
      if (!mPreloadingMode) {
        unregisterAllReceivers();

        // register wifi
        registerWifi();

        // register socket
        registerSocket();

      }
    }

    // register IDANG service
    registerIDANGServiceReceiver();

    mActivityStart = false;
  }

  private void registerIDANGServiceReceiver() {
    this.runOnUiThread(new Runnable() {

      @Override
      public void run() {
        // mIDANGServiceListener = new BroadcastReceiver() {
        //
        // @Override
        // public void onReceive(Context context, Intent intent) {
        // int serviceRequest =
        // intent.getIntExtra(IDANGService.SERVICE_EXTRA_REQUEST, 0);
        // int serviceStatus =
        // intent.getIntExtra(IDANGService.SERVICE_EXTRA_STATUS, 0);
        //
        // switch (serviceRequest) {
        // case IDANGService.DOWNLOAD_SITEDATA:
        // if (serviceStatus == IDANGService.STATUS_SUCCESS) {
        // Bundle bundle = new Bundle();
        // bundle.putString(IDANGDialogFragment.BUNDLE_REFERENCE_TAG,
        // RESTART_APPLICATION_TAG);
        //
        // String moodDescription =
        // "The IDANG application must be restarted to take effect.";
        // bundle.putString(IDANGDialogFragment.BUNDLE_DESCRIPTION,
        // moodDescription);
        //
        // IDANGDialogFragment fd = (IDANGDialogFragment)
        // IDANGDialogFragment.instantiate(getBaseContext(),
        // IDANGDialogFragment.class.getName(), bundle);
        // fd.show(getSupportFragmentManager(), "mydialog");
        // }
        // break;
        //
        // case IDANGService.CHECK_IN:
        // case IDANGService.CHECK_OUT:
        // backToActivity(IdangActivity.class);
        // break;
        // }
        // }
        // };
        //
        // registerReceiver(mIDANGServiceListener, new
        // IntentFilter(ProductConfig.INTENT_ACTION_IDANG_SERVICE));
      }
    });

  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // if (mPreloadingMode) {
    // return false;
    // }
    // MenuInflater inflater = getSupportMenuInflater();
    // inflater.inflate(R.menu.v1_menu, menu);
    //
    // mMenuCheckin = menu.findItem(R.id.menuCheckIn);
    // mMenuCheckin.setVisible(false);

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle item selection
    Bundle bundle = new Bundle();
    String description = "";
    IdangDialogPasswordFragment fd = null;
    // switch (item.getItemId()) {
    // case R.id.menuCheckIn:
    // GuestActivity.start(this, GuestActivity.OPTION_LIST_FRAGMENT);
    // return true;
    //
    // case R.id.menuRestart:
    // bundle.putString(IDANGDialogFragment.BUNDLE_REFERENCE_TAG,
    // RESTART_APPLICATION_TAG);
    //
    // description = getString(R.string.restart_application);
    // bundle.putString(IDANGDialogFragment.BUNDLE_DESCRIPTION, description);
    //
    // IDANGDialogFragment fdf = (IDANGDialogFragment)
    // IDANGDialogFragment.instantiate(getBaseContext(),
    // IDANGDialogFragment.class.getName(), bundle);
    // fdf.show(getSupportFragmentManager(), "mydialog");
    // return true;
    //
    // case R.id.menuConfigure:
    // bundle.putString(IdangDialogPasswordFragment.BUNDLE_REFERENCE_TAG,
    // MENU_CONFIGURE_TAG);
    //
    // description = getString(R.string.dialog_access_settings);
    // bundle.putString(IdangDialogPasswordFragment.BUNDLE_DESCRIPTION,
    // description);
    //
    // fd = (IdangDialogPasswordFragment)
    // IdangDialogPasswordFragment.instantiate(getBaseContext(),
    // IdangDialogPasswordFragment.class.getName(), bundle);
    // fd.show(getSupportFragmentManager(), "mydialog");
    //
    // return true;
    //
    // default:
    // return super.onOptionsItemSelected(item);
    // }
    return super.onOptionsItemSelected(item);
  }

  public void setHeaderTitle(final String title) {
    this.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        try {
          mTvTitle.setText(title);
        } catch (NullPointerException e) {
          // e.printStackTrace();
        }
      }
    });
  }

  protected void requestStatus() {
    Intent intent = new Intent();
    intent.putExtra(SocketService.SOCKET_EXTRA_STATUS, SocketService.SOCKET_REQUEST_STATUS);
    intent.setAction(ProductConfig.INTENT_ACTION_IDANG_SOCKET_STATUS);
    sendBroadcast(intent);
  }

  private void registerSocket() {
    mSocketListener = SocketHelper.registerSocketListener(getBaseContext(), this);
    requestStatus();
  }

  private void configurePermission() {
    String password = getSharePreferenceValue(ProductConfig.PREF_USER_PASSWORD);
    if (password.equals("")) {
      password = ProductConfig.DEFAULT_USER_PASSWORD;
      setSharePreferenceValue(ProductConfig.PREF_USER_PASSWORD, password);
    }
    password = getSharePreferenceValue(ProductConfig.PREF_SECURE_PASSWORD);
    if (password.equals("")) {
      password = ProductConfig.DEFAULT_SECURE_PASSWORD;
      setSharePreferenceValue(ProductConfig.PREF_SECURE_PASSWORD, password);
    }
    password = getSharePreferenceValue(ProductConfig.PREF_SUPPORT_PASSWORD);
    if (password.equals("")) {
      password = ProductConfig.DEFAULT_SUPPORT_PASSWORD;
      setSharePreferenceValue(ProductConfig.PREF_SUPPORT_PASSWORD, password);
    }
    password = getSharePreferenceValue(ProductConfig.PREF_ADMIN_PASSWORD);
    if (password.equals("")) {
      password = ProductConfig.DEFAULT_ADMIN_PASSWORD;
      setSharePreferenceValue(ProductConfig.PREF_ADMIN_PASSWORD, password);
    }

  }

  public void requestReconnectSocket() {
    Intent intent = new Intent();
    intent.putExtra(SocketService.SOCKET_EXTRA_STATUS, SocketService.SOCKET_REQUEST_NEW_CONNECTION);
    intent.setAction(ProductConfig.INTENT_ACTION_IDANG_SOCKET_STATUS);
    sendBroadcast(intent);
  }

  /**
   * change layout type for activity
   * 
   * @param layoutType
   */
  public void onCreateLayout(int layoutType) {
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

    mLayoutType = layoutType;

    setContentViewType(layoutType);

    mActionBar = getSupportActionBar();
    // mActionBar.setCustomView(R.layout.v1_header);
    // configure title
    // mTvTitle = (TextView)
    // mActionBar.getCustomView().findViewById(R.id.tvTitle);

    if (layoutType == LayoutDefinition.LAYOUT_TYPE_IDANG_HOME
        || layoutType == LayoutDefinition.LAYOUT_TYPE_IDANG_PRELOADING) {
      mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
    } else if (layoutType == LayoutDefinition.LAYOUT_TYPE_IDANG_PAGE_SLIDE_ICON) {
      mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
    }
    //
    if (layoutType == LayoutDefinition.LAYOUT_TYPE_IDANG_PRELOADING) {
      mPreloadingMode = true;
    }

    mActionBar.setDisplayShowHomeEnabled(true);
    mActionBar.setDisplayShowTitleEnabled(true);
    mActionBar.setDisplayShowCustomEnabled(true);
    mActionBar.setDisplayUseLogoEnabled(false);

    ProductConfig.APPLICATION_DATABASE = getCurrentSiteDb();

    if (getCurrentSiteName().equals("IDANG")) {
      ProductConfig.DEFAULT_SITE = true;
    } else {
      ProductConfig.DEFAULT_SITE = false;
    }

    // configure user info
    configureUserInfo();

    // configure permission
    configurePermission();

    // if (!mPreloadingMode) {
    // // configure header;
    // configureHeader();
    // }

    // show the latest status from the phone
    String room = getSharePreferenceValue(ProductConfig.PREF_USER_ROOM_NUMBER);
    String phone_status = getSharePreferenceValue(ProductConfig.PREF_USER_PHONE_STATUS);

    if (room.equals(ProductConfig.ROOM_NONE) || room == null || room.equals(ProductConfig.ROOM_CHECKOUT)) {
      String phoneStatus = getString(R.string.guest_checkout);
      if (phone_status.equals(ProductConfig.PHONE_STATUS_UNREGISTER)) {
        phoneStatus = getString(R.string.guest_unregister);
      }
      setHeaderTitle(phoneStatus);
    } else {
      setHeaderTitle("");
    }
  }

  /**
   * set layout for all inherited activity
   * 
   * @param layoutType
   */
  private void setContentViewType(int layoutType) {
    String IDANG_layout_version = SystemHelper.getSharePreferenceValue(getBaseContext(), "IDANG_layout_version");
    if (IDANG_layout_version == "" || IDANG_layout_version == null) {
      ProductConfig.LAYOUT_VERSION = 1;
    } else {
      ProductConfig.LAYOUT_VERSION = Integer.parseInt(IDANG_layout_version);
    }

    int resId = 0;
    try {
      setTheme(com.actionbarsherlock.R.style.Theme_Sherlock);

      if (layoutType == LayoutDefinition.LAYOUT_TYPE_IDANG_PRELOADING) {
        resId = R.layout.idang_preloading;
      } else if (layoutType == LayoutDefinition.LAYOUT_TYPE_IDANG_HOME) {
        resId = R.layout.idang_home;
      } else if (layoutType == LayoutDefinition.LAYOUT_TYPE_IDANG_PAGE_FULLSCREEN) {
        resId = R.layout.idang_page_fullscreen;
      } else if (layoutType == LayoutDefinition.LAYOUT_TYPE_IDANG_PAGE_SLIDE) {
        resId = R.layout.idang_page_slide;
      } else if (layoutType == LayoutDefinition.LAYOUT_TYPE_IDANG_PAGE_SLIDE_ICON) {
        resId = R.layout.idang_page_slide_icon;
      }
      super.setContentView(resId);
    } catch (Resources.NotFoundException e) {
      // TODO Auto-generated catch block
      // e.printStackTrace();
    }
  }

  /**
   * configure wifi
   */
  private void registerWifi() {
    mConnectivityListener = InternetHelper.registerConnectivityListener(getBaseContext(), this);
    mWifiListener = InternetHelper.registerWifiSignalLevelListener(getBaseContext(), this);
  }

  /**
   * get shared preference value
   * 
   * @param prefId
   * @return
   */
  public String getSharePreferenceValue(String prefId) {
    return SystemHelper.getSharePreferenceValue(getBaseContext(), prefId);
  }

  /**
   * set shared preference value
   * 
   * @param prefId
   * @param prefValue
   */
  public void setSharePreferenceValue(String prefId, String prefValue) {
    SystemHelper.setSharePreferenceValue(getBaseContext(), prefId, prefValue);
  }

  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event) {
    switch (keyCode) {
    case KeyEvent.KEYCODE_MENU:
      break;
    case KeyEvent.KEYCODE_BACK:
      return super.onKeyUp(keyCode, event);
    }
    return super.onKeyUp(keyCode, event);
  }

  @Override
  public void onWifiLevelChanged(int rssi) {
    // try {
    // int resId = 0;
    // switch (rssi) {
    // case 0:
    // resId = R.drawable.signal_0;
    // break;
    //
    // case 1:
    // resId = R.drawable.signal_1;
    // break;
    //
    // case 2:
    // resId = R.drawable.signal_2;
    // break;
    //
    // case 3:
    // resId = R.drawable.signal_3;
    // break;
    //
    // case 4:
    // resId = R.drawable.signal_4;
    // break;
    //
    // case 5:
    // resId = R.drawable.signal_5;
    // break;
    //
    // default:
    // resId = R.drawable.signal_0;
    // break;
    // }
    //
    // mTvWifiSignal.setCompoundDrawablesWithIntrinsicBounds(0, resId, 0, 0);
    // } catch (NullPointerException e) {
    //
    // }
  }

  @Override
  public void onWifiStatusChanged(NetworkInfo networkInfo) {
    // try {
    // if (networkInfo == null) {
    // mTvWifiSignal.setVisibility(TextView.GONE);
    // mTvPrivacy.setVisibility(TextView.GONE);
    // } else {
    // if (networkInfo.getType() == -1) {
    // mTvWifiSignal.setVisibility(TextView.GONE);
    // mTvPrivacy.setVisibility(TextView.GONE);
    // } else {
    // if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
    // mTvWifiSignal.setCompoundDrawablesWithIntrinsicBounds(0,
    // R.drawable.signal_5, 0, 0);
    // mTvWifiSignal.setText(InternetHelper.lookupNetworkSubTypeAbbreviate(networkInfo));
    // } else {
    // mTvWifiSignal.setText(networkInfo.getTypeName());
    // }
    // mTvWifiSignal.setVisibility(TextView.VISIBLE);
    // mTvPrivacy.setVisibility(TextView.VISIBLE);
    //
    // // when wifi is on, broadcast to IDANG service, please check
    // // epg.
    // Intent intent = new Intent();
    // intent.putExtra(IDANGService.SERVICE_EXTRA_REQUEST,
    // IDANGService.GET_EPG);
    // intent.putExtra(IDANGService.SERVICE_EXTRA_STATUS,
    // IDANGService.STATUS_CHECK);
    // intent.setAction(ProductConfig.INTENT_ACTION_IDANG_SERVICE);
    // sendBroadcast(intent);
    // }
    // }
    // } catch (NullPointerException e) {
    //
    // }
  }

  private void changeSocketStatus(int status) {

    String statusText = "";
    int color = 0;
    int progressBarVisibility = 0;
    switch (status) {
    case SocketService.SOCKET_CONNECTED:
      progressBarVisibility = ProgressBar.GONE;
      statusText = getString(R.string.socket_status_online);
      color = Color.parseColor("#608AFF");
      break;

    case SocketService.SOCKET_CONNECTING:
      progressBarVisibility = ProgressBar.VISIBLE;
      statusText = getString(R.string.socket_status_offline);
      color = Color.parseColor("#CCCCCC");
      break;

    case SocketService.SOCKET_FRCD_DOWN:
      progressBarVisibility = ProgressBar.GONE;

      statusText = getString(R.string.socket_status_online);
      color = Color.parseColor("#608AFF");

      String phoneStatus = SocketService.SOCKET_FRCD_STATUS_DOWN;
      setHeaderTitle(phoneStatus);
      break;

    case SocketService.SOCKET_DISCONNECT:
      progressBarVisibility = ProgressBar.GONE;
      statusText = getString(R.string.socket_status_offline);
      color = Color.parseColor("#CCCCCC");
      break;

    default:
      progressBarVisibility = ProgressBar.GONE;
      statusText = getString(R.string.socket_status_offline);
      color = Color.parseColor("#CCCCCC");
      break;
    }

    final int visibility = progressBarVisibility;
    final int textColor = color;
    final String statusTextString = statusText;

    this.runOnUiThread(new Runnable() {
      @Override
      public void run() {

        mPbConnecting.setVisibility(visibility);
        mTvSocketStatus.setTextColor(textColor);
        mTvSocketStatus.setText(statusTextString);
      }
    });

  }

  @Override
  public void onSocketRequestConnection() {
    // TODO Auto-generated method stub

  }

  @Override
  public View onCreateView(String name, Context context, AttributeSet attrs) {
    return super.onCreateView(name, context, attrs);
  }

  @Override
  public void onSocketConnected(JSONObject data) {
    saveInfo(data);
    requestStatus();
  }

  private void saveInfo(JSONObject data) {
    try {
      String frcd_status = "";// data.getString(SocketService.SOCKET_SERVER_FRCD_STATUS);
      String room = data.getString(SocketService.SOCKET_SERVER_ROOM_NUMBER);
      String room_type = data.getString(SocketService.SOCKET_SERVER_ROOM_TYPE);
      String phone_status = data.getString(SocketService.SOCKET_SERVER_PHONE_STATUS);

      // save customer info in shared preference
      if (!room.equals(ProductConfig.ROOM_NONE) && room != null) {
        setSharePreferenceValue(ProductConfig.PREF_USER_ROOM_NUMBER, room);
        setSharePreferenceValue(ProductConfig.PREF_USER_ROOM_TYPE, room_type);
        setSharePreferenceValue(ProductConfig.PREF_USER_PHONE_STATUS, phone_status);

        if (room.equals(ProductConfig.ROOM_CHECKOUT)) {
          mCheckIn = false;

          String phoneStatus = getString(R.string.guest_checkout);
          if (phone_status.equals(ProductConfig.PHONE_STATUS_UNREGISTER)) {
            phoneStatus = getString(R.string.guest_unregister);
          }

          if (frcd_status.equals(SocketService.SOCKET_FRCD_STATUS_DOWN)) {
            phoneStatus += "+" + SocketService.SOCKET_FRCD_STATUS_DOWN;
          }

          setHeaderTitle(phoneStatus);

          mMenuCheckin.setVisible(true);
        } else {
          mCheckIn = true;
          if (mClearTitle) {
            setHeaderTitle("");
          }
          mMenuCheckin.setVisible(false);
        }
      } else {
        String phoneStatus = "";
        if (frcd_status.equals(SocketService.SOCKET_FRCD_STATUS_DOWN)) {
          phoneStatus += "+" + SocketService.SOCKET_FRCD_STATUS_DOWN;
        }
        setHeaderTitle(phoneStatus);
      }
    } catch (NullPointerException e) {
    } catch (JSONException e) {
    }
  }

  public void setClearTitle(boolean clear) {
    mClearTitle = clear;
  }

  @Override
  public void onSocketDisconnected(final String reason) {
  }

  @Override
  public void onSocketReconnecting() {
  }

  @Override
  public void onSocketUpdate(JSONObject data) {
    saveInfo(data);
  }

  @Override
  public void onSocketCommand(String command) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onSocketRequestStatus() {
    // TODO Auto-generated method stub

  }

  @Override
  public void onSocketFRCDDown() {
    // changeSocketStatus(SocketService.SOCKET_FRCD_DOWN);
  }

  public void onDialogConfirmed(String tag) {
    if (tag.equals(RESTART_APPLICATION_TAG)) {
      backToMainAndRestart();
    } else {
      // SystemHelper.appendLog("dialog_", "FFA confirmed" + tag);
      // TODO improve searching know the exact fragment
      // FragmentManager fm = getSupportFragmentManager();
      // try {
      // GeneralWidgetFragment fragment = (GeneralWidgetFragment)
      // fm.findFragmentByTag(tag);
      // fragment.onDialogConfirmed(tag);
      // } catch (NullPointerException e) {
      // // SystemHelper.appendLog("dialog_", "FFA error null");
      // } catch (ClassCastException e) {
      // // SystemHelper.appendLog("dialog_",
      // // "FFA error ClassCastException");
      // }
    }
  }

  public void onDialogConfirmed(String tag, String password) {
    checkUserPermission(password);
    if (tag.equals(MENU_CONFIGURE_TAG)) {
      switch (ProductConfig.CURRENT_MODE) {
      case ProductConfig.MODE_USER:
        // CustomerInformationPreferenceActivity.startForResult(this);
        break;

      case ProductConfig.MODE_SECURE:
        // SecureInformationPreferenceActivity.startForResult(this);
        break;

      case ProductConfig.MODE_SUPPORT:
      case ProductConfig.MODE_ADMIN:
        // AdminInformationPreferenceActivity.startForResult(this);
        break;
      default:

      }
    }
  }

  public void onDialogCancelled(String tag) {
    // SystemHelper.appendLog("dialog_", "FFA cancel");
  }

  public void onWidgetOpened() {
    // SystemHelper.appendLog("widget_", "FFA Widget show");
  }

}
