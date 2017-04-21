package com.rmbcorp.empire.API;

public interface Grid {
	public Tile origin();
	public Tile atCoords(int dir0, int dir1);
	public Tile terminus();
	public Grid subGrid(int subOriginDir0, int subOriginDir1, int length, int width);
}
