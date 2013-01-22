package com.bomzaiya.internet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpProtocolParams;

public class HttpDownloader {

	public void setSSL(String domain, String username, String password) {
		// self-signed certificate
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));
		schemeRegistry.register(new Scheme("https", new EasySSLSocketFactory(),
				443));

		BasicHttpParams params = new BasicHttpParams();
		params.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 1);
		params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE,
				new ConnPerRouteBean(1));
		params.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);

		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, "UTF-8");
		HttpProtocolParams.setHttpElementCharset(params, "UTF-8");

		CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

		credentialsProvider.setCredentials(new AuthScope(domain,
				AuthScope.ANY_PORT), new UsernamePasswordCredentials(username,
				password));

		try {
			HttpsURLConnection.setDefaultSSLSocketFactory(EasySSLSocketFactory
					.createEasySSLContext().getSocketFactory());
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Create all-trusting host name verifier
		HostnameVerifier allHostsValid = new HostnameVerifier() {
			@Override
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		};
		// Install the all-trusting host verifier
		HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

	}

	public boolean downloadFile(final String urlstring, final String filename,
			final OnHttpDownloadListener onHttpDownloadListener) {

		URL url = null;
		try {
			url = new URL(urlstring);
		} catch (MalformedURLException e) {
		}

		boolean https = false;
		if (urlstring.indexOf("https") != -1) {
			https = true;
		}

		HttpURLConnection httpConnection = null;
		HttpsURLConnection httpsConnection = null;
		try {
			if (https) {
				httpsConnection = (HttpsURLConnection) url.openConnection();
			} else {
				httpConnection = (HttpURLConnection) url.openConnection();
			}
		} catch (IOException e) {
		}

		File file = new File(filename);
		int downloaded = 0;
		if (file.exists()) {
			downloaded = (int) file.length();
			if (https) {
				httpsConnection.setRequestProperty("Range",
						"bytes=" + (file.length()) + "-");

			} else {
				httpConnection.setRequestProperty("Range",
						"bytes=" + (file.length()) + "-");
			}
		}
		if (https) {
			httpsConnection.setDoInput(true);
			httpsConnection.setDoOutput(true);
		} else {
			httpConnection.setDoInput(true);
			httpConnection.setDoOutput(true);
		}

		int x = 0;
		try {
			BufferedInputStream in = null;

			try {
				if (https) {
					InputStream is = httpsConnection.getInputStream();
					in = new BufferedInputStream(is);
				} else {
					in = new BufferedInputStream(
							httpConnection.getInputStream());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (in != null) {
				FileOutputStream fos = null;
				try {
					if (downloaded == 0) {
						fos = new FileOutputStream(filename);
					} else {
						fos = new FileOutputStream(filename, true);
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				try {
					BufferedOutputStream bout = new BufferedOutputStream(fos,
							1024);
					byte[] data = new byte[1024];
					while ((x = in.read(data, 0, 1024)) >= 0) {
						bout.write(data, 0, x);
						downloaded += x;
						onHttpDownloadListener.onDownloading(urlstring,
								filename, downloaded);
					}
					// must close bout otherwise it won't write file properly
					bout.close();
					fos.close();

					onHttpDownloadListener.onDownloaded(filename, downloaded);
				} catch (NullPointerException e) {

				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return false;
	}
}
