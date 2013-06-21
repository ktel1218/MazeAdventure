package com.katielefevre.mazeadventure;

import com.katielefevre.mazeadventure.R;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.VideoView;

public class MinotaurCS extends Activity implements OnCompletionListener, OnErrorListener
{
	VideoView vv;
	String fileName;
	
	 @Override
	protected void onCreate(Bundle savedInstanceState) 
	 {
		 requestWindowFeature(Window.FEATURE_NO_TITLE); //hide title bar
			//set app to full screen and keep screen on 
			getWindow().setFlags(0xFFFFFFFF,
					LayoutParams.FLAG_FULLSCREEN|LayoutParams.FLAG_KEEP_SCREEN_ON);
			
		// TODO 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cutscene);
		
		//initialize
		
		vv = (VideoView)this.findViewById(R.id.videoView);	 
		fileName = "android.resource://" + getPackageName() + "/" + R.raw.test;
		
		vv.setVideoURI(Uri.parse(fileName));
		vv.start();

	}
	 
	@Override
	public void onCompletion(MediaPlayer mp) 
	{
		// TODO Register a callback to be invoked when the end of a media file has been reached during playback.
		finish();
		
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) 
	{
		// TODO Auto-generated method stub
		return false;
	}
		

}
