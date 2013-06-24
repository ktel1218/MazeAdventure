package com.katielefevre.mazeadventure;

import com.katielefevre.mazeadventure.R;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;

public class LevelAdvanceActivity extends BaseActivity{
	MediaPlayer ourSound;
	
	@Override
	protected void onCreate(Bundle SavedInstanceState)
	{
		super.onCreate(SavedInstanceState);
		setContentView(R.layout.next);
		
		GameSettings.getInstance(this).incrementLevel();
		
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
					Intent maze = new Intent("com.katielefevre.mazeadventure.GAME");
					startActivity(maze);
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
