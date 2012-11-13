package com.bomzaiya.app.idang;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.json.JSONObject;

import com.bomzaiya.app.idang.story.StoryActivity;
import com.bomzaiya.system.SystemHelper;

import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.CalendarContract.Attendees;
import android.util.AttributeSet;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class HomeFragment extends IdangFragment {
  protected static final String HOME_ACTIVITY_LOG_BUTTON = "HOME_ACTIVITY_LOG_BUTTON";

  private IdangFragmentActivity mActivity;

  public static HomeFragment newInstance(String content) {
    HomeFragment fragment = new HomeFragment();

    return fragment;
  }

  private View mView;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mActivity = (IdangFragmentActivity) getActivity();

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    mView = createView(inflater);
    return mView;
  }

  @Override
  public void onStart() {
    super.onStart();
  }

  @Override
  public void onResume() {
    super.onResume();

  }

  @SuppressLint("NewApi")
  private View createView(LayoutInflater inflater) {
    final IdangFragmentActivity activity = mActivity;

    View view = null;
    try {
      view = inflater.inflate(R.layout.home_category, null);
    } catch (InflateException e) {

    }

    if (view != null) {
      LinearLayout layoutCategoryList = (LinearLayout) view.findViewById(R.id.layoutCategoryList);
      layoutCategoryList.removeAllViews();
      for (int i = 0; i < 10; i++) {
        View item = inflater.inflate(R.layout.home_category_item, null);
        final int story_id = i;

        Drawable storyCategory = SystemHelper.readAssetDrawable(mActivity.getBaseContext(),
            "sample/category/category_sample_2.jpg", ProductConfig.IMAGE_LOCATION_CATEGORY_ITEM);
        try {
          ImageView ivCategory = (ImageView) item.findViewById(R.id.ivCategory);

          ivCategory.setImageDrawable(storyCategory);
        } catch (NullPointerException e) {
        }

        item.setOnClickListener(new OnClickListener() {

          @Override
          public void onClick(View v) {
            StoryActivity.startForResult(mActivity, story_id);
          }
        });

        layoutCategoryList.addView(item);
      }
    }
    return view;
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
  }

  @Override
  public void onSocketRequestConnection() {
    // TODO Auto-generated method stub

  }

  @Override
  public void onSocketConnected(JSONObject data) {
    // mLayoutReconnect.setVisibility(RelativeLayout.GONE);
  }

  @Override
  public void onSocketDisconnected(String reason) {
    // mButtonReconnect.setText(R.string.home_reconnect);
    // mButtonReconnect.setEnabled(true);
    // mLayoutReconnect.setVisibility(RelativeLayout.VISIBLE);
  }

  @Override
  public void onSocketReconnecting() {
    // mButtonReconnect.setText(R.string.home_connecting);
    // mButtonReconnect.setEnabled(false);
  }

  @Override
  public void onSocketUpdate(JSONObject data) {
    // TODO Auto-generated method stub

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

  }

  @Override
  public void onSocketDataRequest(JSONObject data) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onSocketDataResponse(JSONObject data) {
    // TODO Auto-generated method stub

  }

}
