package com.rmbcorp.empire;

import com.rmbcorp.empire.API.Grid;
import com.rmbcorp.empire.API.Piece;
import com.rmbcorp.empire.API.Player;
import com.rmbcorp.empire.API.Tile;
import com.rmbcorp.empire.API.Unit;
import com.rmbcorp.empire.Objects.BasicGrid;
import com.rmbcorp.empire.Objects.GameTile;
import com.rmbcorp.empire.Objects.Terrain;

import java.util.ArrayList;

/**
 * Spinoff of ViewController that searches the backend for tile and unit data.  Importantly, the 
 * result is stored in a way that reduces the amount of times the backend needs to be touched.  
 * I could probably word that differently.
 * @author Aspire
 *
 */
public class ClientGrid implements Grid {
	
	private GridData[][] gridData;
	int rows = 0;
	int cols = 0;
	//private ArrayList<AirContents> airContents; // airContents gets its own matrix overlay; should be more efficient
	
	public ClientGrid(int rows, int cols, BasicGrid bGrid, Player player) {
		this.rows = rows;
		this.cols = cols;
		gridData = new GridData[rows][cols];
		//setInitialVisibility // called externally
		//initClientGrid(bGrid.originG(), player);;
		
	}
	
	public GridData[][] getGridData() {
		return gridData;
	}
	
	public void initClientGrid(GameTile origin, Player player) {
		GridData[][] results = new GridData[rows][cols];
		int i=0, j=0;
		GameTile gt = origin;
		GameTile rt = gt; // track row-begin tile

		while(gt.nextG(1) != null) { // I think this may as well be: while(true) { .. }.
			// terrain type routine
			try {
				results[i][j].findTerrainType(gt.tileTerrain(player));
			} catch (NullPointerException e) {
				System.out.println("Terrain fail at (" + i + "," + j + "); current tileID is " + gt.id().toString());
			}

			//unit routine
			results[i][j].findGroundUnits(gt, player);

			//List<Unit> aPiece = gt.airContents(player);
			results[i][j].findAirUnits(gt, player);

			// general loop iteration procedure:
			gt = (GameTile) gt.nextG(1);
			j++;
			if (gt.isBlank()) {
				int mod = (i%2)*5;
				if (!rt.nextG(mod).isBlank()) {
					gt = (GameTile) rt.nextG(mod);
					rt = gt;
					i++;
					j=0;
				}
				//else break;
			}
		} // end while
		
		gridData = results;
	}
	
	public byte getGroundUnits(int row, int col) {
		return gridData[row][col].gContents;
	}

	@Override
	public Tile origin() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Tile atCoords(int dir0, int dir1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Tile terminus() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Grid subGrid(int subOriginDir0, int subOriginDir1, int length,
			int width) {
		// TODO Auto-generated method stub
		return null;
	}

	private class GridData {
		private Terrain type = Terrain.UNKNOWN;
		private byte gContents;
		private ArrayList<Unit> airContents;
		
		private GridData() {
			airContents =  new ArrayList<Unit>();
		}
		
		public void setTerrainType(Terrain type) {
			this.type = type;
		}

		private void findTerrainType(Terrain type) {
			this.type = type;
		}
		
		// setGroundUnits only sets up to 1 ground unit at present 
		// setGroundUnits will also identify Cities - consider name change or implementation change
		private void findGroundUnits(GameTile g, Player requestor) {
			Piece piece = g.gContents(requestor); 
			if (piece != null) {  // CAUTION: request may be null even if unit is present (depending on visibility)
				this.gContents = piece.type();
			}
		}
		private void findAirUnits(GameTile g, Player requestor) {
			ArrayList<Unit> input = (ArrayList<Unit>) g.airContents(requestor);
			if(input.size() > 0) {
				airContents = input;
			}
		}
	}
}
