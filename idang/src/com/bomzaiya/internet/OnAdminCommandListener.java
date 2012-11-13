package com.bomzaiya.internet;

import org.json.JSONObject;

public interface OnAdminCommandListener {
	// command must be lowercase
	public static final String ADMINCOMMAND_FIELD_COMMAND = "command";
	// action must be capitalized
	public static final String ADMINCOMMAND_FIELD_ACTION = "ACTION";
	
	public static final String ADMINCOMMAND_COMMAND_PHONECONFIG = "phoneconfig";
	public static final String ADMINCOMMAND_ACTION_CHECKOUT = "CHKO";
	public static final String ADMINCOMMAND_ACTION_CHECKIN = "CHKI";
	public static final String ADMINCOMMAND_ACTION_CHECKMOVE = "CMOV";
	
	public static final int ADMINCOMMAND_STATUS_CHECKIN = 1;
	public static final int ADMINCOMMAND_STATUS_CHECKOUT = 2;
	public static final int ADMINCOMMAND_STATUS_CHECKMOVE = 3;
	
	public static final String ADMINCOMMAND_EXTRA_ACTION = "ADMINCOMMAND_EXTRA_ACTION";
	public static final String ADMINCOMMAND_EXTRA_DATA = "ADMINCOMMAND_EXTRA_DATA";
	
	public abstract void onAdminCommandCheckIn(JSONObject data);
	public abstract void onAdminCommandCheckOut(JSONObject data);
}
