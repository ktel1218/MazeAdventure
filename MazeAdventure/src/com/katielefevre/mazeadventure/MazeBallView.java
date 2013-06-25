package com.katielefevre.mazeadventure;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

public class MazeBallView extends View 
{
	private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private MazeBall Ball;
	
	//construct new ball object
	public MazeBallView(Context context) 
	{
		super(context);
		//color hex is [transparency][red][green][blue]
		mPaint.setColor(0xFF00FF00);  //not transparent. color is green
	}      
	
	public void init(MazeBall ball) {
		Ball = ball; 	
	}

	@Override
	protected void onDraw(Canvas canvas) 
	{
		super.onDraw(canvas);
		
		if (Ball == null) return;
		canvas.drawCircle(Ball.getBallPos().x, Ball.getBallPos().y, Ball.getRadius(), mPaint);
	}

}
