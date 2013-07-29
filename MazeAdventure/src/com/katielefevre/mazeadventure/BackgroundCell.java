package com.katielefevre.mazeadventure;

import android.graphics.Rect;

public class BackgroundCell 
{
	public Rect square;
	public int color;
	
	BackgroundCell(Rect square, int color) {
		this.square = square;
		this.color = color;
	}
}
