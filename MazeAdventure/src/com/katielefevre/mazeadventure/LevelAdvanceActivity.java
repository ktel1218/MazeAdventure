package com.katielefevre.mazeadventure;

import java.util.Timer;
import java.util.TimerTask;

import com.katielefevre.mazeadventure.R;

//import android.media.MediaPlayer;
import android.os.Bundle;

public class LevelAdvanceActivity extends BaseActivity{

	/*	private void playGong() {
		//save sound
		 * 	MediaPlayer ourSound;
		ourSound = MediaPlayer.create(NextMaze.this, R.raw.gong);
		
		SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		boolean musicPlay = getPrefs.getBoolean("checkbox", true);
		//make sound
		if (musicPlay)
		{
		
		ourSound.start();
		}
	}
	*/
	
	public static final int LEVEL_ADVANCE_REQUEST = 1;
  
	private void levelUp() {
		GameSettings.getInstance(this).incrementLevel();
	}
	
	private void close() {
		finish();
	}
	
	private void startTimer() {
		//initialize timer for splash screen
		final Timer timer = new Timer();
		TimerTask task = new TimerTask()
		{
			public void run()
			{
				timer.cancel();
				close();
			}
		};
		
		timer.schedule(task, 2000);
	}
	
	@Override
	protected void onCreate(Bundle SavedInstanceState)
	{
		super.onCreate(SavedInstanceState);
		setContentView(R.layout.next);
		
		levelUp();
		startTimer();
	}	
}
