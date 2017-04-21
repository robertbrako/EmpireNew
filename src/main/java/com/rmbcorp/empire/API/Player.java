package com.rmbcorp.empire.API;

import com.Empire.EmpireNew.util.SaveData;

public interface Player {
	
	/** return id object for this player **/
	public PlayerID id();
	
	/** Description of equals(Player that)
	 * @param that is Player to be compared to
	 * @return true if same PlayerID, false otherwise
	 */
	public boolean equals(Player that);
	
	/** Game must be able to give piece to player **/
	public void givePiece(Piece piece);
	
	/** Player must be able to receive messages **/
	public void sendMsg(String msg);
	
	/** Tells AI to conduct turn
	 * At minimum, each player (AI or real) must increment their copy of the turn number 
	 **/
	public void conductTurn();
	
	/** collect any persistent data fields and put them into a SaveData object for export **/
	public SaveData save();
}
