package com.katielefevre.mazeadventure;

import com.katielefevre.mazeadventure.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class MainActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
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
