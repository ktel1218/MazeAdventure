package com.katielefevre.mazeadventure;

import java.util.List;
import java.util.Random;
import java.util.Vector;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

public class MazeView extends View
{		
	private Paint texturePaint = new Paint (Paint.ANTI_ALIAS_FLAG); 
	private Paint targetPaint1 = new Paint(Paint.ANTI_ALIAS_FLAG);	// target color 1
	private Paint targetPaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);	// target color 2
	private Paint backPaint1 = new Paint (Paint.ANTI_ALIAS_FLAG);	// background color
	private Paint backPaint2 = new Paint (Paint.ANTI_ALIAS_FLAG);	// background color 2 for fade
	private Paint wallPaint = new Paint(Color.BLACK);
	
	private Maze theMaze;
	private List<BackgroundCell> theBG;
	
	public MazeView(Context context) { 
		super(context);
		
		targetPaint1.setColor(0xffffffff); 		//white 
		targetPaint2.setColor(0xff000000);  	//black
		backPaint1.setColor(0xffffff00);		//Yellow
		backPaint2.setColor(0xff0000ff);		//Blue
		backPaint2.setAlpha(0);					//increments to Blue
		
		//backPaint3.setColor(0xff000000); 		// Black
		//backPaint3.setAlpha(0);				//increments to Black
		//setBackgroundResource(R.drawable.background);			
	}

	public void init(Maze maze) {
		theMaze = maze;	
		createBG();
	}
	
	@Override
	public void onDraw(Canvas g)
	{	
		super.onDraw(g);
		
		if (theMaze == null) return;
		
		drawBG(g);
		drawMaze(g);
	}
	
	private void drawMaze(Canvas g)
	{
		//g.drawRect(theMaze.getRect(), backPaint1);	
		
		for (Maze.Cell[] row : theMaze.cells) {
			for (Maze.Cell cell : row) {
				for (Maze.Wall wall : cell.getWalls()) {
					g.drawRect(Maze.getWallRect(cell, wall), wallPaint);
				}
			}
		}		
		
		//draw end target
		float radii1[] = {3, 6};
		float radii2[] = {4, 11};
		
		int x = theMaze.getExitCell().getCenter().x;
		int y = theMaze.getExitCell().getCenter().y;
		
		for (float radius : radii1) {
			g.drawCircle(x, y, (float) (Maze.BLOCK/radius), targetPaint2);
		}
		for (float radius : radii2) {
			g.drawCircle(x, y, (float) (Maze.BLOCK/radius), targetPaint1);
		}	
	}

	private void drawBG(Canvas g)
	{
		for (BackgroundCell cell : theBG) {
			texturePaint.setColor(cell.color);
			g.drawRect(cell.square, texturePaint);
		}
	}
	
	private void createBG()
	{
		int squaresize = (int)(0.5 * Maze.WALL_WIDTH);
		int width = theMaze.getWidth() / squaresize; 
		int height = theMaze.getHeight() / squaresize;
		
		theBG = new Vector<BackgroundCell>();
		
		for (int i=0; i<height; i++)
		{
			for (int j=1; j<width; j++)
			{
			  // Distance from Beginning
				double dFromB = Math.sqrt(((0-i)*(0-i))+((0-j)*(0-j)));   
			  // Distance from End
				double dFromE = Math.sqrt(((height-i)*(height-i))+((width-j)*(width-j)));  

				Random rand = new Random();

				//30-orange, 50-yellow, 100-green, 200-blue, 300-violet, 360-red

				int max=100; 			// value ceiling (100,white)
				int min=54; 			// value base (0, black)
				int mainHue=200;
				int gradientSize=20;
				double startHue=280;
				double endHue=80;

				if (dFromB<gradientSize)
				{
					mainHue=(int) ((((mainHue-startHue)/gradientSize)*dFromB)+startHue);   //y=mx+b
				}

				if (dFromE<gradientSize)
				{
					mainHue=(int) ((((mainHue-endHue)/gradientSize)*dFromE)+endHue);
				}

				int randomNum_value = rand.nextInt(max-min+1)+min;//generate random number for value
				int color = Color.HSVToColor( new float[]{ mainHue, 1, (float)(randomNum_value*.01)} );

				//hue (0-360), saturation (0-1), value (0-1)
				Rect square = new Rect((squaresize * j), (squaresize * i), (squaresize * j)+squaresize, (squaresize * i)+squaresize);
				theBG.add(new BackgroundCell(square, color));
			}
		}
	}
}
