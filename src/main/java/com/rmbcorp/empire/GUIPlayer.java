/**
 * 
 */
package com.rmbcorp.empire;

import java.util.ArrayList;
import com.Empire.EmpireNew.API.Piece;
import com.Empire.EmpireNew.Objects.GenericPlayer;
import com.Empire.EmpireNew.Objects.GenericPlayerID;
import com.Empire.EmpireNew.Views.MainBarView;

/**Client version has two implementations of GenericPlayer: this one represents player himself/herself.
 * Other one is AIPlayer, though I think I could make it a stand-in for remote players.
 * @author Aspire
 *
 */
public final class GUIPlayer extends GenericPlayer {
	//private List<Piece> pieces;
	private MainBarView mbv;
	private int turnNumber = 0; // conductTurn should be only thing to modify this
	
	public GUIPlayer() {
		id = new GenericPlayerID();
		pieces = new ArrayList<Piece>();
	}
	public void init(MainBarView mbv) {
		this.mbv = mbv;
	}
	
	/** Send message to this player's GUI
	 * @see com.Empire.EmpireNew.API.Player#sendMsg(java.lang.String)
	 */
	@Override
	public void sendMsg(String msg) {
		// remember to split msg into 3 if needed
		mbv.setText(new String[] { msg });
	}

	/* (non-Javadoc)
	 * @see com.Empire.EmpireNew.API.Player#conductTurn()
	 */
	@Override
	public void conductTurn() {
		turnNumber++;
		mbv.updateTurnNumber(turnNumber);
	}
	
	int getTurnNumber() {
		return turnNumber;
	}

	@Override
	public void givePiece(Piece p) {
		pieces.add(p);
	}
	/*
	@Override
	public SaveData save() {
		return super();
	}
*/
}
