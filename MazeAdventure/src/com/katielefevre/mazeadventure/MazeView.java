package com.katielefevre.mazeadventure;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class MazeView extends View
{		
	private Paint targetPaint1 = new Paint(Paint.ANTI_ALIAS_FLAG);	// target color 1
	private Paint targetPaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);	// target color 2
	private Paint backPaint1 = new Paint (Paint.ANTI_ALIAS_FLAG);	// background color
	private Paint backPaint2 = new Paint (Paint.ANTI_ALIAS_FLAG);	// background color 2 for fade
	private Paint wallPaint = new Paint(Color.BLACK);
	
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

	// DRAW MAZE
	public void onDraw(Canvas g)
	{	
		Maze theMaze = Maze.getInstance();
		
		g.drawRect(theMaze.getRect(), backPaint1);	
		
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
}
