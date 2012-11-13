package com.bomzaiya.app.idang;

import android.content.BroadcastReceiver;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragment;
import com.bomzaiya.internet.OnSocketServiceListener;

public abstract class IdangFragment extends SherlockFragment implements OnSocketServiceListener   {
  protected IdangFragmentActivity mActivity;
  OnFragmentWidgetListener mOnFragmentWidgetListener = null;
  private BroadcastReceiver mSocketListener;

  private boolean mActivityStart = false;
  protected boolean mHistory = false;
  
  public interface OnFragmentWidgetListener {
    public void onWidgetOpened();
  }

  public void onAttach(IdangFragmentActivity activity) {
    super.onAttach(activity);
    try {
      mOnFragmentWidgetListener = (OnFragmentWidgetListener) activity;
    } catch (ClassCastException e) {
      throw new ClassCastException(activity.toString() + " must implement OnFragmentWidgetListener");
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // SystemHelper.appendLog("socket_", "found IdangFragmentActivity");
    mActivity = (IdangFragmentActivity) getActivity();
  }

  @Override
  public void onStart() {
    super.onStart();
    mActivityStart = true;

    unregisterAllReceivers();

    registerListeners();
  }

  @Override
  public void onResume() {
    super.onResume();
    if (!mActivityStart) {
      unregisterAllReceivers();
      registerListeners();
    }
    mActivityStart = false;
  }

  @Override
  public void onStop() {
    super.onStop();
    unregisterAllReceivers();
  }

  protected void registerListeners() {
    // register listener
//    mSocketListener = SocketHelper.registerSocketListener(mActivity.getBaseContext(), this);
//    requestStatus();
  }

  protected void unregisterAllReceivers() {
//    SocketHelper.unregisterListener(mActivity.getBaseContext(), mSocketListener);
  }

 

  public void onWidgetOpened() {
    // SystemHelper.appendLog("widget_", "FF widget open");
  }
 

  public boolean onHistoryBack() {
    return false;
  }
}
