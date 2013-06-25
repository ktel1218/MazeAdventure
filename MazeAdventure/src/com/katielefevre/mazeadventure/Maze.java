package com.katielefevre.mazeadventure;

import java.util.ArrayList;
import java.util.Random;

import android.graphics.Point;
import android.graphics.Rect;

public class Maze {
	public static int BLOCK;
	public static int WALL_WIDTH;
	
	private final static double WALL_FACTOR = 0.22;

	//member variables
	private int width;
	private int height;
	
	private int XCellCount;
	private int YCellCount;
	
	private ArrayList<Cell> ActiveCellList = 
			    new ArrayList<Maze.Cell>(); ;
	
	public Cell cells[][]; 
	public MazeBall Ball; 
	
	// Singleton
	private static Maze Instance = null;
	private Maze() {}
	public static Maze getInstance() {
		if (Instance == null) {
			Instance = new Maze();
		}
		return Instance;
	}
	//
	
	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}
	
	public Rect getRect() {
		return new Rect(0, 0, getWidth(), getHeight());
	}
	
	public Cell getExitCell() {
		return cells[XCellCount-1][YCellCount-1];
	}

	public static Maze BuildMaze(int screenWidth, int screenHeight, int block) {
		
		Maze theMaze = getInstance();

		BLOCK = block;
		WALL_WIDTH = (int)(WALL_FACTOR * BLOCK);
		
		theMaze.XCellCount = (screenWidth / BLOCK);
		theMaze.YCellCount = (screenHeight / BLOCK);
		
		theMaze.buildMaze_Internal();
		
		theMaze.Ball = MazeBall.getInstance();
		theMaze.Ball.init(block);
		
		return theMaze;
	}
	
	private void buildMaze_Internal() {

		setWidth(XCellCount * BLOCK);
		setHeight(YCellCount * BLOCK);
		
		cells = new Cell[XCellCount][YCellCount];

		for (int i=0; i < XCellCount; i++) {
			for (int j=0; j < YCellCount; j++) {
				cells[i][j] = new Maze.Cell(i, j);
			}
		}
		
		ActiveCellList.clear();
		
		int xLastCell = XCellCount - 1;
		int yLastCell = YCellCount - 1;
		
		// Choose random starting block and add it to maze
		int x = new Random().nextInt(XCellCount);
		int y = new Random().nextInt(YCellCount);
		cells[x][y].status = Cell.ADDED;
		
		addAdjacentCells(x, y);

		while (ActiveCellList.size() > 0)
		{
			//choose a random block from block list
			int randomBlock = (int) (Math.random() * ActiveCellList.size());
			Cell randomCell = ActiveCellList.get(randomBlock);
			x = randomCell.x;
			y = randomCell.y;
			
			int[] dir = new int[4];
			int numdir = 0;

			// find which block in the maze it is adjacent to
			// if cell exists, note it and it's direction

			// left
			if (x > 0 && cells[x - 1][y].isInMaze()) {
				dir[numdir++] = Wall.WEST;
			}
			// right
			if (x < xLastCell && cells[x + 1][y].isInMaze()) {
				dir[numdir++] = Wall.EAST;
			}
			// up
			if (y > 0 && cells[x][y - 1].isInMaze()) {
				dir[numdir++] = Wall.NORTH;
			}
			// down
			if (y < yLastCell && cells[x][y + 1].isInMaze()) {
				dir[numdir++] = Wall.SOUTH;
			}

			int randomDirection = dir[new Random().nextInt(numdir)];

			// And remove the wall between the two 
			//lefts
			if (randomDirection == Wall.WEST) {
				cells[x][y].walls[Wall.WEST] = false;
				cells[x - 1][y].walls[Wall.EAST] = false;
			}
			// rights
			else if (randomDirection == Wall.EAST) {
				cells[x][y].walls[Wall.EAST] = false;
				cells[x + 1][y].walls[Wall.WEST] = false;
			}
			// ups
			else if (randomDirection == Wall.NORTH) {
				cells[x][y].walls[Wall.NORTH] = false;
				cells[x][y - 1].walls[Wall.SOUTH] = false;
			}
			// downs
			else if (randomDirection == Wall.SOUTH) {
				cells[x][y].walls[Wall.SOUTH] = false;
				cells[x][y + 1].walls[Wall.NORTH] = false;
			}

			//set the block as "in the maze"
			cells[x][y].status = Cell.ADDED;

			// remove it from the block list
			ActiveCellList.remove(randomCell);
			addAdjacentCells(x, y);
		}
		
		//remove top left and bottom right edges
		cells[0][0].walls[Wall.WEST] = false;
		cells[xLastCell][yLastCell].walls[Wall.EAST] = false;
	}

	private void addAdjacentCells(int x, int y) {
		int xLastCell = XCellCount - 1;
		int yLastCell = YCellCount - 1;
		
		// Add all inactive adjacent blocks to block list
		if (x > 0 && cells[x - 1][y].isInactive()) addToActiveCellList(x-1, y);
		if (x < xLastCell && cells[x + 1][y].isInactive()) addToActiveCellList(x+1, y);
		if (y > 0 && cells[x][y - 1].isInactive()) addToActiveCellList(x, y-1);
		if (y < yLastCell && cells[x][y + 1].isInactive()) addToActiveCellList(x, y+1);	
	}
	
	private void addToActiveCellList(int x, int y) {
		cells[x][y].status = Cell.ACTIVE;
		ActiveCellList.add(cells[x][y]);
	}
	
	public static Rect getWallRect(Cell cell, Wall wall) {
		Rect r = new Rect();
		
		/*
		 *     (yT)
		 *  (xL) +-------------+ (xR)
		 *       |      ^      |
		 *       |      |      |
		 *       | <- BLOCK -> |
		 *       |      |      |
		 *       |      v      |
		 *       +-------------+
		 *     (yB)  
		 */
		
		int xL =  (cell.x * BLOCK);
		int xR = ((cell.x + 1) * BLOCK);
		int yT =  (cell.y * BLOCK);
		int yB = ((cell.y + 1) * BLOCK);
		
		int w = WALL_WIDTH;
		
		switch (wall.direction)
		{
		case Wall.NORTH:
			r.left = xL;
			r.top = yT;
			r.right = xR + w;
			r.bottom = yT + w;
			break;
			
		case Wall.SOUTH:
			r.left = xL;
			r.top = yB;
			r.right = xR + w;
			r.bottom = yB + w;
			break;
			
		case Wall.WEST:
			r.left = xL;
			r.top = yT;
			r.right = xL + w;
			r.bottom = yB + w;
			break;
			
		case Wall.EAST:
			r.left = xR;
			r.top = yT;
			r.right = xR + w;
			r.bottom = yB + w;
			break;
		}
		return r;
	}
	
	public static class Cell 
	{ 
		int x, y;
		
		private static final int INACTIVE = 0, ACTIVE = 1, ADDED = 2;
		private int status = INACTIVE;
		
		boolean[] walls = { true, true, true, true };
		
		private Cell(int x, int y) {
			this.x = x;
			this.y = y;
		}
		
		private boolean isInMaze() {
			return (status == ADDED);
		}
		
		private boolean isInactive() {
			return (status == INACTIVE);
		}
		
		public Point getCenter() {
			Point p = new Point();
			p.x = (x * BLOCK) + ((BLOCK + WALL_WIDTH) / 2);
			p.y = (y * BLOCK) + ((BLOCK + WALL_WIDTH) / 2);
			return p;
		}
		
		public ArrayList<Wall> getWalls() {
			ArrayList<Wall> w = new ArrayList<Maze.Wall>();
			for (int i = 0; i < 4; i++) {
				if(walls[i]) {
					w.add(new Wall(i));
				}
			}	
			return w;
		}
		
		public String printCell()
		{
			return " " + this.x + " " + 
					    status + " " + 
		          walls[Wall.NORTH] + " " + 
					    walls[Wall.EAST]  + " " + 
		          walls[Wall.SOUTH] + " " + 
					    walls[Wall.WEST] ;
		}
	}
	
	public static class Wall
	{
		public static final int NORTH = 0, SOUTH = 1, WEST = 2, EAST = 3;
		
		private int direction;
		
		private Wall(int direction) {
			this.direction = direction;
		}
		
	}
}
