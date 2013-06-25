package com.katielefevre.mazeadventure;

import java.util.Timer;
import java.util.TimerTask;

import com.katielefevre.mazeadventure.R;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

public class GameActivity extends BaseActivity 
{    
	private MazeBallView mBallView = null;
	private Handler RedrawHandler = new Handler();
	
	private Timer mTmr = null;
	private TimerTask mTsk = null;

	boolean mFirstCellExited = false;

	private int mScrWidth, mScrHeight;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);
		
		getWindow().getDecorView().setBackgroundColor(Color.BLACK);
		final FrameLayout mainView = 
				(android.widget.FrameLayout)findViewById(R.id.main_view);
		
		//get screen dimensions
		Display display = getWindowManager().getDefaultDisplay();  

		int BLOCK = GameSettings.getInstance(this).BLOCK();
		
		Point size = new Point();
		display.getSize(size);
		
		mScrWidth = size.x;
		mScrHeight = size.y;
		
		int MWIDTH = mScrWidth/BLOCK;
		mainView.setX((mScrWidth-(MWIDTH*BLOCK))/4);
		
		final Maze theMaze = Maze.BuildMaze(mScrWidth, mScrHeight, BLOCK);
		MazeView maze_view = new MazeView(this);
		maze_view.init(theMaze);
		mainView.addView(maze_view);

		mBallView = new MazeBallView(this);
		mBallView.init(theMaze.Ball);
		mainView.addView(mBallView); 	//add ball to main screen
		
		mBallView.invalidate(); 		//call onDraw in BallView

		/*//create timer
		mTimerView = new TimerView(this);
		mainView.addView(mTimerView); //add timer to main screen
		mTimerView.invalidate(); //call onDraw in TimerView
		 */

		//listener for accelerometer, use anonymous class for simplicity
		((SensorManager)getSystemService(Context.SENSOR_SERVICE)).registerListener(
				new SensorEventListener() 
				{    
					@Override  
					public void onSensorChanged(SensorEvent event) {  
						//set ball speed based on phone tilt (ignore Z axis)
						theMaze.Ball.getBallSpd().x = -event.values[0];
						theMaze.Ball.getBallSpd().y = event.values[1];
						//timer event will redraw ball
					}
					
					@Override  
					public void onAccuracyChanged(Sensor sensor, int accuracy) { /* ignore */ } 
				},
				((SensorManager)getSystemService(Context.SENSOR_SERVICE))
				               .getSensorList(Sensor.TYPE_ACCELEROMETER).get(0),   
				                              SensorManager.SENSOR_DELAY_NORMAL);
		
		//listener for touch event 
		mainView.setOnTouchListener(
				new android.view.View.OnTouchListener() 
				{
					@Override
					public boolean onTouch(android.view.View v, android.view.MotionEvent e) {
						return true;
					}
				}); 
	} //OnCreate
	

	//listener for menu button on phone
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		menu.add("Exit"); //only one menu item
		return super.onCreateOptionsMenu(menu);

	}
	//listener for menu item clicked
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		// Handle item selection    
		if (item.getTitle() == "Exit") //user clicked Exit
			//"I'll just rest here... for a moment."
			//finishActivity(0); //will call onPause
			finish();
		return super.onOptionsItemSelected(item);    
	}
	
	//For state flow see http://developer.android.com/reference/android/app/Activity.html
	@Override
	public void onPause() 
	{
		super.onPause();
		
		//application moved to background, 
		// stop background threads
		
		//kill & release timer 
		// (our only background thread)
		mTmr.cancel(); 
		mTmr = null;
		mTsk = null;
		
		finish();
	}

	@Override
	public void onResume() 
	{
		super.onResume();
		
		//application moved to foreground 
		// (also occurs at application startup)
		//
		// create timer to move ball to new position
		//
		mTmr = new Timer(); 
		mTsk = new TimerTask() {
			@Override
			public void run() {

				int BLOCK = Maze.BLOCK;
				int wallWidth = Maze.WALL_WIDTH;
				Maze.Cell[][] maze = Maze.getInstance().cells;
				
				//if debugging with external device, 
				//  a log cat viewer will be needed on the device
				//android.util.Log.d("TiltBall","Timer Hit - " + mBallPos.x + ":" + mBallPos.y);

				PointF mBallPos = Maze.getInstance().Ball.getBallPos();
				PointF mBallSpd = Maze.getInstance().Ball.getBallSpd();
				float mRadius = Maze.getInstance().Ball.getRadius();
				
				//move ball based on current speed
				mBallPos.x += mBallSpd.x*1.1;
				mBallPos.y += mBallSpd.y*1.1;

				//get the balls square/maze cell
				int currentCellX =(int)(mBallPos.x) / BLOCK;
				int currentCellY =(int)(mBallPos.y) / BLOCK;

				//COLLISION DETECTION

				//TRY
				try{

					//is there a top wall?
					if (maze[currentCellX][currentCellY].walls[Maze.Wall.NORTH])
					{
						//don't go over top wall

						if (mBallPos.y-mRadius < currentCellY*BLOCK+wallWidth)
						{
							mBallPos.y = currentCellY*BLOCK+wallWidth+mRadius;
							//System.out.println("top collision detected");
						}
					}
					
					//is there a bottom wall?
					if (maze[currentCellX][currentCellY].walls[Maze.Wall.SOUTH])
					{
						//don't move down
						if (mBallPos.y+mRadius >= currentCellY*BLOCK+BLOCK)
						{
							mBallPos.y = currentCellY*BLOCK+BLOCK-mRadius;
							//System.out.println("bottom collision detected");
						}
					}

					//is there a left wall?
					if (maze[currentCellX][currentCellY].walls[Maze.Wall.WEST])
					{
						//don't go over left wall
						if (mBallPos.x-mRadius < currentCellX*BLOCK+wallWidth)
						{
							mBallPos.x = currentCellX*BLOCK+wallWidth+mRadius;
							//System.out.println("left collision detected");
						}
					}

					//is there a right wall?
					if (maze[currentCellX][currentCellY].walls[Maze.Wall.EAST])
					{
						//don't move right
						if (mBallPos.x+mRadius >= currentCellX*BLOCK+BLOCK)
						{
							mBallPos.x = currentCellX*BLOCK+BLOCK-mRadius;
							//System.out.println("right collision detected");
						}

					}

					//is there neither top nor left wall? AND is there a top left overlap?
					//does the top cell have a left wall OR does the left cell have a top wall?
					if((!maze[currentCellX][currentCellY].walls[Maze.Wall.NORTH]) && (!maze[currentCellX][currentCellY].walls[Maze.Wall.WEST])
							&&(maze[currentCellX][currentCellY-1].walls[Maze.Wall.WEST] || maze[currentCellX-1][currentCellY].walls[Maze.Wall.NORTH]))
					{
						if (mBallPos.x>((currentCellX*BLOCK)+wallWidth) && mBallPos.y<((currentCellY*BLOCK)+wallWidth))//y simple collision
						{
							if (mBallPos.x-mRadius<currentCellX*BLOCK+wallWidth)
							{
								mBallPos.x=currentCellX*BLOCK+wallWidth+mRadius;
							}
						}
						else if(mBallPos.y>((currentCellY*BLOCK)+wallWidth) && mBallPos.y-mRadius < ((currentCellY*BLOCK)+wallWidth))
						{
							double y = Math.sqrt((Math.pow(mRadius, 2) - Math.pow((mBallPos.x-((currentCellX*BLOCK)+wallWidth)),2)));
							if (mBallPos.y-y<currentCellY*BLOCK+wallWidth)
							{
								mBallPos.y=(float) ((currentCellY*BLOCK+wallWidth)+y);
							}
						}

						if (mBallPos.y>((currentCellY*BLOCK)+wallWidth) && mBallPos.x<((currentCellX*BLOCK)+wallWidth))//x simple collision
						{
							if (mBallPos.y-mRadius<currentCellY*BLOCK+wallWidth)
							{
								mBallPos.y=currentCellY*BLOCK+wallWidth+mRadius;
							}
						}

						else if (mBallPos.x>((currentCellX*BLOCK)+wallWidth) && mBallPos.x-mRadius < ((currentCellX*BLOCK)+wallWidth))
						{
							double x = Math.sqrt((Math.pow(mRadius, 2) - Math.pow((mBallPos.y-((currentCellY*BLOCK)+wallWidth)),2)));
							if (mBallPos.x-x<(currentCellX*BLOCK)+wallWidth)
							{
								mBallPos.x=(float) ((currentCellX*BLOCK+wallWidth)+x);
							}
						}
					}

					//is there neither a top nor right wall? AND is there a top right overlap?
					//does the top right cell have a left wall OR does the right cell have a top wall?
					if((!maze[currentCellX][currentCellY].walls[Maze.Wall.NORTH] && !maze[currentCellX][currentCellY].walls[Maze.Wall.EAST]) 
							&& (maze[currentCellX+1][currentCellY-1].walls[Maze.Wall.WEST] || maze[currentCellX+1][currentCellY].walls[Maze.Wall.NORTH]))
					{
						if (mBallPos.y<currentCellY*BLOCK+wallWidth)
						{
							if (mBallPos.x+mRadius>(currentCellX+1)*BLOCK)
							{
								mBallPos.x = (currentCellX+1)*BLOCK-mRadius;
							}
						}
						else if (mBallPos.y>currentCellY*BLOCK+wallWidth && mBallPos.y-mRadius<currentCellY*BLOCK+wallWidth)
						{
							double x = Math.sqrt(Math.pow(mRadius, 2) - Math.pow(((currentCellY*BLOCK+wallWidth)-mBallPos.y),2));
							if ((mBallPos.x+x)>((currentCellX+1)*BLOCK))
							{
								mBallPos.x=(float) (((currentCellX+1)*BLOCK)-x);
							}
						}
					}

					//is there neither a bottom nor left wall? AND is there a bottom left overlap?
					//does the bottom left cell have a top wall OR does the bottom cell have a left wall?
					if((!maze[currentCellX][currentCellY].walls[Maze.Wall.SOUTH]) && (!maze[currentCellX][currentCellY].walls[Maze.Wall.WEST])
							&& (maze[currentCellX][currentCellY+1].walls[Maze.Wall.WEST] || maze[currentCellX-1][currentCellY+1].walls[Maze.Wall.NORTH]))
					{
						if (mBallPos.x<(currentCellX*BLOCK)+wallWidth)
						{
							if (mBallPos.y+mRadius>(currentCellY+1)*BLOCK)
							{
								mBallPos.y = (currentCellY+1)*BLOCK-mRadius;
							}
						}
						else if (mBallPos.x>currentCellX*BLOCK+wallWidth && mBallPos.x-mRadius<currentCellX*BLOCK+wallWidth)
						{
							double y = Math.sqrt(Math.pow(mRadius, 2) - Math.pow(((currentCellX*BLOCK+wallWidth)-mBallPos.x),2));
							if ((mBallPos.y+y)>((currentCellY+1)*BLOCK))
							{
								mBallPos.y=(float) (((currentCellY+1)*BLOCK)-y);
							}
						}
					}

					//is there neither a right nor bottom wall? AND is there a bottom right overlap?
					//does the bottom right cell have a top wall OR a left wall?
					if((!maze[currentCellX][currentCellY].walls[Maze.Wall.EAST]) && (!maze[currentCellX][currentCellY].walls[Maze.Wall.SOUTH])
							&&  (maze[currentCellX+1][currentCellY+1].walls[Maze.Wall.WEST] 
							|| maze[currentCellX+1][currentCellY+1].walls[Maze.Wall.NORTH] ))
					{
						if (mBallPos.y+mRadius>(currentCellY+1)*BLOCK)//collide with x
						{
							double x = Math.sqrt(Math.pow(mRadius, 2) - Math.pow((((currentCellY+1)*BLOCK)-mBallPos.y),2));
							if ((mBallPos.x+x)>((currentCellX+1)*BLOCK))
							{
								mBallPos.x=(float) (((currentCellX+1)*BLOCK)-x);
							}
						}
						else if (mBallPos.x+mRadius>(currentCellX+1)*BLOCK)//collide with y
						{
							double y = Math.sqrt(Math.pow(mRadius, 2) - Math.pow((((currentCellX+1)*BLOCK)-mBallPos.x),2));
							if ((mBallPos.y+y)>((currentCellY+1)*BLOCK))
							{
								mBallPos.y=(float) (((currentCellY+1)*BLOCK)-y);
							}
						}
					}

				}
				catch(ArrayIndexOutOfBoundsException e) {
					
				}

				//CATCH, Array out of bounds
				///////

				if (currentCellX == 0 && currentCellY == 0)
				{				
					if (mBallPos.x-mRadius <= wallWidth)
					{
						if (mFirstCellExited)
						{
							RedrawHandler.post(new Runnable()
							{
								public void run()
								{
									Toast.makeText(getApplicationContext(), "Can't go back, they will find you. Must go deeper...", Toast.LENGTH_SHORT).show();
								}
							});
							mFirstCellExited = false;
						}
					}
				}

				if (currentCellX != 0 || currentCellY != 0)
				{				
					mFirstCellExited = true;
				}

				Maze.Cell cell = Maze.getInstance().getExitCell();
				
				if (currentCellX == cell.x && currentCellY == cell.y)
				{
					//Next Level
					Intent LevelAdvance = new Intent(GameActivity.this, LevelAdvanceActivity.class);
					startActivity(LevelAdvance);
				}
				
				//collision with sides of screen in case of no wall glitch
				if ((mBallPos.x + mRadius) > mScrWidth) mBallPos.x=mScrWidth-mRadius;
				if ((mBallPos.y + mRadius) > mScrHeight) mBallPos.y=mScrHeight-mRadius;
				if ((mBallPos.x - mRadius) < 0) mBallPos.x=0+mRadius;
				if ((mBallPos.y - mRadius) < 0) mBallPos.y=0+mRadius;

				//END OF COLLISION DETECTION

				//redraw ball. Must run in background 
			    //  thread to prevent thread lock.
				RedrawHandler.post(new Runnable() 
				{
					@Override
					public void run() {    
						mBallView.invalidate();
					}
				});

			}}; // TimerTask

			mTmr.schedule(mTsk, 10, 10); //start timer
	} // onResume

	//@Override
	/*public void onDestroy() //main thread stopped
	{
		super.onDestroy();
		//wait for threads to exit before clearing application
		System.runFinalizersOnExit(true); //ASK DR BLACK ABOUT ALTERNATIVE
		//remove application from memory
		android.os.Process.killProcess(android.os.Process.myPid());  
	}*/
	//listener for config change. 
	//This is called when user tilts phone enough to trigger landscape view
	//we want our app to stay in portrait view, so bypass event 


	//@Override 
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
	}
} 

//TiltBallActivity


