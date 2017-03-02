package com.Empire.EmpireNew.API;

import com.Empire.EmpireNew.Objects.Terrain;

/** This class represents field units, which are the mobile, fighting pieces
 * in the game, as opposed to cities, which are the other type of piece.
 * 
 * @author daks
 *
 */
public interface Unit extends Piece {	
	public Terrain terrain();
}
