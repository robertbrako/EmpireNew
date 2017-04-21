package com.rmbcorp.empire.Objects;

import com.rmbcorp.empire.API.GameExceptions.InvalidMoveException;
import com.rmbcorp.empire.API.Player;

public class PieceManager { // local; consider this class a helper of ViewController; should this class be an object?

	public static final byte GENERAL_ERROR = 0;
	public static final byte START_IS_FINISH = 1;
	public static final byte CANNOT_STACK = 2;
	public static final byte BAD_TERRAIN_TYPE = 3;
	public static final byte TOO_FAR = 4;
	public static final byte SUCCESS = 100;
	
	/** Instructs back end to create one GamePiece for player at location (namely a City)
	 * Handles InvalidMoveException by returning false.
	 * @param startLocation is the desired starting tile
	 * @param p1 is the player recipient
	 * @return true if piece is created, added to board, and given to player without incident
	 */
	public static boolean create(GameTile startLocation, Player p1) {
		GamePiece gp = new GamePiece(startLocation, p1); // should create a city
		p1.givePiece(gp);
		return true;
	}
	
	/** Instructs back end to create one GameUnit for player at location (a City is not currently a valid argument)
	 * Handles InvalidMoveException by returning false.
	 * @param startLocation is the desired starting tile
	 * @param p1 is the player recipient
	 * @param type is the type of unit to be created.  See GameUnit.java
	 * @return true if piece is created, added to board, and given to player without incident
	 */
	public static boolean create(GameTile startLocation, Player p1, byte type) {
		try {
			GamePiece gp = new GameUnit(startLocation, p1, type);
			p1.givePiece(gp);
		} catch (InvalidMoveException e) {
			System.out.println("Invalid Move");
			return false;
		}
		return true;
	}
	
	/** Instructs back end to execute a move of one GameUnit (a City is not a valid argument)
	 * Handles InvalidMoveException by returning false.
	 * UPDATE: was good for testing purposes.  Now, we want move orders, rather than execute-on-click. i.e.... try to deprecate this
	 * @param g is the unit to be moved
	 * @param finish is the desired tile
	 * @return true if move executed without incident OR if origin and destination are the same tile.
	 */
	public static boolean movePiece(int x0, int y0, int x1, int y1, Player player) {
		GameTile start = Controller.getController().retrieve(x0,y0);
		GameTile finish = Controller.getController().retrieve(x1,y1);
		if(start.equals(finish)) {
			System.out.println("Origin equals destination. No action taken.");
			return false;
		}
		
		GameUnit unit;
		if (start.gContents(player) != null) {
			unit = ((GameUnit) start.gContents(player));
		}
		else if (start.airContents(player).size() > 0) {
			unit = (GameUnit) start.airContents(player).get(0);
		}
		else return false;
		
		// now try to move piece
		unit.moveTo(finish);

		if (!start.equals(unit.location())) { // implementation is GameTile-based
			//moveHistory.add()
			return true;
		}
		System.out.println("Piece remains at original location.");
		return false;
	}
	
}
