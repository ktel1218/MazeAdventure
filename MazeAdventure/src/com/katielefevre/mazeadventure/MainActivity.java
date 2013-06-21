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

	//public static boolean newGame = true;
	//public static boolean level_complete = false;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE); //hide title bar
		//set app to full screen and keep screen on 
		getWindow().setFlags(0xFFFFFFFF,
				LayoutParams.FLAG_FULLSCREEN);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Button next = (Button) findViewById(R.id.NEW_GAME);
		next.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				
				Intent startGame = new Intent(MainActivity.this, GameActivity.class);
				startGame.putExtra("blockSize", 120);
				startActivityForResult(startGame, 1);
			}
		});
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
