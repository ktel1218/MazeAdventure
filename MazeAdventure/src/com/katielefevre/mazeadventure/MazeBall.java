package com.katielefevre.mazeadventure;

import android.graphics.PointF;

//
// Ball variables - move to Maze


public class MazeBall {
	private final float RADIUS_FACTOR = (float) 0.25;
	
	private PointF mBallAccel = new PointF();
	private PointF mBallPos = new PointF();
	private PointF mBallSpd = new PointF();
	private float mRadius; 
	
	private MazeBall () { }
	private static MazeBall Instance = null;
	public static MazeBall getInstance() {
		if (Instance == null) {
			Instance = new MazeBall();
			
		}
		return Instance;
	}
	
	public void init(int block) {
		//this.block = block;
		
		//set ball radius as factor of BLOCK 
		// size for scaling purposes
		setRadius(RADIUS_FACTOR * block);
		// STARTS BALL IN UPPER LEFT OF MAZE
		setBallPos(new PointF(0, 0));
		setBallSpd(new PointF(0, 0));
		setBallAccel(new PointF(0, 0));
		//
	}
	
	public PointF getBallPos() {
		return mBallPos;
	}
	public void setBallPos(PointF ballPos) {
		this.mBallPos = ballPos;
	}

	public PointF getBallAccel() {
		return mBallAccel;
	}
	public void setBallAccel(PointF ballAccel) {
		this.mBallAccel = ballAccel;
	}
	
	public PointF getBallSpd() {
		return mBallSpd;
	}
	public void setBallSpd(PointF ballSpd) {
		this.mBallSpd = ballSpd;
	}

	public float getRadius() {
		return mRadius;
	}

	public void setRadius(float mRadius) {
		this.mRadius = mRadius;
	}

}
