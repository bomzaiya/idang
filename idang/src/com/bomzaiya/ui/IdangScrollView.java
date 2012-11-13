package com.bomzaiya.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class IdangScrollView extends ScrollView {

  public interface OnScrollChangedListener {
    public abstract void onScrollChanged(int l, int t, int oldl, int oldt);
  }

  private OnScrollChangedListener mScrollListener = null;

  public IdangScrollView(Context context) {
    super(context);
  }

  public IdangScrollView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  public IdangScrollView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  public void onScrollChanged(int l, int t, int oldl, int oldt) {
    super.onScrollChanged(l, t, oldl, oldt);

    if (mScrollListener != null) {
      mScrollListener.onScrollChanged(l, t, oldl, oldt);
    }
  }

  public void setOnScrollChangedListener(OnScrollChangedListener scrollListener) {
    mScrollListener = scrollListener;
  }

  public int getVerticalScrollRange() {
    return computeVerticalScrollRange() - getHeight();
  }

}
