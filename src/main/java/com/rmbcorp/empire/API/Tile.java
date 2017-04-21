package com.rmbcorp.empire.API;
import java.util.List;

import com.Empire.EmpireNew.Objects.Terrain;
import com.Empire.EmpireNew.util.SaveData;

public interface Tile {

	public TileID id ();
	
	/** Description of equals(Tile that)
	 * @return				1 iff this.tileId().equals(that.tileId())
	 * 						0 otherwise
	 */
	public boolean equals(Tile that);
	
	/** Description of next(int direction)
	 * @param direction			integer from 0 to 5
	 * @return					the next tile in a given direction
	 * note that next(X).next( (x+3)%5 ) = this
	 * returns NULL iff this.isBlank()=true... so check for blankness before calling next(X).next(Y) 
	 */
	public Tile next(int direction);

	public boolean isBlank();
		
	/**	Description of visibleTo(Player)
	 * 
	 * This function tells whether a given player has permission to see a given tile.
	 * 
	 * @param player	the player whose visibility you want to know about
	 * @return			1 if visible to player, 0 if not
	 */

	public boolean visibleTo(Player player);
	
	// All the following will only return non-null if the tile is visible to the player
	// (so if there is an operation which involves multiple calls to these, check visibleTo(Player) first,
	// and skip the whole operation to save time...
	
	/** Description of gContents(Player requester) (the 'g' stands for 'ground')
	 * 
	 * 
	 * @return		a single piece which is either a ground unit or a city,
	 * 				or NULL if either:
	 * 					there are no ground piece
	 * 					OR the tile is not visible to requester
	 */
	public Piece gContents(Player requester);

	/** 
	 * 
	 * @param 		requester
	 * @return		a list of air units over that tile
	 * 				an empty list if there are none
	 * 				NULL iff the tile is not visible to requester
	 */
	public List<Unit> airContents(Player requester);
	
	public Terrain tileTerrain(Player requester);
	
	public SaveData save();
	
}
