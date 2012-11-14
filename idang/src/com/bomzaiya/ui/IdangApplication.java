package com.bomzaiya.ui;

import com.bomzaiya.app.idang.ProductConfig;

import android.app.Application;

public class IdangApplication extends Application {
  private ProductConfig mProductConfig = null;

  public void setProductConfig(ProductConfig productConfig) {
    mProductConfig = productConfig;
  }

  public ProductConfig getProductConfig() {
    return mProductConfig;
  }
}
