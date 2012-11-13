package com.bomzaiya.internet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.bomzaiya.app.idang.ProductConfig;
import com.bomzaiya.service.SocketService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class SocketHelper {

  public static BroadcastReceiver registerSocketListener(Context context,
      final OnSocketServiceListener onSocketServiceListener) {
    BroadcastReceiver socketStatusReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        int status = intent.getIntExtra(SocketService.SOCKET_EXTRA_STATUS, SocketService.SOCKET_DISCONNECT);
        String reason = intent.getStringExtra(SocketService.SOCKET_EXTRA_REASON);
        String data = intent.getStringExtra(SocketService.SOCKET_EXTRA_DATA);
        String command = intent.getStringExtra(SocketService.SOCKET_EXTRA_COMMAND);

        switch (status) {
        case SocketService.SOCKET_CONNECTED:
          try {
            JSONObject jsoData = new JSONObject(data);
            onSocketServiceListener.onSocketConnected(jsoData);
          } catch (NullPointerException e) {
          } catch (JSONException e) {
          }
          break;
        case SocketService.SOCKET_DISCONNECT:
          onSocketServiceListener.onSocketDisconnected(reason);
          break;
        case SocketService.SOCKET_CONNECTING:
          onSocketServiceListener.onSocketReconnecting();
          break;
        case SocketService.SOCKET_UPDATE:
          try {
            JSONObject jsoData = new JSONObject(data);
            onSocketServiceListener.onSocketUpdate(jsoData);
          } catch (NullPointerException e) {
          } catch (JSONException e) {
          }
          break;
        case SocketService.SOCKET_COMMAND:
          onSocketServiceListener.onSocketCommand(command);
          break;
        case SocketService.SOCKET_REQUEST_STATUS:
          onSocketServiceListener.onSocketRequestStatus();
          break;
        case SocketService.SOCKET_FRCD_DOWN:
          onSocketServiceListener.onSocketFRCDDown();
          break;
        case SocketService.SOCKET_REQUEST_NEW_CONNECTION:
          onSocketServiceListener.onSocketRequestConnection();
          break;
        case SocketService.SOCKET_DATA_REQUEST:
          try {
            JSONObject jsoData = new JSONObject(data);
            onSocketServiceListener.onSocketDataRequest(jsoData);
          } catch (NullPointerException e) {
          } catch (JSONException e) {
          }
          break;

        case SocketService.SOCKET_DATA_RESPONSE:
          JSONObject jsoData = null;
          try {
            jsoData = new JSONObject(data);
          } catch (NullPointerException e) {
          } catch (JSONException e) {
            try {
              jsoData = new JSONObject();
              JSONArray jsoArray = new JSONArray(data);
              jsoData.put("list", jsoArray);
            } catch (JSONException e1) {
            }
          }

          onSocketServiceListener.onSocketDataResponse(jsoData);
          break;

        default:
          break;
        }

      }
    };
    IntentFilter filter = new IntentFilter(ProductConfig.INTENT_ACTION_IDANG_SOCKET_STATUS);
    context.registerReceiver(socketStatusReceiver, filter);
    return socketStatusReceiver;
  }

  /**
   * Unregister any kind of broadcast receiver
   */
  public static void unregisterListener(Context context, BroadcastReceiver broadcastReceiver) {
    try {
      if (broadcastReceiver != null) {
        context.unregisterReceiver(broadcastReceiver);
        broadcastReceiver = null;
      }
    } catch (Exception e) {
    }
  }

}