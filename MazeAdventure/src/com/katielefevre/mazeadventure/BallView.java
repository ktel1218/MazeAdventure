package com.katielefevre.mazeadventure;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

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
		this.r = radius;  
	}               

	@Override
	protected void onDraw(Canvas canvas) 
	{
		super.onDraw(canvas);
		canvas.drawCircle(x, y, r, mPaint);
	}

}
