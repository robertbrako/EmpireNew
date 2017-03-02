package com.Empire.EmpireNew.API;

public interface BackendTile extends Tile {
	public void removeUnit(Unit u);
	public void addUnit(Unit u) throws GameExceptions.InvalidMoveException;
	public void addCity(Piece c);
	public BackendTile nextG(int dir);
}
