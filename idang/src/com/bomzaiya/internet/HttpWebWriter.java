package com.bomzaiya.internet;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

public class HttpWebWriter {
  private DefaultHttpClient mHttpClient;

  public HttpWebWriter() {
    setSSL("fordfiestaclub.net", "fiesta", "");
  }

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

  /**
   * execute post with whole json data as param
   * 
   * @param serverPath
   * @param json
   * @param onHttpWebListener
   */
  public void executePost(String serverPath, JSONObject json, final OnHttpWebListener onHttpWebListener) {
    HttpPost post = new HttpPost(serverPath);
    try {
      post.setEntity(new StringEntity(json.toString(), "UTF8"));
      post.setHeader("Content-type", "application/json");
    } catch (UnsupportedEncodingException e) {
      // e.printStackTrace();
    }
    // set this to avoid 417 error (Expectation Failed)
    post.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
    try {
      mHttpClient.execute(post, new ResponseHandler<String>() {

        @Override
        public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
          boolean error = false;
          HttpEntity entity = response.getEntity();
          InputStream content = null;
          StringBuilder buffer = null;
          try {
            content = entity.getContent();

            InputStreamReader streamReader = new InputStreamReader(content, "UTF-8");
            buffer = new StringBuilder();
            try {
              char[] tmp = new char[1024];
              int l;
              while ((l = streamReader.read(tmp)) != -1) {
                buffer.append(tmp, 0, l);
              }
            } finally {
              streamReader.close();
            }
            content.close();
          } catch (IllegalStateException e1) {
            error = true;
          } catch (IOException e1) {
            error = true;
          }

          if (!error) {
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
          } else {
            onHttpWebListener.onHttpWebError();
          }
          return "";
        }
      });
    } catch (ClientProtocolException e) {
      onHttpWebListener.onHttpWebError();
    } catch (IOException e) {
      onHttpWebListener.onHttpWebError();
    }
  }

  /**
   * execute post with name value pairs
   * 
   * @param serverPath
   * @param json
   * @param onHttpWebListener
   */
  public void executePost(String serverPath, ArrayList<NameValuePair> nvpsList, final OnHttpWebListener onHttpWebListener) {
    HttpPost post = new HttpPost(serverPath);
    try {
      post.setEntity(new UrlEncodedFormEntity(nvpsList, HTTP.UTF_8));
    } catch (UnsupportedEncodingException e) {
    }
    // set this to avoid 417 error (Expectation Failed)
    post.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
    try {
      mHttpClient.execute(post, new ResponseHandler<String>() {

        @Override
        public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
          HttpEntity entity = response.getEntity();
          InputStream content = null;
          StringBuilder buffer = null;
          try {
            content = entity.getContent();

            InputStreamReader streamReader = new InputStreamReader(content, "UTF-8");
            buffer = new StringBuilder();
            try {
              char[] tmp = new char[1024];
              int l;
              while ((l = streamReader.read(tmp)) != -1) {
                buffer.append(tmp, 0, l);
              }
            } finally {
              streamReader.close();
            }
            content.close();
          } catch (IllegalStateException e1) {
          } catch (IOException e1) {
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
    } catch (IOException e) {
    }

  }

  public void executeMultipartPost(String serverPath, String filePath, boolean compress) throws Exception {

    Bitmap bitmapOrg = null;
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try {
      bitmapOrg = BitmapFactory.decodeFile(filePath);

      File imageFile = new File(filePath);
      ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
      int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

      int rotate = 0;
      int newRotate = 0;
      switch (orientation) {
      case ExifInterface.ORIENTATION_ROTATE_270:
        rotate = 270;
        newRotate = 90;
        break;
      case ExifInterface.ORIENTATION_ROTATE_180:
        rotate = 180;
        newRotate = 180;
        break;
      case ExifInterface.ORIENTATION_ROTATE_90:
        rotate = 90;
        newRotate = 90;
        break;
      }

      int width = bitmapOrg.getWidth();
      int height = bitmapOrg.getHeight();

      if (compress) {
        int newWidth = 500;
        int newHeight = 500;
        if (width > newWidth || height > newHeight) {
          // calculate the scale - in this case = 0.4f
          float scale = 0f;
          if (width > newWidth) {
            scale = ((float) newWidth) / width;
          } else if (height > newHeight) {
            scale = ((float) newHeight) / height;
          }

          // createa matrix for the manipulation
          Matrix matrix = new Matrix();
          // resize the bit map
          matrix.postScale(scale, scale);
          // rotate the Bitmap
          if (rotate > 0) {
            matrix.postRotate(newRotate);
          }

          // recreate the new Bitmap
          Bitmap resizedBitmap = Bitmap.createBitmap(bitmapOrg, 0, 0, width, height, matrix, true);

          resizedBitmap.compress(CompressFormat.JPEG, 75, bos);
        } else {
          if (rotate > 0) {
            // createa matrix for the manipulation
            Matrix matrix = new Matrix();
            // rotate the Bitmap
            if (rotate > 0) {
              matrix.postRotate(newRotate);
            }
            // recreate the new Bitmap
            Bitmap resizedBitmap = Bitmap.createBitmap(bitmapOrg, 0, 0, width, height, matrix, true);
            resizedBitmap.compress(CompressFormat.JPEG, 75, bos);
          } else {
            bitmapOrg.compress(CompressFormat.JPEG, 75, bos);
          }
        }
      } else {
        if (rotate > 0) {
          // createa matrix for the manipulation
          Matrix matrix = new Matrix();
          // rotate the Bitmap
          if (rotate > 0) {
            matrix.postRotate(newRotate);
          }
          // recreate the new Bitmap
          Bitmap resizedBitmap = Bitmap.createBitmap(bitmapOrg, 0, 0, width, height, matrix, true);
          resizedBitmap.compress(CompressFormat.JPEG, 75, bos);
        } else {
          bitmapOrg.compress(CompressFormat.JPEG, 75, bos);
        }
      }
    } catch (Exception e) {
      // Log.e(e.getClass().getName(), e.getMessage());
    }

    String[] fileNames = filePath.split("/");

    String fileName = fileNames[fileNames.length - 1];

    try {

      byte[] data = bos.toByteArray();
      HttpPost postRequest = new HttpPost(serverPath);
      ByteArrayBody bab = new ByteArrayBody(data, fileName);
      MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
      reqEntity.addPart("file_upload", bab);
      reqEntity.addPart("photoCaption", new StringBody("sfsdfsdf"));
      postRequest.setEntity(reqEntity);
      HttpResponse response = mHttpClient.execute(postRequest);
      BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
      String sResponse;
      StringBuilder s = new StringBuilder();

      while ((sResponse = reader.readLine()) != null) {
        s = s.append(sResponse);
      }
      System.out.println("Response: " + s);
    } catch (Exception e) {
    }
  }

}
