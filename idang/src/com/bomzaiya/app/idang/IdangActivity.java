package com.bomzaiya.app.idang;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.bomzaiya.ui.IdangFragmentActivity;
import com.bomzaiya.ui.LayoutDefinition;

public class IdangActivity extends IdangFragmentActivity {

  public static void start(Context ctx) {
    Intent intent = new Intent(ctx, IdangActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    ctx.startActivity(intent);
  }

  private ViewPager mPager;

  public static Intent mSocketService = null;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    onCreateLayout(LayoutDefinition.LAYOUT_TYPE_IDANG_HOME);
    createHomePager();
    Log.d("start_idang", "asdfsafsdf");

    mApplication.startIdangService();
  }

  @Override
  public void onStop() {
    super.onStop();
    mApplication.unbindIdangService();
  }

  class MyFragmentAdapter extends FragmentPagerAdapter implements ViewPager.OnPageChangeListener, ActionBar.TabListener {

    private final List<String> mTabs = new ArrayList<String>();

    private FragmentActivity mContext;

    private ActionBar mActionBar;

    private ViewPager mViewPager;

    public MyFragmentAdapter(FragmentActivity activity,

    ActionBar actionBar, ViewPager pager) {
      super(activity.getSupportFragmentManager());
      mContext = activity;
      mActionBar = actionBar;
      mViewPager = pager;
      mViewPager.setAdapter(this);
      mViewPager.setOnPageChangeListener(this);
    }

    public void addTab(ActionBar.Tab tab, Class<?> clss) {
      mTabs.add(clss.getName());
      mActionBar.addTab(tab.setTabListener(this));
      notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int position) {
      return Fragment.instantiate(mContext, mTabs.get(position), null);
    }

    @Override
    public int getCount() {
      return 1;
    }

    @Override
    public CharSequence getPageTitle(int position) {
      String pageTitle = "";
      switch (position) {
      case 0:
        pageTitle = getString(R.string.app_name);
        break;
      }
      return pageTitle;
    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
      if (mViewPager.getCurrentItem() != tab.getPosition()) {
        mViewPager.setCurrentItem(tab.getPosition(), true);
      }
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {

    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
      // TODO Auto-generated method stub

    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
      // TODO Auto-generated method stub

    }

    @Override
    public void onPageSelected(int arg0) {
      // TODO Auto-generated method stub
      Tab tab = mActionBar.getTabAt(arg0);
      mActionBar.selectTab(tab);

    }
  }

  private void createHomePager() {
    ActionBar ab = getCurrentActionBar();

    final Tab homeTab = ab.newTab().setIcon(R.drawable.idang_launcher);

    mPager = (ViewPager) findViewById(R.id.mainPager);
    MyFragmentAdapter mTabsAdapter = new MyFragmentAdapter(this, getSupportActionBar(), mPager);

    mTabsAdapter.addTab(homeTab, HomeFragment.class);
  }

}
