package com.bomzaiya.ui;


import com.bomzaiya.app.idang.IdangActivity;

import android.os.Bundle;

public class PrelaunchActivity extends IdangFragmentActivity {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    onCreateLayout(LayoutDefinition.LAYOUT_TYPE_IDANG_PRELOADING);
    // setContentView(R.layout.idang_page_fullscreen);
    
    IdangActivity.start(getBaseContext());
    finish();
  }

}
