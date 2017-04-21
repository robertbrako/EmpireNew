package com.rmbcorp.empire.AI;

import com.rmbcorp.empire.Objects.GenericPlayerID;

import java.util.ArrayList;

public final class EmotionalStates {
	/* These variables should generally range from [0.0 to 1.0) */
	
	/** keep track of players **/
	private ArrayList<GenericPlayerID> players;
	/** Indicates rage towards a specific player.  Rage is not necessary to attack, but it can change things up a bit **/
	private float[] RAGE;
	
	/** Constructor; players[] should not include self, lest AI can become self-hating.  That sounds interesting though... **/
	public EmotionalStates (GenericPlayerID[] players) {
		this(convertArrayList(players));
	}
	public EmotionalStates (ArrayList<GenericPlayerID> players) {
		this.players = players;
		//this.players.get(0).toString();
		RAGE = new float[players.size()];
		for (int i=0; i<players.size(); i++) {
			RAGE[i] = 0.0f;
		}
	}
	/** Puts AI on bad terms with one player.  Feel free to use sendMsg after calling this. **/
	public void getPissedAt(GenericPlayerID target) {
		for (GenericPlayerID player : players) {
			RAGE[players.indexOf(player)] = (player.equals(target)) ? 0.99f : 0f;
		}
	}
	
	private static <T> ArrayList<T> convertArrayList(T[] obj) {
		ArrayList<T> temp = new ArrayList<T>();
		for (int i=0; i<obj.length; i++) {
			temp.add(obj[i]);
		}
		return temp;
	}
}
