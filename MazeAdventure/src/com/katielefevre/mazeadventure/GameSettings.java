package com.katielefevre.mazeadventure;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
//import android.content.res.Resources;
import android.preference.PreferenceManager;

public class GameSettings {
	private final int LEVEL_ZERO = 0;
	
	private static GameSettings Instance = null;
	private SharedPreferences mSharedPrefs;
	//private Resources mResources;
	
	private GameSettings() {}
	
	public static GameSettings getInstance(Context context) {
		if (Instance == null) {
			Instance = new GameSettings();
			
			Context appContext = context.getApplicationContext();
	    //PreferenceManager.setDefaultValues(appContext, R.xml.settings, false); 
	    Instance.mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(appContext);
	    //Instance.mResources = appContext.getResources();
		}
		return Instance;
	}
	
	public Boolean isResumable() {
		return (getLevel() != LEVEL_ZERO); 
	}
	public void resetNew(){
		setLevel(LEVEL_ZERO);
	}
	public int getLevel() {
		return Instance.mSharedPrefs.getInt("level", LEVEL_ZERO);
	}
	public void setLevel(int level) {
	    Editor e = mSharedPrefs.edit();
	    e.putInt("level", level);
	    e.commit();		
	}
	public void incrementLevel() {
		setLevel(getLevel() + 1);
	}
	
	public int BLOCK() {
		return (60 + getLevel() * 2);
	}
}
