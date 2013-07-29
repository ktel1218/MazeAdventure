package com.katielefevre.mazeadventure;

import java.util.Timer;
import java.util.TimerTask;

import com.katielefevre.mazeadventure.R;

import android.content.Context;
import android.content.Intent;
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
import android.widget.TextView;
import android.widget.Toast;

public class GameActivity extends BaseActivity 
{ 
	public final static int MAZE_REQUEST = 1;
	public final static int MAZE_QUIT_RESULT = 1;
	
	private MazeBallView mBallView = null;
	private Handler RedrawHandler = new Handler();
	
	private Timer mTmr = null;
	boolean mFirstCellExited = false;
	private int mScrWidth, mScrHeight;
	
	private final static int FREQ_MSECS = 20;
	private final static float FREQ_SECS = FREQ_MSECS / 1000f;
	private final static double HALF_TSQUARED = Math.pow(FREQ_SECS, 2) / 2;
	private final static double REAL_WIDTH = 0.254; // 10" width game board
	private double SCALE_FACTOR = 0;
	
	private void LogSize() {
		if (BuildConfig.DEBUG) {
			StringBuilder sb = new StringBuilder();
			sb.append("H: ").append(mScrHeight).append(", W: ").append(mScrWidth);
			android.util.Log.d(getLocalClassName(), sb.toString());
		}
	}
	
	private void LogPointF(String header, PointF mBallAccel) {
		if (BuildConfig.DEBUG) {
			StringBuilder sb = new StringBuilder();
			sb.append(header).append(": x: ").append(mBallAccel.x).append(", y: ").append(mBallAccel.y);
			android.util.Log.d(getLocalClassName(), sb.toString());
		}				
	}
	
	private void setupScaling() {
		// get screen dimensions
		Display display = getWindowManager().getDefaultDisplay();  

		Point size = new Point();
		display.getSize(size);
		
		mScrWidth = size.x;
		mScrHeight = size.y;	
		
		SCALE_FACTOR = mScrWidth/REAL_WIDTH;
		
		LogSize();
	}
	
	private void setHeader() {
		StringBuilder sb = new StringBuilder();
		sb.append("Level [").append(GameSettings.getInstance(this).getLevel()).append("] ");
		sb.append("Block [").append(Maze.BLOCK).append("] ");
		
		TextView levelView = (TextView) findViewById(R.id.levelView);
		levelView.setText(sb.toString());		
	}
	
	private void exitMaze() {
		pauseMaze();
		setResult(MAZE_QUIT_RESULT);
		finish();
	}
	
	private void pauseMaze() {
		if (mTmr == null) return;
		mTmr.cancel(); 
		mTmr = null;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);
		
		getWindow().getDecorView().setBackgroundColor(Color.BLACK);
		
		setupScaling();
		createMaze();
	}
	
	private void startMaze() {
		createMaze();
	}
	
	private void createMaze() {
		
		int BLOCK = GameSettings.getInstance(this).BLOCK();
		
		final FrameLayout mainView = 
				(android.widget.FrameLayout)findViewById(R.id.main_view);
		
		//TODO: Ask Katie about this line
		//mainView.setX((mScrWidth-(mScrWidth/BLOCK)*BLOCK))/4);
		
		mFirstCellExited = false;
		
		final Maze theMaze = Maze.BuildMaze(mScrWidth, mScrHeight, BLOCK);
		MazeView maze_view = new MazeView(this);
		maze_view.init(theMaze);
		mainView.addView(maze_view);

		mBallView = new MazeBallView(this);
		mBallView.init(theMaze.Ball);
		mBallView.invalidate();
		
		mainView.addView(mBallView);
	
		//listener for accelerometer, use anonymous class for simplicity
		((SensorManager)getSystemService(Context.SENSOR_SERVICE)).registerListener(
				new SensorEventListener() 
				{    
					@Override  
					public void onSensorChanged(SensorEvent event) {  
						//set ball acceleration based on phone tilt (ignore Z axis)
						theMaze.Ball.getBallAccel().x = event.values[0];
						theMaze.Ball.getBallAccel().y = event.values[1];
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
						PointF mBallAccel = Maze.getInstance().Ball.getBallAccel();
						PointF mBallPos = Maze.getInstance().Ball.getBallPos();
						PointF mBallSpd = Maze.getInstance().Ball.getBallSpd();
						LogPointF("Position", mBallPos);
						LogPointF("Acceleration", mBallAccel);
						LogPointF("Speed", mBallSpd);
						return true;
					}
				}); 
	}
	
	//
	// application moved to foreground 
	// (also occurs at application startup)
	private void resumeMaze() {
    //
		// create timer to move ball to new position
		
		mTmr = new Timer(); 
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				int BLOCK = Maze.BLOCK;
				int wallWidth = Maze.WALL_WIDTH;
				Maze.Cell[][] maze = Maze.getInstance().cells;
				
				//if debugging with external device, 
				//  a log cat viewer will be needed on the device
				
				PointF mBallAccel = Maze.getInstance().Ball.getBallAccel();
				PointF mBallPos = Maze.getInstance().Ball.getBallPos();
				PointF mBallSpd = Maze.getInstance().Ball.getBallSpd();
				float mRadius = Maze.getInstance().Ball.getRadius();
				
				mBallSpd.x += mBallAccel.x * FREQ_SECS;
				mBallSpd.y += mBallAccel.y * FREQ_SECS;
				
				mBallPos.x -= (mBallSpd.x * FREQ_SECS - mBallAccel.x * HALF_TSQUARED) * SCALE_FACTOR;
				mBallPos.y += (mBallSpd.y * FREQ_SECS - mBallAccel.y * HALF_TSQUARED) * SCALE_FACTOR; 
				
				//get the balls square/maze cell
				int currentCellX =(int)(mBallPos.x) / BLOCK;
				int currentCellY =(int)(mBallPos.y) / BLOCK;

				//COLLISION DETECTION
				try{

					//is there a top wall?
					if (maze[currentCellX][currentCellY].walls[Maze.Wall.NORTH])
					{
						//don't go over top wall
						if (mBallPos.y-mRadius < currentCellY*BLOCK+wallWidth)
						{
							mBallPos.y = currentCellY*BLOCK+wallWidth+mRadius;
							mBallSpd.y = 0f;
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
							mBallSpd.y = 0f;
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
							mBallSpd.x = 0f;
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
							mBallSpd.x = 0f;
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
								mBallSpd.x = 0f;
							}
						}
						else if(mBallPos.y>((currentCellY*BLOCK)+wallWidth) && mBallPos.y-mRadius < ((currentCellY*BLOCK)+wallWidth))
						{
							double y = Math.sqrt((Math.pow(mRadius, 2) - Math.pow((mBallPos.x-((currentCellX*BLOCK)+wallWidth)),2)));
							if (mBallPos.y-y<currentCellY*BLOCK+wallWidth)
							{
								mBallPos.y=(float) ((currentCellY*BLOCK+wallWidth)+y);
								mBallSpd.y = 0f;
							}
						}

						if (mBallPos.y>((currentCellY*BLOCK)+wallWidth) && mBallPos.x<((currentCellX*BLOCK)+wallWidth))//x simple collision
						{
							if (mBallPos.y-mRadius<currentCellY*BLOCK+wallWidth)
							{
								mBallPos.y=currentCellY*BLOCK+wallWidth+mRadius;
								mBallSpd.y = 0f;
							}
						}

						else if (mBallPos.x>((currentCellX*BLOCK)+wallWidth) && mBallPos.x-mRadius < ((currentCellX*BLOCK)+wallWidth))
						{
							double x = Math.sqrt((Math.pow(mRadius, 2) - Math.pow((mBallPos.y-((currentCellY*BLOCK)+wallWidth)),2)));
							if (mBallPos.x-x<(currentCellX*BLOCK)+wallWidth)
							{
								mBallPos.x=(float) ((currentCellX*BLOCK+wallWidth)+x);
								mBallSpd.x = 0f;
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
								mBallSpd.x = 0f;
							}
						}
						else if (mBallPos.y>currentCellY*BLOCK+wallWidth && mBallPos.y-mRadius<currentCellY*BLOCK+wallWidth)
						{
							double x = Math.sqrt(Math.pow(mRadius, 2) - Math.pow(((currentCellY*BLOCK+wallWidth)-mBallPos.y),2));
							if ((mBallPos.x+x)>((currentCellX+1)*BLOCK))
							{
								mBallPos.x=(float) (((currentCellX+1)*BLOCK)-x);
								mBallSpd.x = 0f;
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
								mBallSpd.y = 0f;
							}
						}
						else if (mBallPos.x>currentCellX*BLOCK+wallWidth && mBallPos.x-mRadius<currentCellX*BLOCK+wallWidth)
						{
							double y = Math.sqrt(Math.pow(mRadius, 2) - Math.pow(((currentCellX*BLOCK+wallWidth)-mBallPos.x),2));
							if ((mBallPos.y+y)>((currentCellY+1)*BLOCK))
							{
								mBallPos.y=(float) (((currentCellY+1)*BLOCK)-y);
								mBallSpd.y = 0f;
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
								mBallSpd.x = 0f;
							}
						}
						else if (mBallPos.x+mRadius>(currentCellX+1)*BLOCK)//collide with y
						{
							double y = Math.sqrt(Math.pow(mRadius, 2) - Math.pow((((currentCellX+1)*BLOCK)-mBallPos.x),2));
							if ((mBallPos.y+y)>((currentCellY+1)*BLOCK))
							{
								mBallPos.y=(float) (((currentCellY+1)*BLOCK)-y);
								mBallSpd.y = 0f;
							}
						}
					}

				}
				catch(ArrayIndexOutOfBoundsException e) 
				{
				
				}

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
					completeMaze();
				} 
				else 
				{
				
					//collision with sides of screen in case of no wall glitch
					if ((mBallPos.x + mRadius) > mScrWidth)
					{
						mBallPos.x=mScrWidth-mRadius;
						mBallSpd.x = 0f;
					}
					if ((mBallPos.y + mRadius) > mScrHeight)
					{
						mBallPos.y=mScrHeight-mRadius;
						mBallSpd.y = 0f;
					}
					if ((mBallPos.x - mRadius) < 0) 
					{
						mBallPos.x=0+mRadius;
						mBallSpd.x = 0f;
					}
					if ((mBallPos.y - mRadius) < 0)
					{
						mBallPos.y=0+mRadius;
						mBallSpd.y = 0f;
					}
	
					//END OF COLLISION DETECTION
	
					RedrawHandler.post(new Runnable() 
					{
						@Override
						public void run() { 
							setHeader();
							mBallView.invalidate();
						}
					});
				}

			}}; // TimerTask

			mTmr.schedule(task, 10, FREQ_MSECS); 
	}
	
	private void completeMaze() {
		pauseMaze();
		Intent levelAdvance = new Intent(GameActivity.this, LevelAdvanceActivity.class);
		startActivityForResult(levelAdvance, LevelAdvanceActivity.LEVEL_ADVANCE_REQUEST);
	}
	
	//listener for menu button on phone
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		super.onCreateOptionsMenu(menu);
		menu.add("Exit"); //only one menu item
		return true;
	}
	
	//listener for menu item clicked
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		super.onOptionsItemSelected(item);  
		if (item.getTitle() == "Exit") {
			exitMaze();
		}
		return true;    
	}
	
	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		//Only one return path right now
		startMaze();
	}
	
	@Override
	public void onPause() 
	{
		super.onPause();
		pauseMaze();
	}

	@Override
	public void onResume() 
	{
		super.onResume();
		resumeMaze();
	} 
} 


