package com.katielefevre.mazeadventure;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.VideoView;

//import android.media.MediaPlayer;

public class LevelAdvanceActivity extends BaseActivity{
	
	VideoView videoView;


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
	
	@Override
	protected void onCreate(Bundle SavedInstanceState)
	{
		super.onCreate(SavedInstanceState);
		setContentView(R.layout.next);
		
		levelUp();
		
		videoView = (VideoView)findViewById(R.id.videoView);   
		videoView.setMediaController(null);
	    
	    Uri uri = Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.go_deeper);
	    videoView.setVideoURI(uri);
	    
	    videoView.start();  
	    
	 // video finish listener
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
            	//Close activity at the end of the video
            	close();
            }
        });		  
	}	
}
