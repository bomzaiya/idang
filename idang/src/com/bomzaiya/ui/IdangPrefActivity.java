package com.bomzaiya.ui;

import android.os.Bundle;
import android.view.WindowManager;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.bomzaiya.app.idang.ProductConfig;
import com.bomzaiya.system.SystemHelper;

/**
 * This is just used in order to be able to recognize home button presses
 * 
 * @author Apisarn
 * 
 */
public class IdangPrefActivity extends SherlockPreferenceActivity {

  protected ProductConfig mProductConfig;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    setTheme(com.actionbarsherlock.R.style.Theme_Sherlock);  
    
    super.onCreate(savedInstanceState);

    IdangApplication app = (IdangApplication) getApplication();
    mProductConfig = app.getProductConfig();

    if (mProductConfig == null) {
      mProductConfig = new ProductConfig(getBaseContext());
      app.setProductConfig(mProductConfig);
    }

    configurePreference();

  }

  @SuppressWarnings("deprecation")
  private void configurePreference() {
    getPreferenceManager().setSharedPreferencesName(SystemHelper.PREFERENCES);
    getPreferenceManager().setSharedPreferencesMode(SystemHelper.PREFERENCE_MODE);
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
}
