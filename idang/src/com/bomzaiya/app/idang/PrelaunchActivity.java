package com.bomzaiya.app.idang;


import com.bomzaiya.ui.IdangFragmentActivity;
import com.bomzaiya.ui.LayoutDefinition;

import android.os.Bundle;

public class PrelaunchActivity extends IdangFragmentActivity {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    onCreateLayout(LayoutDefinition.LAYOUT_TYPE_IDANG_PRELOADING);
    
    IdangActivity.start(getBaseContext());
    finish();
  }

}
