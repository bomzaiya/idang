package com.bomzaiya.internet;

import org.json.JSONObject;

public interface OnSocketServiceListener {
	public abstract void onSocketRequestConnection();
	public abstract void onSocketConnected(JSONObject data);
	public abstract void onSocketDisconnected(String reason);
	public abstract void onSocketReconnecting();
	public abstract void onSocketUpdate(JSONObject data);
	public abstract void onSocketCommand(String command);
	public abstract void onSocketRequestStatus();
	public abstract void onSocketFRCDDown();
	public abstract void onSocketDataRequest(JSONObject data);
	public abstract void onSocketDataResponse(JSONObject data);
}
