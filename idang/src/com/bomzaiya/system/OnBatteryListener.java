package com.bomzaiya.system;

public interface OnBatteryListener {
	/**
	 * scale 0-100
	 * bar 0-5
	 * @param scale
	 * @param bar
	 * @param status
	 */
	public abstract void onBatteryLevelChanged(int scale, int bar, boolean status);
}
