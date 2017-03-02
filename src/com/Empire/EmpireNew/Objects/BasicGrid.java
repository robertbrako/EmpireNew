package com.Empire.EmpireNew.Objects;

import java.util.ArrayList;

import com.Empire.EmpireNew.API.Grid;
import com.Empire.EmpireNew.API.Tile;
import com.Empire.EmpireNew.util.TerrainMatrix;

public class BasicGrid implements Grid {
	
	private ArrayList<GameTile> contents;
	private ArrayList<GameTile> inBoundsContents;
	private GameTile terminus;
	private ArrayList<GameTile> startLocation = new ArrayList<GameTile>();
	
	public BasicGrid(int rows, int cols) {
		contents = new ArrayList<GameTile>();
		inBoundsContents = new ArrayList<GameTile>(); // this does not contain any blank tiles
		int cellCount = 0; //increment EVERY time you add a cell
		int trueWidth = cols+2;
		Terrain[][] terrainMatrix = TerrainMatrix.spawn(rows, cols);
		Terrain t;
		
		int lcount, wcount, columnFactor;
		for(lcount=0; lcount <= rows+1; lcount++) {
			columnFactor = ((lcount)%2); // needed for non-skewed grid. Return 0 or 1.
			
			//BEGINNING-OF-ROW PROCESS
			if(lcount==0) { //make a blank to start each row, first one with no back-references
				contents.add(new GameTile(null, null, null, null, null, null, Terrain.SEA, true) );
			} else { //make a blank to start each row, referring back to previous row
				if (lcount%2 == 1)
					contents.add(new GameTile(null, null, contents.get(cellCount-trueWidth+1), contents.get(cellCount-trueWidth), null, null, Terrain.SEA, true) );
				else // modification added to make skewed grid work
					contents.add(new GameTile(null, null, contents.get(cellCount-trueWidth), null, null, null, Terrain.SEA, true) );
			} cellCount++;
			
			//MIDDLE-OF-ROW PROCESS
			for(wcount=0; wcount < cols; wcount++) {
				if(lcount==0) { //make a row of blanks (without ref to previous row) if it's the first row
					contents.add(new GameTile(null, null, null, null, contents.get(cellCount-1), null, Terrain.SEA, true) );
				} else if(lcount==rows+1) { //make a row of blanks if it's the last row
					contents.add(new GameTile(null, null, contents.get(cellCount-trueWidth+columnFactor), contents.get(cellCount - trueWidth + columnFactor - 1), contents.get(cellCount-1), null, Terrain.SEA, true) );
				} else { //otherwise make real ones
					try {
						t = terrainMatrix[lcount-1][wcount]; // lcount runs from 1 to 10, wcount runs from 0 to 9 !!!
					} catch (ArrayIndexOutOfBoundsException e) { t = Terrain.SEA; }
					contents.add(new GameTile(null, null, contents.get(cellCount-trueWidth+(1-columnFactor)), contents.get(cellCount-trueWidth+(1-columnFactor)-1), contents.get(cellCount - 1), null, t, false) );
				} cellCount++;
			}
			
			if(lcount==rows) {
				terminus = contents.get(cellCount-1);
			}
			//END-OF-ROW PROCESS: make a blank to end each row
			if (lcount == 0) {
				contents.add(new GameTile(null, null, null, null, contents.get(cellCount-1), null, Terrain.SEA, true) );
			}
			else if (lcount == rows+1) {
				contents.add(new GameTile(null, null, null, contents.get(cellCount - trueWidth + columnFactor - 1), contents.get(cellCount-1), null, Terrain.SEA, true) );
			}
			else {
				if (lcount%2 == 0) {
					contents.add(new GameTile(null, null, null, null, contents.get(cellCount-1), null, Terrain.SEA, true) );
				}
				else {
					contents.add(new GameTile(null, null, null, contents.get(cellCount - trueWidth + columnFactor - 1), contents.get(cellCount-1), null, Terrain.SEA, true) );
				}
				
			}
			cellCount++;
		}
		
		for(GameTile gt:contents) {
			if(!gt.isBlank()) {
				inBoundsContents.add(gt);
			}
		}
		//setting the unset neighbors
		for (GameTile gt: inBoundsContents) {
			if ((contents.indexOf(gt)/trueWidth)%2 == 0) {
				gt.setNext(0, contents.get(contents.indexOf(gt) + trueWidth + 1));
				gt.setNext(1, contents.get(contents.indexOf(gt) + 1));
				gt.setNext(5, contents.get(contents.indexOf(gt) + trueWidth));
			}
			else {
				gt.setNext(0, contents.get(contents.indexOf(gt) + trueWidth));
				gt.setNext(1, contents.get(contents.indexOf(gt) + 1));
				gt.setNext(5, contents.get(contents.indexOf(gt) + trueWidth - 1));
			}
		}
		
		//finally, let's not forget the seed location
		int[] locales = TerrainMatrix.getStart();
		for (int i=0;i<locales.length; i+=2){
			startLocation.add(atCoords(locales[i], locales[i+1]));
		}
	}
	
	@Override
	public Tile origin() {
		return inBoundsContents.get(0);
	}
	
	public GameTile originG() {
		return inBoundsContents.get(0);
	}
	
	public GameTile getStartLocation(int index) {
		return startLocation.get(index);
	}

	@Override
	public GameTile atCoords(int dir0, int dir1) {
		GameTile workingTile = inBoundsContents.get(0);
		for (int i=0; i < dir0; i++) {
			workingTile = (GameTile) workingTile.nextG((i % 2)*5);
		}
		for (int j=0; j < dir1; j++) { // even: 0; odd: 5
			workingTile = (GameTile) workingTile.nextG(1);
		}
		return workingTile;
	}

	@Override
	public Tile terminus() {
		return terminus;
	}

	@Override
	public Grid subGrid(int subOriginDir0, int subOriginDir1, int length, int width) {
		// TODO Auto-generated method stub
		return null;
	}
}
