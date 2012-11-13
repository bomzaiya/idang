package com.bomzaiya.internet;

import android.telephony.TelephonyManager;

public interface OnGsmListener {
	public abstract void onGsmLevelChanged(int scaled, TelephonyManager telephonyManager);
}
