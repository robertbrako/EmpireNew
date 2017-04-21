package com.rmbcorp.empire.Objects;

import com.rmbcorp.empire.API.*;
import com.rmbcorp.empire.API.GameExceptions.InvalidMoveException;
import com.rmbcorp.empire.util.SaveData;

import java.util.ArrayList;
import java.util.List;

public class GameTile implements BackendTile {

	private GameTile [] nextTile = new GameTile[6]; // save if out-of-order saving is allowed
	private Terrain localType = Terrain.SEA;
	private boolean isBlank; // might not need to save this data
	private boolean isComplete = false;// might not need to save this data
	private GameTileID id;
	private Piece gContents;
	private List<Unit> airContents;
	
	public GameTile(GameTile dir0NextTile, GameTile dir1NextTile, GameTile dir2NextTile, GameTile dir3NextTile,
			GameTile dir4NextTile, GameTile dir5NextTile, Terrain type, boolean makeBlank) {
		
		isBlank = makeBlank;
		localType = type;
		id = new GameTileID();
		airContents = new ArrayList<Unit>();
		
		nextTile[0] = dir0NextTile; // to down-right
		nextTile[1] = dir1NextTile; // to right
		nextTile[2] = dir2NextTile; // to up-right
		nextTile[3] = dir3NextTile; // to up-left
		nextTile[4] = dir4NextTile; // to left
		nextTile[5] = dir5NextTile; // to down-left
		
		for(int dir = 0; dir <=5; dir++) { //TODO: this might be redundant
			if(nextTile[dir]!=null) {
				nextTile[dir].setNext(oppositeDirection(dir), this);
			}
		}
		
		if(!(isBlank)) {
			boolean foundGap = false;
			for(int dir = 0; dir <=5; dir++) {
				if(null==nextTile[dir]) {
					foundGap = true;
				}
			}
			isComplete = !(foundGap); // a grid or game map should NEVER return a non-blank tile with any null neighbors
		}
		
	}
	
	public static int oppositeDirection(int dir) {
		return (dir+3)%6;
	}
	
	protected void setNext(int direction, GameTile newNext) {
		if(direction >= 0 && direction <= 5) {
			nextTile[direction] = newNext;
		} else {
			// fail silently (as of right now)
			// throw some exception (later)
		}
		
		if(!(isBlank)) {
			boolean foundGap = false;
			for(int dir = 0; dir <=5; dir++) {
				if(null==nextTile[dir]) {
					foundGap = true;
				}
			}
			isComplete = !(foundGap); // a grid or game map should NEVER return a non-blank tile with any null neighbors
		}
	}
	
	public boolean isComplete() {
		return isComplete;
	}
	
	@Override
	public TileID id() {
		return id;
	}

	@Override
	public boolean equals(Tile that) {
		if(that==null) {
			return false;
		} else {
			return this.id().equals(that.id());
		}
	}

	@Override
	public Tile next(int direction) {
		if(direction >= 0 && direction <= 5) {
			return nextTile[direction];
		} else {
			return null;
			// fail silently (as of right now)
			// throw some exception (later)
		}
	}
	
	@Override
	public BackendTile nextG(int direction) {
		if(direction >= 0 && direction <= 5) {
			return nextTile[direction];
		} else {
			return null;
			// fail silently (as of right now)
			// throw some exception (later)
		}		
	}

	@Override
	public boolean visibleTo(Player player) {
		if(isBlank()) {
			return false; // blank units can never be visible
		}
		return true;
		/* HACK - just be visible until further notice
		if(gContents!=null) {
			if(gContents.owner().equals(player)) {
				return true;
			}
		}
		for(Unit u : airContents) {
			if(u!=null) {
				if(u.owner().equals(player)) {
					return true;
				}
			}
		} // owning any piece on a Tile makes that Tile visible
		
		for(int dir=0; dir<=5; dir++) {
			if(nextTile[dir]!=null) {
				if(nextTile[dir].gContents!=null) {
					if(nextTile[dir].gContents.owner().equals(player)) {
						return true;
					}
				}
				for(Unit u : nextTile[dir].airContents) {
					if(u!=null) {
						if(u.owner().equals(player)) {
							return true;
						}
					}
				}
			}
		} // owning any piece adjacent to a Tile makes that Tile visible
		
		//owning certain pieces have visibility radius larger than 1; generalized algorithm is forthcoming...
		
		return false; //if they don't have a unit that can see it by now, they can't see
		*/
	}
	/** gContents now checks visibility and may return null if player has no privileges
	 * @param requestor the Player seeking knowledge of ground unit contents
	 * @return a ground piece if visible to player, null otherwise
	 */
	@Override
	public Piece gContents(Player requestor) {
		if (gContents != null && visibleTo(requestor)) {
			// need sub-tests: most units visible by default; submarines not visible except under certain conditions
			return gContents;
		}
		else return null; // actually, else needs to test for if the piece is 
	}

	@Override
	public List<Unit> airContents(Player requestor) {
		return airContents;
	}

	@Override
	public boolean isBlank() {
		return isBlank;
	}
	/** TileTerrain now checks visibility.
	 * Thus, this method is more expensive; users of this function are expected to save the result.
	 * @param requestor the Player seeking knowledge of terrain type
	 * @return localType Terrain.SEA, Terrain.LAND, or Terrain.UNKNOWN if not visible to player
	 */
	@Override
	public Terrain tileTerrain(Player requestor) {
		if (visibleTo(requestor))
			return localType;
		else return Terrain.UNKNOWN;
	}
	
	@Override
	public void addUnit(Unit u) throws InvalidMoveException {
		if(u==null) {
			throw new NullPointerException("Cannot add a null unit to a tile.");
		}
		if(!u.isMobile()) { //we will add it to the location it starts at
			if(u.location()==null||u.location()==this) {
				// just fall through to the general case
			} else {  //immobile units can only be added to one tile per game
				throw new InvalidMoveException();
			}
		}
		
		if(u.terrain()==Terrain.AIR) { //there can be multiple air units on this tile
			if(!airContents.contains(u)) { airContents.add(u); }
		} else { //there can only be one sea or land unit on this tile
			if(gContents==null) {
				if (u.terrain() == localType) {
					gContents=u;
				}
				else throw new InvalidMoveException();
			} else { //do not clobber a unit to place another
				System.out.println("Throwing invalid move exception due to clobbering!");
				throw new InvalidMoveException();
			}
			//TODO: add a handler for armies to move onto transports
		}
		return;
	}
	
	@Override
	public void removeUnit(Unit u) {
		if(u==null) {
			throw new NullPointerException("Cannot remove a null unit from a tile.");
		}
		if(u.terrain()==Terrain.AIR) {
			while(airContents.contains(u)) {
				airContents.remove(u);
			}
		} else {
			if(gContents==u) {
				gContents=null;
			}
		}
		return;
	}

	@Override
	public void addCity(Piece c) {
		if(c.type() != PieceType.CITY) {
			// fail silently
		} else {
			gContents = c;
		}
	}

	@Override
	public SaveData save() {
		SaveData result;
		StringBuilder conts = new StringBuilder();
		conts.append(false); // uh?...
		result = new SaveData(conts);
		return result;
	}
}
 