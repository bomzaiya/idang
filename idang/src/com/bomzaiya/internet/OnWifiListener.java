package com.bomzaiya.internet;

import android.net.NetworkInfo;

public interface OnWifiListener {
	public abstract void onWifiLevelChanged(int rssi);
	public abstract void onWifiStatusChanged(NetworkInfo networkInfo);
}
