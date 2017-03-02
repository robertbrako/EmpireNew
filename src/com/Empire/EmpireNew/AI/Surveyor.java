package com.Empire.EmpireNew.AI;

import java.util.ArrayList;

import com.Empire.EmpireNew.RuleManager;

public class Surveyor {
	private int id;
	private byte[][][] mapData;
	private int cityX, cityY;
	private int rows, cols;
	private int armyX, armyY;
	
	private ArrayList<int[]> visibleTiles;
	
	public void initialSurvey(byte[][][] unitDataIn, int id) {
		this.id = id;
		mapData = unitDataIn;
		rows = mapData.length;
		cols = mapData[0].length; // fine if each col is the same
		updateVisible(mapData);
		locateCity(mapData);
		
	}
	
	public int[] getCity() {
		if (mapData != null && visibleTiles != null) // remove this test once visData is addressed
			return new int[] { cityX, cityY };
		return new int[] { -999000, -999000 };
	}
	
	public byte army() {
		return RuleManager.ARMY;
	}
	
	public int[] armyAt() {
		return new int[] { armyX, armyY };
	}
	
	public void armySet(int x, int y) {
		armyX = x;
		armyY = y;
	}
	
	private void updateVisible(byte[][][] data) { // called by ?
		ArrayList<int[]> output = new ArrayList<int[]>();
		for (int i=0; i<rows; i++) {
			for (int j=0; j<cols; j++) {
				if (data[i][j][0] != (byte) 0) { // 0 is code for UNKNOWN
					output.add(new int[] {i, j });
					System.out.println("AI found visible tile at" + i + "," + j);
				}
			}
		}
		visibleTiles = output;
	}
	
	private void locateCity(byte[][][] data) { // can work for all ground units
		for (int i=0; i<rows; i++) {
			for (int j=0; j<cols; j++) {
				if (data[i][j][1]%32 == RuleManager.CITY && owns(data[i][j][1])) {
					cityX = i;
					cityY = j;
					System.out.println("AI found City (should find 1)");
				}
				if (data[i][j][1]%32 == RuleManager.ARMY && owns(data[i][j][1])) {// should identify second army piece, overwriting first
					armyX = i;
					armyY = j;
				}
			}
		}
	}
	private boolean owns(byte unit) {
		return (unit/32 == id) ? true : false;
	}
}
