package com.bomzaiya.internet;

public interface OnSocketListener {
  public void onMessage(String message);

  public void onConnected(String data);

  public void onLogin();
  
  public void onPingPong(String data);

  public void onDisconnect(String data);

  public void onAdminCommand(String command);
  
  public void onUpdate(String data);
  
  public void onBroadCast(String data);
  
  public void onDataRequest(String data);
}