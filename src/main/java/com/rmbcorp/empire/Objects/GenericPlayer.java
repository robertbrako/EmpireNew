package com.rmbcorp.empire.Objects;

import java.util.List;

import com.Empire.EmpireNew.AIPlayer;
import com.Empire.EmpireNew.API.Piece;
import com.Empire.EmpireNew.API.Player;
import com.Empire.EmpireNew.util.ClientUtils;
import com.Empire.EmpireNew.util.SaveData;

public abstract class GenericPlayer implements Player {
	protected GenericPlayerID id;
	
	@Override
	public GenericPlayerID id() {
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
	
	//public abstract void connectToPlayers();
	
	protected List<Piece> pieces;
	
	@Override
	/* Example output:
	 * (non-Javadoc)
	 * 1
	 * 33ARMY1
	 * 16BOMBER2
	 * 
	 * @see com.Empire.EmpireNew.API.Player#save()
	 */
	public SaveData save() {
		if (pieces.size() <= 0)
			return null; // don't ultimately want this; if player loses pieces/"dies", we still want to keep track of history
		SaveData sd = new SaveData();
		StringBuilder result = new StringBuilder(id.toString() + "\n");
		for (Piece p : pieces) {
			result.append(
					p.location().id().toString() +
					ClientUtils.interpret(p.type())
					);
			if (p.isUnit()) {
				result.append(
						GameUnit.getIntData(p.type(), "hits") // this gets the generic hits, not the HP remaining (yet)
						);
			}
			result.append("\n");
		}
		/** don't use the below snippet, just remember to think about save data differing slightly among subclasses
		 * And don't specifically reference AIPlayer - GenericPlayer shouldn't know the subclasses.
		 **/
		if (this.getClass().isInstance(AIPlayer.class)) {
			//result.append(((AIPlayer)this).getAIStateAsString()); // function getAIStateAsString does not exist... just an example
		}
		
		sd = new SaveData(result);
		return sd;
	}
}
