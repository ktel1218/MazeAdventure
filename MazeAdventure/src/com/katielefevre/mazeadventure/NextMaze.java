package com.katielefevre.mazeadventure;

import com.katielefevre.mazeadventure.R;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager.LayoutParams;

public class NextMaze extends Activity{

	
	MediaPlayer ourSound;
	
	@Override
	protected void onCreate(Bundle SavedInstanceState)
	{
		//TODO
		requestWindowFeature(Window.FEATURE_NO_TITLE); //hide title bar
		//set app to full screen and keep screen on 
		getWindow().setFlags(0xFFFFFFFF,
				LayoutParams.FLAG_FULLSCREEN|LayoutParams.FLAG_KEEP_SCREEN_ON);
		super.onCreate(SavedInstanceState);
		setContentView(R.layout.next);
		
		/*//save sound
		ourSound = MediaPlayer.create(NextMaze.this, R.raw.gong);
		
		SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		boolean musicPlay = getPrefs.getBoolean("checkbox", true);
		//make sound
		if (musicPlay)
		{
		
		ourSound.start();
		}*/
		
		
		//initialize timer thread for splash screen
		Thread timer = new Thread()
		{
			public void run()
			{
				try
				{
					sleep(2000);
				}
				catch(InterruptedException e)
				{
					e.printStackTrace();
				}
				finally
				{
					Intent openNewMaze = new Intent("com.example.mazeadventure.GAME");
					startActivity(openNewMaze);
				}
			}
		};
		
		//start timer
		timer.start();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		//ourSound.release();
		finish();
	}
	
}
