package com.katielefevre.mazeadventure;

import java.util.Timer;
import java.util.TimerTask;

import com.katielefevre.mazeadventure.R;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class GameActivity extends Activity 
{    
	BallView mBallView = null;
	//TimerView mTimerView = null;
	Handler RedrawHandler = new Handler();
	Timer mTmr = null;
	TimerTask mTsk = null;

	String toast_stringStart=" @";
	Handler mazeStartToast;

	String toast_stringFinish=" @";
	Handler mazeFinishToast;

	GameActivity main;
	FrameLayout mainFrame;

	long gameTimer = 0;

	static boolean level_complete = false;
	boolean first_cell_exited = false;


	static int mScrWidth, mScrHeight;
	android.graphics.PointF mBallPos, mBallSpd;

	public static int BLOCK = 120;

	public static int MWIDTH=8; //default to be reassigned as screenwidth/BLOCK
	public static int MHEIGHT=12; // same as above

	public static float radius = (float) (.25*BLOCK);// set ball radius as factor of BLOCK size for scaling purposes

	static Cell[][] maze;
	private MazeComponent mazeView;

	public static Paint mazePaint = new Paint(Color.BLACK);// wall color, doesn't do anything
	public static float wallWidth = (float) (.22*BLOCK);

	public static final int ADDED = 0, NORTH = 1, SOUTH = 2, WEST = 3, EAST = 4;

	public static Paint targetPaint1 = new Paint(Paint.ANTI_ALIAS_FLAG);// target color 1
	public static Paint targetPaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);// target color 2
	public static Paint backPaint = new Paint (Paint.ANTI_ALIAS_FLAG);// background color
	public static Paint backPaint2 = new Paint (Paint.ANTI_ALIAS_FLAG);// background color 2 for fade

	Intent openNextMaze;


	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	@Override
	public void onCreate(Bundle savedInstanceState) {
		//hide title bar
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		//set app to full screen and keep screen on 
		getWindow().setFlags(0xFFFFFFFF,
				LayoutParams.FLAG_FULLSCREEN|LayoutParams.FLAG_KEEP_SCREEN_ON);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);
		
		getWindow().getDecorView().setBackgroundColor(Color.BLACK);
		//create pointer to main screen
		final FrameLayout mainView = 
				(android.widget.FrameLayout)findViewById(R.id.main_view);

		mazeStartToast = new Handler(); 
		mazeFinishToast = new Handler(); 

		main = this;
		mainFrame = mainView;

		//get screen dimensions
		Display display = getWindowManager().getDefaultDisplay();  
		Point size = new Point();
		display.getSize(size);
		mScrWidth = size.x;
		mScrHeight = size.y;
		mBallPos = new android.graphics.PointF();
		mBallSpd = new android.graphics.PointF();
		MWIDTH = mScrWidth/BLOCK;
		MHEIGHT = mScrHeight/BLOCK;

		//create variables for ball position and speed
		//mBallPos.x = mScrWidth/2;  // STARTS BALL IN CENTER OF MAZE
		//mBallPos.y = mScrHeight/2; 
		mBallPos.x=0; //STARTS BALL IN UPPER LEFT OF MAZE
		mBallPos.y=0;
		mBallSpd.x = 0;
		mBallSpd.y = 0;

		maze=new Cell[MWIDTH][MHEIGHT];
		for (int i=0; i<MWIDTH; i++)
			for (int j=0; j<MHEIGHT; j++)
			{
				maze[i][j] = new Cell(i, j);
			}

		makeMaze(BLOCK, mainView);

		mainView.setX((mScrWidth-(MWIDTH*BLOCK))/4);

		//create maze view
		mazeView = new MazeComponent(this);
		//add it to main view
		mainView.addView(mazeView);

		//create initial ball
		mBallView = new BallView(this, mBallPos.x, mBallPos.y, radius);
		//add it to view
		mainView.addView(mBallView); //add ball to main screen
		mBallView.invalidate(); //call onDraw in BallView

		/*//create timer
		mTimerView = new TimerView(this);
		//add it to view
		mainView.addView(mTimerView); //add timer to main screen
		mTimerView.invalidate(); //call onDraw in TimerView
		 */

		//listener for accelerometer, use anonymous class for simplicity
		((SensorManager)getSystemService(Context.SENSOR_SERVICE)).registerListener(
				new SensorEventListener() {    
					@Override  
					public void onSensorChanged(SensorEvent event) {  
						//set ball speed based on phone tilt (ignore Z axis)
						mBallSpd.x = -event.values[0];
						mBallSpd.y = event.values[1];
						//timer event will redraw ball
					}
					@Override  
					public void onAccuracyChanged(Sensor sensor, int accuracy) {} //ignore
				},
				((SensorManager)getSystemService(Context.SENSOR_SERVICE))
				.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0),   
				SensorManager.SENSOR_DELAY_NORMAL);
		//listener for touch event 
		mainView.setOnTouchListener(new android.view.View.OnTouchListener() {
			@Override
			public boolean onTouch(android.view.View v, android.view.MotionEvent e) {
				//set ball position based on screen touch
				//mBallPos.x = e.getX();
				//mBallPos.y = e.getY();
				//timer event will redraw ball

				//PAUSE

				return true;
			}}); 
	}
	//OnCreate

	public class Cell 
	{ // TODO Cell
		int x, y;
		boolean[] walls = { false, true, true, true, true };

		public Cell(int X, int Y) {
			this.x = X;
			this.y = Y;
		}
		public String printCell()
		{
			return " " + this.x + " " + walls[0] +" " + walls[1] + " " + walls[2] + " " + walls[3] + " " + walls[4];
		}
	}

	public static void makeMaze(int block, FrameLayout mainView)
	{
		BLOCK = block;

		wallWidth = (float) (.22*BLOCK);
		radius = (float) (.25*BLOCK);
		MWIDTH = mScrWidth/BLOCK;
		MHEIGHT = mScrHeight/BLOCK;

		int[] blockListX = new int[MWIDTH*MHEIGHT];
		int[] blockListY = new int[MWIDTH*MHEIGHT];
		int blocks=0;
		int x,y;

		// Choose random starting block and add it to maze
		x = (int) (Math.random() * (MWIDTH - 2) + 1);
		y = (int) (Math.random() * (MHEIGHT - 2) + 1);
		maze[x][y].walls[ADDED] = true;// added to the maze

		//Add all adjacent blocks to blocklist
		if (x>0)
		{
			blockListX[blocks]=x-1;
			blockListY[blocks]=y;
			blocks++;
		}
		if (x<MWIDTH-1)
		{
			blockListX[blocks]=x+1;
			blockListY[blocks]=y;
			blocks++;
		}
		if (y>0)
		{
			blockListX[blocks]=x;
			blockListY[blocks]=y-1;
			blocks++;
		}
		if (y<MHEIGHT-1)
		{
			blockListX[blocks]=x;
			blockListY[blocks]=y+1;
			blocks++;
		}

		while (blocks>0)
		{
			//choose a random block from blocklist
			int b = (int) (Math.random() * blocks);
			x = blockListX[b];
			y = blockListY[b];
			int[] dir = new int[4];
			int numdir = 0;

			// find which block in the maze it is adjacent to
			// if cell exists, note it and it's direction

			// left
			if (x > 0 && maze[x - 1][y].walls[ADDED]) {
				dir[numdir++] = 0;
			}
			// right
			if (x < MWIDTH - 1 && maze[x + 1][y].walls[ADDED]) {
				dir[numdir++] = 1;
			}
			// up
			if (y > 0 && maze[x][y - 1].walls[ADDED]) {
				dir[numdir++] = 2;
			}
			// down
			if (y < MHEIGHT - 1 && maze[x][y + 1].walls[ADDED]) {
				dir[numdir++] = 3;
			}

			int d = (int)(Math.random()*numdir);
			d=dir[d];

			// And remove the wall between the two
			// left
			if (d == 0) {
				maze[x][y].walls[WEST] = false;
				maze[x - 1][y].walls[EAST] = false;
			}
			// right
			else if (d == 1) {
				maze[x][y].walls[EAST] = false;
				maze[x + 1][y].walls[WEST] = false;
			}
			// up
			else if (d == 2) {
				maze[x][y].walls[NORTH] = false;
				maze[x][y - 1].walls[SOUTH] = false;
			}
			// down
			else if (d == 3) {
				maze[x][y].walls[SOUTH] = false;
				maze[x][y + 1].walls[NORTH] = false;
			}

			//set that block as "in the maze"
			maze[x][y].walls[ADDED] = true;

			// remove it from the block list
			for (int j = 0; j < blocks; j++) {
				if (maze[blockListX[j]][blockListY[j]].walls[ADDED]) {
					for (int i = j; i < blocks - 1; i++) {
						blockListX[i] = blockListX[i + 1];
						blockListY[i] = blockListY[i + 1];
					}
					blocks--;
					j = 0;
				}
			}

			// put all adjacent blocks that aren't in the maze in the block list
			if (x > 0 && !maze[x - 1][y].walls[ADDED]) {
				blockListX[blocks] = x - 1;
				blockListY[blocks] = y;
				blocks++;
			}
			if (x < MWIDTH - 1 && !maze[x + 1][y].walls[ADDED]) {
				blockListX[blocks] = x + 1;
				blockListY[blocks] = y;
				blocks++;
			}
			if (y > 0 && !maze[x][y - 1].walls[ADDED]) {
				blockListX[blocks] = x;
				blockListY[blocks] = y - 1;
				blocks++;
			}
			if (y < MHEIGHT - 1 && !maze[x][y + 1].walls[ADDED]) {
				blockListX[blocks] = x;
				blockListY[blocks] = y + 1;
				blocks++;
			}
		}
		//remove top left and bottom right edges
		maze[0][0].walls[WEST]=false;
		maze[MWIDTH-1][MHEIGHT-1].walls[EAST]=false;
	}


	public static class MazeComponent extends View
	{		

		public MazeComponent(Context context) { 
			super(context);
			targetPaint1.setColor(0xffffffff); //white 
			targetPaint2.setColor(0xff000000);  //black
			backPaint.setColor(0xffffff00);//Yellow
			backPaint2.setColor(0xff0000ff);//Blue
			backPaint2.setAlpha(0);//increments to Blue
			//backPaint3.setColor(0xff000000); // Black
			//backPaint3.setAlpha(0);//increments to Black
			//setBackgroundResource(R.drawable.background);			
		}

		public void onDraw(Canvas g)// DRAW MAZE
		{	

			/*float xscale = (float)g.getWidth()/(float)WINDOWX;
			float yscale = (float)g.getHeight()/(float)WINDOWY;
			g.scale(xscale,yscale);*/
			g.drawRect(0,0, BLOCK*MWIDTH, BLOCK*MHEIGHT, backPaint);

			for (int x=0; x<MWIDTH; x++)
			{
				for (int y=0; y<MHEIGHT; y++)
				{
					if (maze[x][y].walls[NORTH])
					{
						g.drawRect(x*BLOCK, y*BLOCK, (x+1)*BLOCK+wallWidth, y*BLOCK+wallWidth, mazePaint);
					}
					if (maze[x][y].walls[SOUTH])
					{
						g.drawRect(x*BLOCK, (y+1)*BLOCK, (x+1)*BLOCK+wallWidth, (y+1)*BLOCK+wallWidth, mazePaint);
					}
					if (maze[x][y].walls[WEST])
					{
						g.drawRect(x*BLOCK, y*BLOCK, x*BLOCK+wallWidth, (y+1)*BLOCK+wallWidth, mazePaint);
					}
					if (maze[x][y].walls[EAST])
					{
						g.drawRect((x+1)*BLOCK, y*BLOCK, (x+1)*BLOCK+wallWidth, (y+1)*BLOCK+wallWidth, mazePaint);
					}

					//draw end target
					if (x == MWIDTH-1 && y == MHEIGHT-1)
					{
						g.drawCircle((x*BLOCK)+((BLOCK+wallWidth)/2), (y*BLOCK)+((BLOCK+wallWidth)/2), (float) (BLOCK/3), targetPaint2);
						g.drawCircle((x*BLOCK)+((BLOCK+wallWidth)/2), (y*BLOCK)+((BLOCK+wallWidth)/2), (float) (BLOCK/4), targetPaint1);
						g.drawCircle((x*BLOCK)+((BLOCK+wallWidth)/2), (y*BLOCK)+((BLOCK+wallWidth)/2), (float) (BLOCK/6), targetPaint2);
						g.drawCircle((x*BLOCK)+((BLOCK+wallWidth)/2), (y*BLOCK)+((BLOCK+wallWidth)/2), (float) (BLOCK/11), targetPaint1);
					}	
				}
			}
		}
	}

	public class BallView extends View 
	{

		public float x;
		public float y;
		public float r;
		private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

		//construct new ball object
		public BallView(Context context, float x, float y, float radius) 
		{
			super(context);
			//color hex is [transparency][red][green][blue]
			mPaint.setColor(0xFF00FF00);  //not transparent. color is green
			this.x = x;
			this.y = y;
			this.r = radius;  //radius
		}               

		//called by invalidate()
		@Override
		protected void onDraw(Canvas canvas) 
		{
			super.onDraw(canvas);
			canvas.drawCircle(x, y, r, mPaint);
		}

	}


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
	public void onPause() //app moved to background, stop background threads
	{
		super.onPause();
		mTmr.cancel(); //kill\release timer (our only background thread)
		mTmr = null;
		mTsk = null;
		finish();
	}

	@Override
	public void onResume() //app moved to foreground (also occurs at app startup)
	{
		//create timer to move ball to new position
		mTmr = new Timer(); 
		mTsk = new TimerTask() {
			@Override
			public void run() {

				//if debugging with external device, 
				//  a log cat viewer will be needed on the device
				//android.util.Log.d("TiltBall","Timer Hit - " + mBallPos.x + ":" + mBallPos.y);

				//move ball based on current speed
				mBallPos.x += mBallSpd.x*1.1;
				mBallPos.y += mBallSpd.y*1.1;

				//get the balls square/maze cell
				int currentCellX =(int)(mBallPos.x) / BLOCK;
				int currentCellY =(int)(mBallPos.y) / BLOCK;

				//UI timer
				gameTimer ++;

				//COLLISION DETECTION

				//TRY
				try{

					//is there a top wall?
					if (maze[currentCellX][currentCellY].walls[NORTH])
					{
						//dont go over top wall

						if (mBallPos.y-radius < currentCellY*BLOCK+wallWidth)
						{
							mBallPos.y = currentCellY*BLOCK+wallWidth+radius;
							//System.out.println("top collision detected");
						}
					}
					
					//is there a bottom wall?
					if (maze[currentCellX][currentCellY].walls[SOUTH])
					{
						//dont move down
						if (mBallPos.y+radius >= currentCellY*BLOCK+BLOCK)
						{
							mBallPos.y = currentCellY*BLOCK+BLOCK-radius;
							//System.out.println("bottom collision detected");
						}
					}

					//is there a left wall?
					if (maze[currentCellX][currentCellY].walls[WEST])
					{
						//dont go over left wall
						if (mBallPos.x-radius < currentCellX*BLOCK+wallWidth)
						{
							mBallPos.x = currentCellX*BLOCK+wallWidth+radius;
							//System.out.println("left collision detected");
						}
					}

					//is there a right wall?
					if (maze[currentCellX][currentCellY].walls[EAST])
					{
						//dont move right
						if (mBallPos.x+radius >= currentCellX*BLOCK+BLOCK)
						{
							mBallPos.x = currentCellX*BLOCK+BLOCK-radius;
							//System.out.println("right collision detected");
						}

					}

					//is there neither top nor left wall? AND is there a top left overlap?
					//does the top cell have a left wall OR does the left cell have a top wall?
					if((!maze[currentCellX][currentCellY].walls[NORTH]) && (!maze[currentCellX][currentCellY].walls[WEST])
							&&(maze[currentCellX][currentCellY-1].walls[WEST] || maze[currentCellX-1][currentCellY].walls[NORTH]))
					{
						if (mBallPos.x>((currentCellX*BLOCK)+wallWidth) && mBallPos.y<((currentCellY*BLOCK)+wallWidth))//y simple collision
						{
							if (mBallPos.x-radius<currentCellX*BLOCK+wallWidth)
							{
								mBallPos.x=currentCellX*BLOCK+wallWidth+radius;
							}
						}
						else if(mBallPos.y>((currentCellY*BLOCK)+wallWidth) && mBallPos.y-radius < ((currentCellY*BLOCK)+wallWidth))
						{
							double y = Math.sqrt((Math.pow(radius, 2) - Math.pow((mBallPos.x-((currentCellX*BLOCK)+wallWidth)),2)));
							if (mBallPos.y-y<currentCellY*BLOCK+wallWidth)
							{
								mBallPos.y=(float) ((currentCellY*BLOCK+wallWidth)+y);
							}
						}

						if (mBallPos.y>((currentCellY*BLOCK)+wallWidth) && mBallPos.x<((currentCellX*BLOCK)+wallWidth))//x simple collision
						{
							if (mBallPos.y-radius<currentCellY*BLOCK+wallWidth)
							{
								mBallPos.y=currentCellY*BLOCK+wallWidth+radius;
							}
						}

						else if (mBallPos.x>((currentCellX*BLOCK)+wallWidth) && mBallPos.x-radius < ((currentCellX*BLOCK)+wallWidth))
						{
							double x = Math.sqrt((Math.pow(radius, 2) - Math.pow((mBallPos.y-((currentCellY*BLOCK)+wallWidth)),2)));
							if (mBallPos.x-x<(currentCellX*BLOCK)+wallWidth)
							{
								mBallPos.x=(float) ((currentCellX*BLOCK+wallWidth)+x);
							}
						}
					}

					//is there neither a top nor right wall? AND is there a top right overlap?
					//does the top right cell have a left wall OR does the right cell have a top wall?
					if((!maze[currentCellX][currentCellY].walls[NORTH] && !maze[currentCellX][currentCellY].walls[EAST]) 
							&& (maze[currentCellX+1][currentCellY-1].walls[WEST] || maze[currentCellX+1][currentCellY].walls[NORTH]))
					{
						if (mBallPos.y<currentCellY*BLOCK+wallWidth)
						{
							if (mBallPos.x+radius>(currentCellX+1)*BLOCK)
							{
								mBallPos.x = (currentCellX+1)*BLOCK-radius;
							}
						}
						else if (mBallPos.y>currentCellY*BLOCK+wallWidth && mBallPos.y-radius<currentCellY*BLOCK+wallWidth)
						{
							double x = Math.sqrt(Math.pow(radius, 2) - Math.pow(((currentCellY*BLOCK+wallWidth)-mBallPos.y),2));
							if ((mBallPos.x+x)>((currentCellX+1)*BLOCK))
							{
								mBallPos.x=(float) (((currentCellX+1)*BLOCK)-x);
							}
						}
					}

					//is there neither a bottom nor left wall? AND is there a bottom left overlap?
					//does the bottom left cell have a top wall OR does the bottom cell have a left wall?
					if((!maze[currentCellX][currentCellY].walls[SOUTH]) && (!maze[currentCellX][currentCellY].walls[WEST])
							&& (maze[currentCellX][currentCellY+1].walls[WEST] || maze[currentCellX-1][currentCellY+1].walls[NORTH]))
					{
						if (mBallPos.x<(currentCellX*BLOCK)+wallWidth)
						{
							if (mBallPos.y+radius>(currentCellY+1)*BLOCK)
							{
								mBallPos.y = (currentCellY+1)*BLOCK-radius;
							}
						}
						else if (mBallPos.x>currentCellX*BLOCK+wallWidth && mBallPos.x-radius<currentCellX*BLOCK+wallWidth)
						{
							double y = Math.sqrt(Math.pow(radius, 2) - Math.pow(((currentCellX*BLOCK+wallWidth)-mBallPos.x),2));
							if ((mBallPos.y+y)>((currentCellY+1)*BLOCK))
							{
								mBallPos.y=(float) (((currentCellY+1)*BLOCK)-y);
							}
						}
					}

					//is there neither a right nor bottom wall? AND is there a bottom right overlap?
					//does the bottom right cell have a top wall OR a left wall?
					if((!maze[currentCellX][currentCellY].walls[EAST]) && (!maze[currentCellX][currentCellY].walls[SOUTH])
							&&  (maze[currentCellX+1][currentCellY+1].walls[WEST] 
							|| maze[currentCellX+1][currentCellY+1].walls[NORTH] ))
					{
						if (mBallPos.y+radius>(currentCellY+1)*BLOCK)//collide with x
						{
							double x = Math.sqrt(Math.pow(radius, 2) - Math.pow((((currentCellY+1)*BLOCK)-mBallPos.y),2));
							if ((mBallPos.x+x)>((currentCellX+1)*BLOCK))
							{
								mBallPos.x=(float) (((currentCellX+1)*BLOCK)-x);
							}
						}
						else if (mBallPos.x+radius>(currentCellX+1)*BLOCK)//collide with y
						{
							double y = Math.sqrt(Math.pow(radius, 2) - Math.pow((((currentCellX+1)*BLOCK)-mBallPos.x),2));
							if ((mBallPos.y+y)>((currentCellY+1)*BLOCK))
							{
								mBallPos.y=(float) (((currentCellY+1)*BLOCK)-y);
							}
						}
					}

				}
				catch(ArrayIndexOutOfBoundsException e){}

				//CATCH, Array out of bounds
				///////

					if (currentCellX == 0 && currentCellY == 0)
					{				
						if (mBallPos.x-radius <= wallWidth)
						{

							if (first_cell_exited)
							{
								mazeStartToast.post(new Runnable()
								{
									public void run()
									{
										Toast.makeText(main, "Can't go back, they'll find you. Must go deeper...", Toast.LENGTH_SHORT).show();
									}
								});
								first_cell_exited = false;
							}
						}
					}

					if (currentCellX != 0 || currentCellY != 0)
					{				
						first_cell_exited = true;
					}

					if (currentCellX == MWIDTH-1 && currentCellY == MHEIGHT-1)
					{
						//Next Level

						openNextMaze = new Intent(GameActivity.this, NextMaze.class);
						startActivity(openNextMaze);
						
						

						/*if (!level_complete)
					{
						mazeFinishToast.post(new Runnable(){
							public void run()
							{
								Toast.makeText(main, "You go deeper into the maze...", Toast.LENGTH_SHORT).show();
							}
						});
						level_complete = true;

						//TODO
					}*/
					}
					
				

					//collision with sides of screen in case of no wall glitch
					if ((mBallPos.x + radius) > mScrWidth) mBallPos.x=mScrWidth-radius;
					if ((mBallPos.y + radius) > mScrHeight) mBallPos.y=mScrHeight-radius;
					if ((mBallPos.x - radius) < 0) mBallPos.x=0+radius;
					if ((mBallPos.y - radius) < 0) mBallPos.y=0+radius;


				//END OF COLLISION DETECTION

				//update ball class instance
				mBallView.x = mBallPos.x;
				mBallView.y = mBallPos.y;

				//redraw ball. Must run in background thread to prevent thread lock.
				RedrawHandler.post(new Runnable() {
					@Override
					public void run() {    
						mBallView.invalidate();

					}});

			}}; // TimerTask

			mTmr.schedule(mTsk,10,10); //start timer
			super.onResume();
	} // onResume

	//@Override
	/*public void onDestroy() //main thread stopped
	{
		super.onDestroy();
		//wait for threads to exit before clearing app
		System.runFinalizersOnExit(true); //ASK DR BLACK ABOUT ALTERNATIVE
		//remove app from memory
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


