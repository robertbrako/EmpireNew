package com.rmbcorp.empire.Objects;

import java.util.ArrayList;

import com.Empire.EmpireNew.API.Player;
import com.Empire.EmpireNew.API.PlayerID;
import com.Empire.EmpireNew.util.HibernateSaver;
import com.Empire.EmpireNew.util.LocalSaver;
import com.Empire.EmpireNew.util.SaveData;
import com.Empire.EmpireNew.util.Saver;

/** Controller.
 * Handles global flow control, moreso than client's ViewController.
 * Provides messaging service between players; be careful not to anger the AI...
 * This should be only class (other than MainActivity for now) with direct access to Player objects
 * 
 * Thread/timing notes:
 * requestMapData provides info useful to Player and AI.
 * -can be done at beginning of turn, and data can be shared between User thread and AI thread
 * -access must close when turn is about to be executed
 * 
 * processOrders will wait until all orders "come in".  The tricky part is the coming in...
 * -A: do analysis, figure out maximum order size, and statically allocate space for each player's orders, or
 * -B: write an essentially foolproof routine that can withstand near-simultaneous submissions
 * 
 * In theory, message service shouldn't need to be halted, even during turn processing.
 * @author Aspire
 *
 */
public final class Controller {

	private static Controller thisC;
	private MessageService msvc;
	
	private BasicGrid bg;
	private Player[] players;
	private int rows, cols;
	private byte[] playerOrdersPending;
	
	private int playerCount = 0;
	private int turnNumber = 0;
	//iteration variables
	private int intI;
	private int intJ;
	private GameTile gt;
	private GameTile rt;
	
	public static Controller getController() {
		if (thisC == null) {
			thisC = new Controller();
		}
		return thisC;
	}
	
	private Controller() {
		
	}
	
	/** Assume ps contains [GUIPlayer,AIPlayer]
	 * @param bg ref to BasicGrid
	 * @param players ref to Initial Players
	 * @param rows number of rows game starts with
	 * @param cols number of columns game starts with
	 */
	public void init(BasicGrid bg, Player[] players, int rows, int cols) {
		this.bg = bg;
		this.players = players;
		this.playerCount = players.length; // said "this" here for consistency
		this.rows = rows;
		this.cols = cols;
		playerOrdersPending = new byte[] { 1, 1 }; // this is the number of human players; remember to modify
		msvc = new MessageService(players);
	}
	/** General method for processing orders.  Will probably need submethods
	 * No need to return anything; method must be foolproof.
	 * @param orderNames represents names of orders
	 * @param byteArgs are byte[][] arguments, if any
	 * @param intArgs are int[][] args, if any
	 * @param stringArgs are String[][] args, if any
	 */
	public void processOrders(ArrayList<String> orderNames, byte[][] byteArgs, int[][] intArgs, String[][] stringArgs, Player p) {
		// INCOMING PROCESSING
		int i = 0;
		for (Player player : players) {
			if (player.equals(p)) {
				for (int j=0; j<orderNames.size(); j++) {
					if (orderNames.get(j).contentEquals("move")) {
						PieceManager.movePiece(intArgs[j][0], intArgs[j][1], intArgs[j][2], intArgs[j][3], player);
						// need to analyze effect of multiple orders... and make this 100% less of a hack
					}
				}
				playerOrdersPending[i] = 0;
			}
			i++;
		}
		if (playerOrdersPending[0] + playerOrdersPending[1] == 1) { // need to test for all human players
			processAIOrders();
		}
		finalizeTurn();
	}
	/** private until determined otherwise **/
	private void processAIOrders() {
		for (int i=0; i<players.length; i++) {
			if (playerOrdersPending[i] == 1) { // sensitive test: assumes human(s) are 0 beforehand
				players[i].sendMsg("submitOrders"); // kind of a hack, but fairly reasonable
			}
		}
	}
	/** finalize turn **/
	private void finalizeTurn() {
		turnNumber++;
		for (int i=(players.length-1); i > -1; i--) {
			playerOrdersPending[i] = 1;
			players[i].conductTurn();
		}
	}
	/** Method for retrieving playerID from list of total players (human and AI)
	 * No bounds check is performed.
	 * @param index is index within array of the player sought
	 * @return player at the given index.
	 */
	public PlayerID getPlayerID(int index) { // change this or secure access
		return players[index].id();
	}
	/** Method for client to obtain number of players
	 * @return number of players known to Controller
	 */
	public int getPlayerCount() {
		return playerCount;
	}
	/** method to view copy of turn number **/
	public int getTurnNumber() {
		return Integer.valueOf(turnNumber);
	}
	/** Send message from one player to another **/
	public boolean sendMsg(String message, PlayerID fromA, PlayerID toB) {
		try {
			msvc.send(message, fromA, toB);
		}
		catch (Exception e) { return false; }
		return true;
	}
	/** returns null if coordinates are out of bounds
	 * @param x is x-coord of desired tile
	 * @param y is y-coord of desired tile
	 * @return the Tile located at (x,y) or null
	 **/
	public GameTile retrieve(int x, int y) {
		if (x < 0 || x > cols || y < 0 || y > rows)
			return null;
		return bg.atCoords(x, y);
	}
	/** saves all persistent objects
	 * -may want an OPTION parameter to alter output instructions
	 */
	public void save() {
		SaveData[][] results = new SaveData[rows][cols];
		Saver saver = HibernateSaver.getSaver(); 
		iterateStart();
		while(gt.nextG(1) != null) {
			results[intI][intJ] = gt.save();
			saver.add(results[intI][intJ]); // consider not using results[][] array
			iterateNext();
		}
		
		for (Player p : players) { // or instead of each player, just get client save
			saver.add(p.save());
		}
		boolean saveToServerFailed = !((HibernateSaver)saver).out();
		boolean saveToDiskFailed = true;
		if (saveToServerFailed) {
			Saver newSaver = LocalSaver.getSaver();
			saveToDiskFailed = !((LocalSaver)newSaver).out(); // is this ok?  will (HibernateSaver) data be preserved?
		}
		if (saveToDiskFailed) {
			System.err.println("Save failed!");
		}
		saver.close();
		
	}
	/** Game-begin/end-of-turn process for updating each player's map.
	 * Methinks java wants me to work with int, and then convert to bytes for mass upload/download.  We'll see.
	 * @param player player requesting data
	 * @return byte[][][], where each [row][col] contains the following array:
	 * 1. Terrain type byte (LAND=2, SEA=1, or UNKNOWN=0)
	 * 2. Byte where high 3 bits represent Player 0-6, and remaining 5 bits represent ground unit.  Yes, this is miserly for now.
	 * 3. Byte where high 3 bits represent Player 0-6, and remaining 5 bits represent air unit.
	 */
	public byte[][][] requestMapData(Player player) {	
		return requestMapData(player, true, true);
	}
	private byte[][][] requestMapData(Player player, boolean visibilityCheck, boolean unitCheck) {
		//possible security check
		byte[][][] results = new byte[rows][cols][3];
		iterateStart();
		while(gt.nextG(1) != null) { // I think this may as well be: while(true) { .. }.
			if (visibilityCheck) {
				results[intI][intJ][0] = (byte) gt.tileTerrain(player).ordinal();
			}
			if (unitCheck) {
				//first address ground units
				results[intI][intJ][1] = (gt.gContents(player) != null) ? (byte) ( Byte.parseByte(gt.gContents(player).owner().id().toString())*32+gt.gContents(player).type() ) : -1;
				//second address air units
				results[intI][intJ][2] = (gt.airContents(player) != null && gt.airContents(player).size() > 0) ? (byte)( Byte.parseByte(gt.airContents(player)
						.get(0).owner().id().toString())*32 +
						gt.airContents(player)
						.get(0).type() ) : -1;
			}
			iterateNext();
		} // end while
		return results;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException { // don't clone me!
		throw new CloneNotSupportedException();
	}
	
	@Override
	public String toString() {
		return "Controller";
	}
	
	private void iterateStart() { // Controller is only class permitted to iterate GameTile
		intI=0;
		intJ=0;
		gt = bg.originG();
		rt = gt; // track row-begin tile
	}
	
	private void iterateNext() {
		gt = (GameTile) gt.nextG(1);
		intJ++;
		
		if (gt.isBlank()) {
			int mod = (intI%2)*5;
			if (!rt.nextG(mod).isBlank()) {
				gt = (GameTile) rt.nextG(mod);
				rt = gt;
				intI++;
				intJ=0;
			} //else break;
		}
	}
	
	/** MessageService has been factored out **/ 
}
