package com.bomzaiya.service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;

import com.bomzaiya.app.idang.IdangApplication;
import com.bomzaiya.app.idang.ProductConfig;
import com.bomzaiya.internet.InternetHelper;
import com.bomzaiya.internet.OnAdminCommandListener;
import com.bomzaiya.internet.OnSocketListener;
import com.bomzaiya.internet.OnSocketServiceListener;
import com.bomzaiya.internet.OnWifiListener;
import com.bomzaiya.internet.SocketHelper;
import com.bomzaiya.system.SystemHelper;

public class SocketService extends Service implements OnSocketServiceListener, OnSocketListener, OnWifiListener {

  private static final String SOCKET_REQUEST_CONNECTION = "SOCKET_REQUEST_CONNECTION";
  private static final String SOCKET_REQUEST_LOGIN = "SOCKET_REQUEST_LOGIN";
  private static final String SOCKET_REQUEST_END = "SOCKET_REQUEST_END";

  private static final String SOCKET_EVENT = "event";
  private static final String SOCKET_DATA = "data";

  private static final String SOCKET_EVENT_LOGIN = "SOCKET_EVENT_LOGIN";
  private static final String SOCKET_EVENT_MESSAGE = "SOCKET_EVENT_MESSAGE";
  private static final String SOCKET_EVENT_COMMAND = "SOCKET_EVENT_COMMAND";
  private static final String SOCKET_EVENT_CONNECTION = "SOCKET_EVENT_CONNECTION";
  private static final String SOCKET_EVENT_END = "SOCKET_EVENT_END";
  private static final String SOCKET_EVENT_CONNECTED = "SOCKET_EVENT_CONNECTED";
  private static final String SOCKET_EVENT_PINGPONG = "SOCKET_EVENT_PINGPONG";
  private static final String SOCKET_EVENT_UPDATE = "SOCKET_EVENT_UPDATE";
  private static final String SOCKET_EVENT_FULLUPDATE = "SOCKET_EVENT_FULLUPDATE";
  private static final String SOCKET_EVENT_ADMINCOMMAND = "SOCKET_EVENT_ADMINCOMMAND";
  private static final String SOCKET_EVENT_BROADCAST = "SOCKET_EVENT_BROADCAST";
  private static final String SOCKET_EVENT_DATA_REQUEST = "SOCKET_EVENT_DATA_REQUEST";

  private static final int STATE_DISCONNECT = 0;
  private static final int STATE_CONNECTION = 1;
  private static final int STATE_LOGIN = 2;
  private static final int STATE_CONNECTED = 3;
  private static final int STATE_PINGPONG = 4;
  private static final int STATE_READY = 5;

  private Socket mSocket;
  private MyClient mClient;

  private boolean mPingPongResult;
  private int mPingPongSendCount = 0;
  private int mPingPongReceiveCount = 0;

  private static int mState = 0;
  private static boolean mStateSleep = false;
  private static String mEvent = "";

  public static final int SOCKET_DISCONNECT = 0;
  public static final int SOCKET_CONNECTED = 1;
  public static final int SOCKET_CONNECTING = 2;
  public static final int SOCKET_UPDATE = 3;
  public static final int SOCKET_COMMAND = 4;
  public static final int SOCKET_REQUEST_STATUS = 5;
  public static final int SOCKET_REQUEST_NEW_CONNECTION = 6;
  public static final int SOCKET_DATA_REQUEST = 7;
  public static final int SOCKET_DATA_RESPONSE = 8;
  public static final int SOCKET_FAIL = -1;
  public static final int SOCKET_FRCD_DOWN = -2;

  private MyHandler mUIHandler = new MyHandler(this);

  public JSONObject mSocketData;
  private JSONObject mAdminCommandData;

  // ====================================================

  // STATUS
  public static final String SOCKET_EXTRA_STATUS = "SOCKET_EXTRA_STATUS";
  public static final String SOCKET_EXTRA_REASON = "SOCKET_EXTRA_REASON";
  public static final String SOCKET_EXTRA_DATA = "SOCKET_EXTRA_DATA";
  public static final String SOCKET_EXTRA_COMMAND = "SOCKET_EXTRA_COMMAND";

  public static final String SOCKET_DISCONNECT_REASON_SERVER_GONE = "SERVER_DOWN";
  public static final String SOCKET_DISCONNECT_REASON_INTERNAL = "SOCKET_FAIL";
  public static final String SOCKET_DISCONNECT_REASON_NO_CONNECTION = "NO_CONNECTION_OR_SERVER_RESTART";
  public static final String SOCKET_DISCONNECT_REASON_JUST_START = "SOCKET_DISCONNECT_REASON_JUST_START";
  public static final String SOCKET_DISCONNECT_REASON_FRCD_DOWN = "FRCD_DOWN";

  // ACTION
  public static final int SOCKET_EMIT = 0;

  // SOCKET PARAM
  public static final String SOCKET_SERVER_FRCD_STATUS = "status";
  public static final String SOCKET_SERVER_ROOM_NUMBER = "room";
  public static final String SOCKET_SERVER_ROOM_TYPE = "room_type";
  public static final String SOCKET_SERVER_PHONE_STATUS = "phone_status";

  // SOCKET PARAM VALUE
  public static final String SOCKET_FRCD_STATUS_UP = "OK";
  public static final String SOCKET_FRCD_STATUS_DOWN = "FRCD";

  // GENERAL
  private String mMAC;
  private String mIMEI;

  // listeners
  private BroadcastReceiver mWifiListener;
  private BroadcastReceiver mConnectivityListener;
  private BroadcastReceiver mSocketListener;

  @Override
  public IBinder onBind(Intent arg0) {
    // load phone information
    loadPhoneInformation();

    // remove all receivers may left
    unregisterAllReceivers();

    registerReceivers();

    return null;
  }

  public void registerReceivers() {
    mConnectivityListener = InternetHelper.registerConnectivityListener(getBaseContext(), this);
    mWifiListener = InternetHelper.registerWifiSignalLevelListener(getBaseContext(), this);
    mSocketListener = SocketHelper.registerSocketListener(getBaseContext(), this);
  }

  private void emit(String event, Object data) {
    if (mSocket != null) {
      if (mSocket.isConnected() && !mSocket.isClosed()) {
        PrintWriter out = null;
        try {
          JSONObject jso = new JSONObject();
          try {
            SystemHelper.appendLog("socket_", "em: " + event + "," + data.toString());
            jso.put(SOCKET_DATA, data);
            jso.put(SOCKET_EVENT, event);
          } catch (JSONException e) {
          }

          if (mSocket != null && mSocket.isConnected() && !mSocket.isClosed()) {
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream())), true);
            // SystemHelper.appendLog("socket_", jso.toString());
            out.print(jso.toString());
          }

        } catch (NullPointerException e) {
          // e.printStackTrace();
        } catch (IOException e) {
          // e.printStackTrace();
        } finally {
          if (out != null) {
            out.flush();
          }
        }
      }
    }
  }

  private void trackState() {
    // showMsg("s: " + mState);
  }

  private int mStateTrack = 0;
  private BroadcastReceiver mFingiServiceListener;
  private boolean mFullUpdate;
  private ProductConfig mProductConfig;
  private String mSocketDataRequest = "";

  private void checkStateLost(boolean forceReconnect) {
    trackState();

    if (mState < STATE_CONNECTED) {
      if (mState > mStateTrack) {
        // state is going forward, if reach this comment
        mStateTrack = mState;
        showMsg("progressive..." + mStateTrack);
        // restate in 1500, if no progress
        mUIHandler.hasMessages(HANDLER_RESTATE);
        Message msg = mUIHandler.obtainMessage(HANDLER_RESTATE);
        mUIHandler.sendMessageDelayed(msg, 3500);
      } else {
        // somethings wrong here
        if (forceReconnect) {
          onDisconnect("STATE LOST");
          connectServer();
        } else {
          showMsg("looking like lost ..." + mState);
          if (!mUIHandler.hasMessages(HANDLER_RESTATE)) {
            Message msg = mUIHandler.obtainMessage(HANDLER_RESTATE);
            mUIHandler.sendMessageDelayed(msg, 1000);
          }
        }
      }
    } else {

      // onPingPong("no pingpong");
    }

  }

  class MyClient implements Runnable {

    @Override
    public void run() {
      int port = mProductConfig.getSocketPort();

      InetAddress serverAddr;
      boolean disconnect = false;
      try {
        mState = STATE_CONNECTION;

        long uptime = SystemClock.uptimeMillis();
        if (uptime < 120000) {
          Thread.sleep(2000);
        }

        serverAddr = InetAddress.getByName(mProductConfig.getSocketHost());

        showMsg("connecting..." + serverAddr.getHostAddress() + ":" + port);

        mSocket = new Socket(serverAddr, port);

        emit(SOCKET_REQUEST_CONNECTION, "");

        InputStream ins = null;
        InputStreamReader streamReader = null;

        ins = mSocket.getInputStream();
        streamReader = new InputStreamReader(ins, "UTF-8");

        String line = "";
        String response = "";
        String data = "";
        String event = "";
        boolean exit = false;
        do {
          response = "";

          // if stream not ready means server go away
          // check but not force to reconnect
          checkStateLost(false);

          int read = 0;
          while ((read = streamReader.read()) != -1 && !mSocket.isClosed() && mSocket.isConnected()
              && streamReader.ready()) {
            char ch = (char) read;
            response += Character.toString(ch);

            if (streamReader.ready() == false) {
              break;
            }
          }
          showMsg("read out..");

          if (response == "") {
            mStateSleep = true;
          }
          // decode json string
          try {
            JSONObject jsonObject = new JSONObject(response);
            event = jsonObject.getString("event");
            data = jsonObject.getString("data");
          } catch (JSONException e1) {
          }

          // define state
          if (event.equals(SOCKET_REQUEST_LOGIN)) {
            mState = STATE_LOGIN;
            mEvent = "";
            event = "";
            mStateSleep = false;
          } else if (event.equals(SOCKET_EVENT_CONNECTED)) {
            mState = STATE_CONNECTED;
            mEvent = "";
            event = "";
            mStateSleep = false;
          } else if (event.equals(SOCKET_EVENT_UPDATE)) {
            mState = STATE_READY;
            mEvent = SOCKET_EVENT_UPDATE;
            event = "";
            mStateSleep = false;
          } else if (event.equals(SOCKET_EVENT_MESSAGE)) {
            mState = STATE_READY;
            mEvent = SOCKET_EVENT_MESSAGE;
            event = "";
            mStateSleep = false;
          } else if (event.equals(SOCKET_EVENT_BROADCAST)) {
            mState = STATE_READY;
            mEvent = SOCKET_EVENT_BROADCAST;
            event = "";
            mStateSleep = false;
          } else if (event.equals(SOCKET_EVENT_PINGPONG)) {
            mState = STATE_PINGPONG;
            mEvent = SOCKET_EVENT_PINGPONG;
            event = "";
            mStateSleep = false;
          } else if (event.equals(SOCKET_EVENT_ADMINCOMMAND)) {
            mState = STATE_READY;
            mEvent = SOCKET_EVENT_ADMINCOMMAND;
            event = "";
            mStateSleep = false;
          } else if (event.equals(SOCKET_EVENT_END)) {
            mState = STATE_DISCONNECT;
            mEvent = SOCKET_EVENT_END;
            event = "";
            mStateSleep = false;
          } else if (event.equals(SOCKET_EVENT_DATA_REQUEST)) {
            mState = STATE_READY;
            mEvent = SOCKET_EVENT_DATA_REQUEST;
            event = "";
            mStateSleep = false;
          }

          if (mStateSleep) {
            showMsg("ohoh...");
            break;
          } else {
            switch (mState) {
            case STATE_LOGIN:
              onLogin();
              mStateSleep = true;
              break;

            case STATE_CONNECTED:
              onConnected(data);
              mState = STATE_PINGPONG;
              mStateSleep = false;
              break;

            case STATE_PINGPONG:
              onPingPong(data);
              break;

            case STATE_READY:
              if (mEvent.equals(SOCKET_EVENT_UPDATE)) {
                onUpdate(data);
              } else if (mEvent.equals(SOCKET_EVENT_ADMINCOMMAND)) {
                onAdminCommand(data);
              } else if (mEvent.equals(SOCKET_EVENT_MESSAGE)) {
                onMessage(data);
              } else if (mEvent.equals(SOCKET_EVENT_BROADCAST)) {
                onBroadCast(data);
              } else if (mEvent.equals(SOCKET_EVENT_DATA_REQUEST)) {
                onDataRequest(data);
              }
              mState = STATE_PINGPONG;
              mStateSleep = false;
              break;

            case STATE_DISCONNECT:
              onDisconnect(data);
              mState = STATE_PINGPONG;
              break;

            default:
              break;

            }

            // state ping pong can come back
            if (mState == STATE_PINGPONG) {
              if (!mUIHandler.hasMessages(HANDLER_PINGPONG)) {
                Message msg = mUIHandler.obtainMessage(HANDLER_PINGPONG);
                mUIHandler.sendMessageDelayed(msg, 25000);
              }
            }

          }

          response = "";

          trackState();

          if (line != null) {
            if (line.toString().indexOf(SOCKET_EVENT_END) != -1) {
              exit = true;
            }
          }
          // showMsg("-vv" + mState);
          Thread.sleep(100);
        } while (!exit && mSocket != null && mSocket.isConnected() && !mSocket.isClosed());

      } catch (UnknownHostException e2) {
        disconnect = true;
        onDisconnect("SERVER HOST GONE");
      } catch (IOException e) {
        disconnect = true;
        onDisconnect("SERVER IO GONE");
      } catch (Exception e) {
        showMsg("error unknown");
      } finally {
        if (!disconnect) {
          onDisconnect("SERVER DOWN either admin, connection change, or back & forth tethering");
        }
        if (!mUIHandler.hasMessages(HANDLER_RECONNECT)) {
          mUIHandler.removeMessages(HANDLER_RECONNECT);
          Message msg = mUIHandler.obtainMessage(HANDLER_RECONNECT);
          mUIHandler.sendMessageDelayed(msg, 5000);
        }
      }
    }
  }

  public void connectServer() {
    mUIHandler.removeMessages(HANDLER_PINGPONG);
    mUIHandler.removeMessages(HANDLER_RESTATE);
    mUIHandler.removeMessages(HANDLER_SEND_BROADCAST);

    String msg = "";
    try {
      mClient = new MyClient();
      Thread thread = new Thread(mClient);
      thread.start();

      // tell that connecting...
      Intent intentConnecting = new Intent();
      intentConnecting.putExtra(SOCKET_EXTRA_STATUS, SOCKET_CONNECTING);
      intentConnecting.setAction(ProductConfig.INTENT_ACTION_IDANG_SOCKET_STATUS);
      sendBroadcast(intentConnecting);

    } catch (Exception e) {
      msg = "error: " + e;
      showMsg(msg);
    } finally {
      // socket.close();
    }
  }

  @Override
  public void onLogin() {
    showMsg("login..." + mIMEI + "," + mMAC);
    JSONObject jso = new JSONObject();
    try {
      jso.put("imei", mIMEI);
      jso.put("mac", mMAC);
      emit(SOCKET_EVENT_LOGIN, jso);
    } catch (JSONException e) {
    }
  }

  @Override
  public void onConnected(String data) {
    try {
      mSocketData = new JSONObject(data);
      showMsg("ct: " + data);

    } catch (JSONException e) {
    }
    showMsg("connected!!!");
    mPingPongResult = true;

    // broadcase socket connected
    mUIHandler.removeMessages(HANDLER_SEND_BROADCAST);
    Message msg = mUIHandler.obtainMessage(HANDLER_SEND_BROADCAST);
    msg.arg1 = SOCKET_CONNECTED;
    msg.obj = ProductConfig.INTENT_ACTION_IDANG_SOCKET_STATUS;
    mUIHandler.sendMessage(msg);

  }

  public void onPingPong(String data) {
    mFullUpdate = true;
    // ping pong receive
    showMsg("p:" + data);
    mPingPongReceiveCount++;

    if (mPingPongSendCount > 5) {
      if (mPingPongSendCount == mPingPongReceiveCount) {
        mPingPongResult = true;
      } else {
        mPingPongResult = false;
        onDisconnect("pinpong fail");
        connectServer();
      }
      mPingPongSendCount = 0;
      mPingPongReceiveCount = 0;
    }

  }

  @Override
  public void onUpdate(String data) {
    showMsg("u: " + data);
    mFullUpdate = true;

    try {
      mSocketData = new JSONObject(data);
    } catch (JSONException e) {
    }

    String frcdStatus = "";
    try {
      frcdStatus = mSocketData.getString(SOCKET_SERVER_FRCD_STATUS);
    } catch (JSONException e) {
    }

    if (frcdStatus.equals(SOCKET_FRCD_STATUS_UP)) {
      mUIHandler.removeMessages(HANDLER_SEND_BROADCAST);
      Message msg = mUIHandler.obtainMessage(HANDLER_SEND_BROADCAST);
      msg.arg1 = SOCKET_UPDATE;
      msg.obj = ProductConfig.INTENT_ACTION_IDANG_SOCKET_STATUS;
      mUIHandler.sendMessage(msg);
    } else {
      // SOCKET_FRCD_STATUS_DOWN
      mUIHandler.removeMessages(HANDLER_SEND_BROADCAST);
      Message msg = mUIHandler.obtainMessage(HANDLER_SEND_BROADCAST);
      msg.arg1 = SOCKET_FRCD_DOWN;
      msg.obj = ProductConfig.INTENT_ACTION_IDANG_SOCKET_STATUS;
      mUIHandler.sendMessage(msg);
    }
  }

  @Override
  public void onAdminCommand(String data) {
    showMsg("adminCommand: " + data);

    try {
      mAdminCommandData = new JSONObject(data);

      String command = mAdminCommandData.getString(OnAdminCommandListener.ADMINCOMMAND_FIELD_COMMAND);
      String action = mAdminCommandData.getString(OnAdminCommandListener.ADMINCOMMAND_FIELD_ACTION);
      if (command.toLowerCase().equals(OnAdminCommandListener.ADMINCOMMAND_COMMAND_PHONECONFIG)) {
        if (action.toUpperCase().equals(OnAdminCommandListener.ADMINCOMMAND_ACTION_CHECKOUT)) {
          // broadcast checkout
          mUIHandler.removeMessages(HANDLER_SEND_ADMINCOMMAND_BROADCAST);
          Message msg = mUIHandler.obtainMessage(HANDLER_SEND_ADMINCOMMAND_BROADCAST);
          msg.arg1 = OnAdminCommandListener.ADMINCOMMAND_STATUS_CHECKOUT;
          msg.obj = ProductConfig.INTENT_ACTION_IDANG_ADMINCOMMAND;
          mUIHandler.sendMessage(msg);
        } else if (action.toUpperCase().equals(OnAdminCommandListener.ADMINCOMMAND_ACTION_CHECKIN)) {
          // check in
          mUIHandler.removeMessages(HANDLER_SEND_ADMINCOMMAND_BROADCAST);
          Message msg = mUIHandler.obtainMessage(HANDLER_SEND_ADMINCOMMAND_BROADCAST);
          msg.arg1 = OnAdminCommandListener.ADMINCOMMAND_STATUS_CHECKIN;
          msg.obj = ProductConfig.INTENT_ACTION_IDANG_ADMINCOMMAND;
          mUIHandler.sendMessage(msg);
        } else if (action.toUpperCase().equals(OnAdminCommandListener.ADMINCOMMAND_ACTION_CHECKMOVE)) {
          // check move
          mUIHandler.removeMessages(HANDLER_SEND_ADMINCOMMAND_BROADCAST);
          Message msg = mUIHandler.obtainMessage(HANDLER_SEND_ADMINCOMMAND_BROADCAST);
          msg.arg1 = OnAdminCommandListener.ADMINCOMMAND_STATUS_CHECKMOVE;
          msg.obj = ProductConfig.INTENT_ACTION_IDANG_ADMINCOMMAND;
          mUIHandler.sendMessage(msg);
        }
      }

    } catch (JSONException e) {
    }
  }

  @Override
  public void onBroadCast(String data) {
    showMsg("b: " + data);
  }

  @Override
  public void onDataRequest(String data) {
    showMsg("dr: " + data);

    mSocketDataRequest = data;

    mUIHandler.removeMessages(HANDLER_SEND_BROADCAST);
    Message msg = mUIHandler.obtainMessage(HANDLER_SEND_BROADCAST);
    msg.arg1 = SOCKET_DATA_RESPONSE;
    msg.obj = ProductConfig.INTENT_ACTION_IDANG_SOCKET_STATUS;
    mUIHandler.sendMessage(msg);
  }

  @Override
  public void onDisconnect(String data) {
    // clear all handlers
    mUIHandler.removeMessages(HANDLER_PINGPONG);
    mUIHandler.removeMessages(HANDLER_RESTATE);
    mUIHandler.removeMessages(HANDLER_SEND_BROADCAST);

    mState = STATE_DISCONNECT;
    mStateTrack = STATE_DISCONNECT;
    mPingPongResult = false;

    showMsg("disconnected!!!" + data);
    try {
      if (mSocket != null) {
        mSocket.close();
        mSocket = null;
      }
      mClient = null;

    } catch (IOException e) {
    }

    // broadcast socket disconnected, lazy send a bit, the connection might
    // be back very soon
    mUIHandler.removeMessages(HANDLER_SEND_BROADCAST);
    Message msg = mUIHandler.obtainMessage(HANDLER_SEND_BROADCAST);
    msg.arg1 = SOCKET_DISCONNECT;
    msg.obj = ProductConfig.INTENT_ACTION_IDANG_SOCKET_STATUS;
    mUIHandler.sendMessageDelayed(msg, 5000);
  }

  private static final int HANDLER_PINGPONG = 0;
  private static final int HANDLER_RESTATE = 1;
  private static final int HANDLER_RECONNECT = 2;
  private static final int HANDLER_SEND_BROADCAST = 3;
  private static final int HANDLER_SEND_ADMINCOMMAND_BROADCAST = 4;

  private static class MyHandler extends Handler {
    private WeakReference<SocketService> mService = null;

    public MyHandler(SocketService service) {
      mService = new WeakReference<SocketService>(service);
    }

    @Override
    public void handleMessage(Message msg) {
      SocketService service = mService.get();
      if (service != null) {
        switch (msg.what) {
        case HANDLER_PINGPONG:
          // send ping pong
          service.mPingPongSendCount++;
          service.showMsg(">");
          service.emit(SOCKET_EVENT_PINGPONG, ">");
          break;

        case HANDLER_RESTATE:
          // try to check state again
          service.checkStateLost(true);
          break;

        case HANDLER_RECONNECT:
          // service.onDisconnect("STATE FAIL made reconnect");
          if (service.mSocket == null || !service.mSocket.isConnected() || service.mSocket.isClosed()) {
            service.connectServer();
          }
          break;

        case HANDLER_SEND_BROADCAST:
          int status = 0;
          String reason = "";
          String extraData = "";
          // it fail but send it as disconnect
          switch (msg.arg1) {
          case SOCKET_DISCONNECT:
            SystemHelper.appendLog("socket_", "SOCKET_DISCONNECT");
            // real disconnect
            status = msg.arg1;
            reason = SOCKET_DISCONNECT_REASON_NO_CONNECTION;

            break;

          case SOCKET_CONNECTED:
            SystemHelper.appendLog("socket_", "SOCKET_CONNECTED");
            status = msg.arg1;
            reason = "";
            try {
              extraData = service.mSocketData.toString();
            } catch (NullPointerException e) {
            }

            break;

          case SOCKET_UPDATE:
            SystemHelper.appendLog("socket_", "SOCKET_UPDATE");
            status = msg.arg1;

            String frcdStatus = "";
            try {
              frcdStatus = service.mSocketData.getString(SOCKET_SERVER_FRCD_STATUS);
            } catch (NullPointerException e) {
            } catch (JSONException e) {
            }

            // notify socket is connected if frcd is up
            if (frcdStatus.equals(SOCKET_FRCD_STATUS_UP)) {
              Intent intent = new Intent();
              intent.putExtra(SOCKET_EXTRA_STATUS, SOCKET_CONNECTED);
              intent.setAction(msg.obj.toString());
              service.sendBroadcast(intent);
            }

            reason = "";
            try {
              extraData = service.mSocketData.toString();
              // SystemHelper.appendLog("socket_", "cache: " + extraData);
            } catch (NullPointerException e1) {
            }

            break;

          case SOCKET_FRCD_DOWN:
            SystemHelper.appendLog("socket_", "SOCKET_FRCD_DOWN");
            status = msg.arg1;
            reason = SOCKET_DISCONNECT_REASON_FRCD_DOWN;
            break;

          case SOCKET_DATA_RESPONSE:
            SystemHelper.appendLog("socket_", "SOCKET_DATA_RESPONSE");
            status = msg.arg1;
            reason = "";
            try {
              extraData = service.mSocketDataRequest;
            } catch (NullPointerException e1) {
            }
            break;

          default:
            SystemHelper.appendLog("socket_", "socket_OTHER");
            status = msg.arg1;
            reason = "";
            break;
          }

          Intent intent = new Intent();
          intent.putExtra(SOCKET_EXTRA_STATUS, status);
          intent.putExtra(SOCKET_EXTRA_REASON, reason);
          intent.putExtra(SOCKET_EXTRA_DATA, extraData);
          intent.setAction(msg.obj.toString());
          service.sendBroadcast(intent);

          break;

        case HANDLER_SEND_ADMINCOMMAND_BROADCAST:
          Intent adminIntent = new Intent();
          adminIntent.putExtra(OnAdminCommandListener.ADMINCOMMAND_EXTRA_ACTION, msg.arg1);
          adminIntent.putExtra(OnAdminCommandListener.ADMINCOMMAND_EXTRA_DATA, service.mAdminCommandData.toString());
          adminIntent.setAction(msg.obj.toString());
          service.sendBroadcast(adminIntent);
          break;

        default:
          break;
        }
      }
    }

  };

  @Override
  public void onCreate() {
    super.onCreate();

    IdangApplication app = (IdangApplication) getApplication();
    mProductConfig = app.getProductConfig();

    if (mProductConfig == null) {
      mProductConfig = new ProductConfig(getBaseContext());
      app.setProductConfig(mProductConfig);
    }

    // load phone information
    loadPhoneInformation();

    // register listeners
    mConnectivityListener = InternetHelper.registerConnectivityListener(getBaseContext(), this);
    mWifiListener = InternetHelper.registerWifiSignalLevelListener(getBaseContext(), this);
    mSocketListener = SocketHelper.registerSocketListener(getBaseContext(), this);

  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    unregisterAllReceivers();
  }

  private void unregisterAllReceivers() {
    // SystemHelper.appendLog("socket_",
    // "RECEIVER = socket listener Remove!!!!!!!!!");
    InternetHelper.unregisterListener(getBaseContext(), mConnectivityListener);
    InternetHelper.unregisterListener(getBaseContext(), mWifiListener);
    SocketHelper.unregisterListener(getBaseContext(), mSocketListener);
    mSocketListener = null;
    mWifiListener = null;
    mConnectivityListener = null;

    try {
      if (mFingiServiceListener != null) {
        unregisterReceiver(mFingiServiceListener);
        mFingiServiceListener = null;
      }
    } catch (Exception e) {
    }

    onDisconnect("Unregister");
  }

  private void loadPhoneInformation() {
    mIMEI = SystemHelper.getPhoneIMEI(getBaseContext());
    mMAC = SystemHelper.getPhoneMAC(getBaseContext());

    if (mIMEI == "") {
      mIMEI = SystemHelper.getSharePreferenceValue(getBaseContext(), "imei");
    } else {
      SystemHelper.setSharePreferenceValue(getBaseContext(), "imei", mIMEI);
    }

    if (mMAC == "") {
      mMAC = SystemHelper.getSharePreferenceValue(getBaseContext(), "mac");
    } else {
      SystemHelper.setSharePreferenceValue(getBaseContext(), "mac", mMAC);
    }
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    return super.onStartCommand(intent, flags, startId);
  }

  @Override
  public boolean onUnbind(Intent intent) {
    // remove all receivers may left
    unregisterAllReceivers();
    return super.onUnbind(intent);
  }

  @Override
  public void onMessage(String message) {
    // showMsg("message: " + message);
  }

  @Override
  public void onWifiLevelChanged(final int rssi) {

  }

  private int mCurrentNetworkType = -1;

  @Override
  public void onWifiStatusChanged(NetworkInfo networkInfo) {
    String msg = "";
    if (networkInfo == null) {
      msg = "offline";
      onDisconnect(msg);
    } else {
      if (networkInfo.getType() == -1) {
        msg = "offline";
        onDisconnect(msg);
      } else {
        msg = networkInfo.getTypeName();

        if (networkInfo.getType() == mCurrentNetworkType) {
          // do nothing, if it is still the same network type
        } else {
          mCurrentNetworkType = networkInfo.getType();
          onDisconnect(msg);
          connectServer();
        }
      }
    }
    showMsg("network: " + msg);
  }

  private void showMsg(final String msg) {
    SystemHelper.appendLog("socket_", msg);
  }

  @Override
  public void onSocketCommand(String data) {
    SystemHelper.appendLog("socket_", "com:" + data);
    emit(SOCKET_EVENT_COMMAND, data);
  }

  @Override
  public void onSocketRequestStatus() {
    if (!mFullUpdate) {
      mFullUpdate = false;
      emit(SOCKET_EVENT_FULLUPDATE, "");
    }

    // return current status on the phone first
    try {
      String frcdStatus = "";
      try {
        frcdStatus = mSocketData.getString(SOCKET_SERVER_FRCD_STATUS);
      } catch (JSONException e) {
      }

      if (frcdStatus.equals(SOCKET_FRCD_STATUS_UP)) {
        mUIHandler.removeMessages(HANDLER_SEND_BROADCAST);
        Message msg = mUIHandler.obtainMessage(HANDLER_SEND_BROADCAST);
        msg.arg1 = SOCKET_UPDATE;
        msg.obj = ProductConfig.INTENT_ACTION_IDANG_SOCKET_STATUS;
        mUIHandler.sendMessage(msg);
      } else {
        // SOCKET_FRCD_STATUS_DOWN
        mUIHandler.removeMessages(HANDLER_SEND_BROADCAST);
        Message msg = mUIHandler.obtainMessage(HANDLER_SEND_BROADCAST);
        msg.arg1 = SOCKET_FRCD_DOWN;
        msg.obj = ProductConfig.INTENT_ACTION_IDANG_SOCKET_STATUS;
        mUIHandler.sendMessage(msg);
      }
    } catch (NullPointerException e) {
    }
  }

  @Override
  public void onSocketConnected(JSONObject data) {
    // ignore, it will cause loop
  }

  @Override
  public void onSocketDisconnected(String reason) {
    // ignore, it will cause loop
  }

  @Override
  public void onSocketReconnecting() {
    // ignore, it will cause loop
  }

  @Override
  public void onSocketUpdate(JSONObject data) {
    // ignore, it will cause loop
  }

  @Override
  public void onSocketFRCDDown() {
    // TODO Auto-generated method stub

  }

  @Override
  public void onSocketRequestConnection() {
    onDisconnect("REQUEST RECONNECT");
    connectServer();
  }

  @Override
  public void onSocketDataRequest(JSONObject data) {
    emit(SOCKET_EVENT_DATA_REQUEST, data);
  }

  @Override
  public void onSocketDataResponse(JSONObject data) {
    // do nothing
  }

}
