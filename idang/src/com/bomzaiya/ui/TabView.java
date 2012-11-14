package com.bomzaiya.ui;

import android.view.View;

public class TabView {
	private View sView = null;
	private String sTabTitle = "";
	private int mId = 0;
	private int mTabId = 0;
	private String sTabParentId = "";
	private IdangFragment mFragment;
	private String mImage = "";

	public void setImage(String image){
		mImage = image;
	}
	
	public String getImage(){
		return mImage;
	}
	
	public void setView(View v) {
		sView = v;
	}

	public void setTabTitle(String title) {
		sTabTitle = title;
	}

	public View getView() {
		return sView;
	}

	public String getTabTitle() {
		return sTabTitle;
	}
	
	public void setTabId(int id){
		mTabId = id;
	}
	
	public int getTabId(){
		return mTabId;
	}
	
	public void setTabParentId(String parentId){
		sTabParentId = parentId;
	}
	
	public String getTabParentId(){
		return sTabParentId;
	}
	
	public void setId(int Id){
		mId = Id;
	}
	
	public int getId(){
		return mId;
	}
	
	public void setFingiFragment(IdangFragment fragment) {
		mFragment = fragment;
	}
	
	public IdangFragment getFingiFragment() {
		return mFragment;
	}
}
