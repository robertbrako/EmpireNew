package com.rmbcorp.empire;

import com.rmbcorp.empire.AI.EmotionalStates;
import com.rmbcorp.empire.AI.Surveyor;
import com.rmbcorp.empire.API.Piece;
import com.rmbcorp.empire.Objects.Controller;
import com.rmbcorp.empire.Objects.GenericPlayer;
import com.rmbcorp.empire.Objects.GenericPlayerID;
import com.rmbcorp.empire.Objects.PieceManager;
import com.rmbcorp.empire.util.SaveData;

import java.util.ArrayList;

public final class AIPlayer extends GenericPlayer { // maybe make un-final and allow folks to extend

	private ArrayList<Piece> pieces;
	private ArrayList<GenericPlayerID> otherPlayers;
	private EmotionalStates emo;
	private Surveyor surveyor;
	private OrderQueue orderQueue;
	private int turnNumberCopy = 0;

	public AIPlayer() {
		id = new GenericPlayerID();
		pieces = new ArrayList<Piece>();
		//otherPlayers = linkToPlayers(); // too soon
		//emo = new EmotionalStates(otherPlayers);
		surveyor = new Surveyor();
		
		orderQueue = new OrderQueue();
	}
	
	public void connectToPlayers() {
		otherPlayers = linkToPlayers();
		emo = new EmotionalStates(otherPlayers);
	}
	/** give piece to AI.  Simply takes without asking questions. **/
	@Override
	public void givePiece(Piece p) {
		pieces.add(p);
	}
	/** Message must begin with PlayerID.toString() in order to receive response **/
	@Override
	public void sendMsg(String msg) {
		interpret(msg);
	}
	/** submit orders: system calls this after all players submit; thread this if AI gets big **/
	public void submitOrders() {
		if (turnNumberCopy == 0) {
			byte[][][] mapData = Controller.getController().requestMapData(this);
			surveyor.initialSurvey(mapData, id.toInt());
		}
		//prepare orders; we'll need a big system for this; the following is pretty much all a hack for prelim testing
		int[] newArmy = surveyor.armyAt();
		if (PieceManager.movePiece(newArmy[0], newArmy[1], newArmy[0], newArmy[1]+1, this) == true) {
			//int[] moveOrder = new int[] { newArmy[0], newArmy[1], newArmy[0], newArmy[1]+1 }; // order to move over until impossible
			//using orderQueue would be redundant
			//orderQueue.add("move", new byte[] { surveyor.army() }, moveOrder, null);
			surveyor.armySet(newArmy[0], newArmy[1]+1);
			System.out.println("AI moved piece successfully right!");
		}
		else {
			if (PieceManager.movePiece(newArmy[0], newArmy[1], newArmy[0]+1, newArmy[1], this) == true) {
				//int[] moveOrder = new int[] { newArmy[0], newArmy[1], newArmy[0]+1, newArmy[1] }; // order to move over until impossible
				//orderQueue.add("move", new byte[] { surveyor.army() }, moveOrder, null);
				surveyor.armySet(newArmy[0]+1, newArmy[1]);
				System.out.println("AI moved piece successfully down!");
			}
			else {
				if (PieceManager.movePiece(newArmy[0], newArmy[1], newArmy[0], newArmy[1]-1, this) == true) {
					//int[] moveOrder = new int[] { newArmy[0], newArmy[1], newArmy[0], newArmy[1]-1 }; // order to move over until impossible
					//orderQueue.add("move", new byte[] { surveyor.army() }, moveOrder, null);
					surveyor.armySet(newArmy[0], newArmy[1]-1);
					System.out.println("AI moved piece successfully left!");
				}
			}
		}
		//send orders
		Controller.getController().processOrders(orderQueue.orderNames, orderQueue.byteArgs, orderQueue.intArgs, orderQueue.stringArgs, this);
		//if (surveyor.armyAt()[0] != newArmy[0] || surveyor.armyAt()[1] != newArmy[1])
		//	surveyor.armySet(newArmy[0], newArmy[1]+1); // possibly problematic... need some analysis
	}
	/** Called by Controller after processing orders and increasing turn number **/
	@Override
	public void conductTurn() {
		turnNumberCopy++;
	}
	
	private ArrayList<GenericPlayerID> linkToPlayers() { // depends on Controller.getPlayerID(int index)
		int length = 2; //Controller.getController().getPlayerCount(); // collect into an InitialOrders collection
		ArrayList<GenericPlayerID> players = new ArrayList<GenericPlayerID>();
		for (int i=0; i<length;i++) {
			GenericPlayerID temp = (GenericPlayerID) Controller.getController().getPlayerID(i); // collect into an InitialOrders collection
			if (!temp.equals(id)) {
				players.add(temp);
				System.out.println("AI recognizing player @" + i);
			}
			else
				System.out.println("CAUTION: self is id="+temp.toString()+", my id="+id.toString()+"!");
		}
		return players;
	}
	
	private void interpret(String msg) {
		// first identify sender; consider making an id for system messages if useful
		GenericPlayerID sender = null;
		for (int i=0; i<otherPlayers.size();i++) {
			if (msg.startsWith("P" + otherPlayers.get(i).toString())) {
				sender = otherPlayers.get(i);
			}
		}
		// if sender was found, address user messages
		if (sender != null) {
			String message = "AI gives you default response.";
			if (msg.contains("screw you")) {
				emo.getPissedAt(sender);
				message = "AI is pissed off at you.";
			}
			Controller.getController().sendMsg(message, id, sender); // send to process orders
		}
		// otherwise address system messages; startsWith is important, because user messages should always have prefixes
		else {
			if (msg.startsWith("submitOrders")) {
				submitOrders();
				return; // obviously, return is not currently needed
			}
		}
	}

	@Override
	public SaveData save() {
		// TODO Auto-generated method stub
		return new SaveData();
	}
}
