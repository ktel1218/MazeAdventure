package com.katielefevre.mazeadventure;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager.LayoutParams;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public abstract class BaseActivity extends Activity {

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//hide title bar
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		
		//set application to full screen and keep screen on 
		getWindow().setFlags(0xFFFFFFFF,
				LayoutParams.FLAG_FULLSCREEN|LayoutParams.FLAG_KEEP_SCREEN_ON);
	}
}
