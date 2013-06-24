package com.katielefevre.mazeadventure;

import com.katielefevre.mazeadventure.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	  //hide title bar
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		//set app to full screen and keep screen on 
		getWindow().setFlags(0xFFFFFFFF, LayoutParams.FLAG_FULLSCREEN);
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Button next = (Button) findViewById(R.id.NEW_GAME);
		next.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				GameSettings.getInstance(getApplicationContext()).resetNew();
				startGame();
			}
		});
		
		Button resume = (Button) findViewById(R.id.RESUME_GAME);
		
		int visibility = settings().isResumable() ? View.VISIBLE : View.GONE;
		resume.setVisibility(visibility);
		
		resume.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startGame();
			}
		});
		
	}

	private GameSettings settings() {
		return GameSettings.getInstance(this);
	}
	
	private void startGame() {
		Intent startGame = new Intent(MainActivity.this, GameActivity.class);
		startActivityForResult(startGame, 1);		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
