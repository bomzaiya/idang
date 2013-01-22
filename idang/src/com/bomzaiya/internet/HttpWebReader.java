package com.bomzaiya.internet;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.bomzaiya.ui.IdangApplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class HttpWebReader {
  
  public interface OnInternet {
    public abstract void onOnline();

    public abstract void onOffline();
  }
  private DefaultHttpClient mHttpClient;

  public void setSSL(String domain, String username, String password) {
    // self-signed certificate
    SchemeRegistry schemeRegistry = new SchemeRegistry();
    schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
    schemeRegistry.register(new Scheme("https", new EasySSLSocketFactory(), 443));

    BasicHttpParams params = new BasicHttpParams();
    params.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 1);
    params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, new ConnPerRouteBean(1));
    params.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);

    HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
    HttpProtocolParams.setContentCharset(params, "UTF-8");
    HttpProtocolParams.setHttpElementCharset(params, "UTF-8");

    CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

    credentialsProvider.setCredentials(new AuthScope(domain, AuthScope.ANY_PORT), new UsernamePasswordCredentials(username, password));
    ThreadSafeClientConnManager clientConnectionManager = new ThreadSafeClientConnManager(params, schemeRegistry);

    // connection (client has to be created for every new connection)
    mHttpClient = new DefaultHttpClient(clientConnectionManager, params);
  }

  public void readJSON(String filename, final OnHttpWebListener onHttpWebListener) {
    HttpGet get = new HttpGet(filename);
    try {
      mHttpClient.execute(get, new ResponseHandler<String>() {

        @Override
        public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
          HttpEntity entity = response.getEntity();
          InputStream content = entity.getContent();

          InputStreamReader streamReader = new InputStreamReader(content, "UTF-8");
          StringBuilder buffer = new StringBuilder();
          try {
            char[] tmp = new char[1024];
            int l;
            while ((l = streamReader.read(tmp)) != -1) {
              buffer.append(tmp, 0, l);
            }
          } finally {
            streamReader.close();
          }
          JSONObject jso = null;
          try {
            jso = new JSONObject(buffer.toString());
            onHttpWebListener.onJSONReceive(jso, OnHttpWebListener.JSON_OBJECT);
          } catch (JSONException e) {
            // it is not json object, so try array
            JSONArray jsoArray = null;
            try {
              jsoArray = new JSONArray(buffer.toString());
              onHttpWebListener.onJSONReceive(jsoArray, OnHttpWebListener.JSON_ARRAY);
            } catch (JSONException e1) {
            }
          }

          return "";
        }
      });

    } catch (ClientProtocolException e) {
      onHttpWebListener.onHttpWebError();
    } catch (IOException e) {
      onHttpWebListener.onHttpWebError();
    }
    return;
  }


  public void isOnline(IdangApplication application, String webHost, final OnInternet onInternet) {
    HttpGet get = new HttpGet(webHost);
    try {
      mHttpClient.execute(get, new ResponseHandler<String>() {
        @Override
        public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
          onInternet.onOnline();
          return "";
        }
      });
    } catch (ClientProtocolException e) {
      onInternet.onOffline();
    } catch (IOException e) {
      onInternet.onOffline();
    }
    return;
  }

  public void readString(String filename, final OnHttpWebListener onHttpWebListener) {
    HttpGet get = new HttpGet(filename);
    try {
      mHttpClient.execute(get, new ResponseHandler<String>() {
        @Override
        public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
          HttpEntity entity = response.getEntity();
          InputStream content = entity.getContent();

          InputStreamReader streamReader = new InputStreamReader(content, "UTF-8");
          StringBuilder buffer = new StringBuilder();
          try {
            char[] tmp = new char[1024];
            int l;
            while ((l = streamReader.read(tmp)) != -1) {
              buffer.append(tmp, 0, l);
            }
          } finally {
            streamReader.close();
          }
          onHttpWebListener.onStringReceive(buffer.toString());
          content.close();
          return "";
        }
      });

    } catch (ClientProtocolException e) {
      onHttpWebListener.onHttpWebError();
    } catch (IOException e) {
      onHttpWebListener.onHttpWebError();
    }
    return;
  }

  public void readStream(String filename, final OnHttpWebListener onHttpWebListener) {
    HttpGet get = new HttpGet(filename);
    try {
      mHttpClient.execute(get, new ResponseHandler<String>() {

        @Override
        public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
          HttpEntity entity = response.getEntity();
          InputStream content = entity.getContent();

          onHttpWebListener.onStreamReceive(content);

          content.close();
          return "";
        }
      });

    } catch (ClientProtocolException e) {
      onHttpWebListener.onHttpWebError();
    } catch (IOException e) {
      onHttpWebListener.onHttpWebError();
    }
    return;
  }

  public Bitmap readBitmap(String filename, final OnHttpWebListener onHttpWebListener) {
    HttpGet get = new HttpGet(filename);
    Bitmap bitmap = null;
    try {
      bitmap = mHttpClient.execute(get, new ResponseHandler<Bitmap>() {

        @Override
        public Bitmap handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
          HttpEntity entity = response.getEntity();
          InputStream content = entity.getContent();

          Bitmap bit = BitmapFactory.decodeStream(content);

          onHttpWebListener.onBitmapReceive(bit);

          content.close();
          return bit;
        }
      });

    } catch (ClientProtocolException e) {
      onHttpWebListener.onHttpWebError();
    } catch (IOException e) {
      onHttpWebListener.onHttpWebError();
    }
    return bitmap;
  }

  public Bitmap readDrawable(String filename, final OnHttpWebListener onHttpWebListener) {
    HttpGet get = new HttpGet(filename);
    Bitmap bitmap = null;
    try {
      bitmap = mHttpClient.execute(get, new ResponseHandler<Bitmap>() {

        @Override
        public Bitmap handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
          HttpEntity entity = response.getEntity();
          InputStream content = entity.getContent();

          Bitmap bit = BitmapFactory.decodeStream(content);
          @SuppressWarnings("deprecation")
          Drawable d = new BitmapDrawable(bit);
          onHttpWebListener.onDrawableReceive(d);

          content.close();
          return bit;
        }
      });

    } catch (ClientProtocolException e) {
      onHttpWebListener.onHttpWebError();
    } catch (IOException e) {
      onHttpWebListener.onHttpWebError();
    }
    return bitmap;
  }

}
