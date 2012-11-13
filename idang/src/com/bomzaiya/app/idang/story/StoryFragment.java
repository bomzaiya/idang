package com.bomzaiya.app.idang.story;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bomzaiya.app.idang.IdangFragment;
import com.bomzaiya.app.idang.IdangFragmentActivity;
import com.bomzaiya.app.idang.ProductConfig;
import com.bomzaiya.app.idang.R;
import com.bomzaiya.system.SystemHelper;

public class StoryFragment extends IdangFragment {
  protected static final String HOME_ACTIVITY_LOG_BUTTON = "HOME_ACTIVITY_LOG_BUTTON";

  public static final String BUNDLE_STORY_ID = "BUNDLE_STORY_ID";

  private IdangFragmentActivity mActivity;

  public static StoryFragment newInstance(String content) {
    StoryFragment fragment = new StoryFragment();

    return fragment;
  }

  private View mView;

  private int mStoryId = 0;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mActivity = (IdangFragmentActivity) getActivity();

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    Bundle bundle = getArguments();
    mStoryId = bundle.getInt(BUNDLE_STORY_ID);

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
    // final IdangFragmentActivity activity = mActivity;

    View view = null;
    try {
      view = inflater.inflate(R.layout.story, null);

      if (view != null) {
        Drawable storyTop = SystemHelper.readAssetDrawable(mActivity.getBaseContext(),
            "sample/story/story_sample_top.jpg", ProductConfig.IMAGE_LOCATION_STORY_TOP);
        ImageView ivTop = (ImageView) view.findViewById(R.id.ivTop);

        ivTop.setImageDrawable(storyTop);

        Drawable position1 = SystemHelper.readAssetDrawable(mActivity.getBaseContext(),
            "sample/story/story_sample_1.jpg", ProductConfig.IMAGE_LOCATION_STORY_TOP);
        ImageView ivPosition1 = (ImageView) view.findViewById(R.id.ivPosition1);

        ivPosition1.setImageDrawable(position1);

        Drawable position2 = SystemHelper.readAssetDrawable(mActivity.getBaseContext(),
            "sample/story/story_sample_2.jpg", ProductConfig.IMAGE_LOCATION_STORY_TOP);
        ImageView ivPosition2 = (ImageView) view.findViewById(R.id.ivPosition2);

        ivPosition2.setImageDrawable(position2);

        Drawable position3 = SystemHelper.readAssetDrawable(mActivity.getBaseContext(),
            "sample/story/story_sample_3.jpg", ProductConfig.IMAGE_LOCATION_STORY_TOP);
        ImageView ivPosition3 = (ImageView) view.findViewById(R.id.ivPosition3);

        ivPosition3.setImageDrawable(position3);

      }
    } catch (InflateException e) {

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
