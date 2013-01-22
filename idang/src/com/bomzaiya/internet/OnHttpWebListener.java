package com.bomzaiya.internet;

import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

public interface OnHttpWebListener {
	public final boolean SUCCESS = true;
	public final String FAIL = "fail";
	public final int JSON_OBJECT = 0;
	public final int JSON_ARRAY = 1;
	
//	@SuppressLint("ParserError")
	public abstract void onJSONReceive(Object object, int type);
	public abstract void onStringReceive(String data);
	public abstract void onStreamReceive(InputStream content);
	public abstract void onBitmapReceive(Bitmap bit);
	public abstract void onDrawableReceive(Drawable d);
	public abstract void onHttpWebError();
}

