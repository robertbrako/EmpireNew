package com.rmbcorp.empire.API;

import com.rmbcorp.empire.Objects.GamePiece;

abstract public class BasicPlayer implements Player {

	protected BasicPlayerID id;
	
	@Override
	public PlayerID id() {
		return id;
	}

	@Override
	public boolean equals(Player that) {
		if(that==null) {
			return false;
		}
		return id.equals(that.id());
	}

	@Override
	public abstract void sendMsg(String msg);
	
	@Override
	public abstract void conductTurn();
	
	public abstract void givePiece(GamePiece p);
}
