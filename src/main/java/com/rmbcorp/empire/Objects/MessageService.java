package com.rmbcorp.empire.Objects;

import com.rmbcorp.empire.API.Player;
import com.rmbcorp.empire.API.PlayerID;

import java.util.ArrayList;

public class MessageService {
	Player[] players;
	ArrayList<String[]> log; // String[] must be length 3; first index is id.toString(), second is message, third is time
	
	MessageService(Player[] players) {
		this.players = players;
		log = new ArrayList<String[]>();
	}
	/** appends message with "PX: ", where X is playerID **/
	boolean send(String message, PlayerID from, PlayerID to) {
		for (Player player : players) {
			if (player.id().equals(to)) {
				player.sendMsg("P" + from.toString() + ": " + message);
				log.add(new String[] { from.toString(), message, String.valueOf(System.currentTimeMillis()) });
			}
		}
		return true;
	}
	
	ArrayList<String> requestLog(PlayerID requester) { // does not include time-stamp info
		ArrayList<String> output = new ArrayList<String>();
		for (Player player : players) {
			if (player.id().equals(requester)) {
				for (String[] q : log) {
					if (q[0].contains(requester.toString()))
						output.add(q[1]);
				}
			}
		}
		return output;
	}
}
