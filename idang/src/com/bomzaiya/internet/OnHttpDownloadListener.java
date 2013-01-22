package com.bomzaiya.internet;

//@SuppressLint("ParserError")
public interface OnHttpDownloadListener {
	public final String OK = "ok";
	public final String FAIL = "fail";
	
	public abstract void onDownloading(String url, String filename, int progress);
	public abstract void onDownloaded(String filename, int filesize);
}
