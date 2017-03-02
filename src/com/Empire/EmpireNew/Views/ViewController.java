package com.Empire.EmpireNew.Views;

import com.Empire.EmpireNew.GUIPlayer;
import com.Empire.EmpireNew.OrderQueue;
import com.Empire.EmpireNew.RuleManager;
import com.Empire.EmpireNew.Objects.Controller;
import com.Empire.EmpireNew.Objects.GameUnit;
import com.Empire.EmpireNew.util.ClientUtils;
import com.Empire.EmpireNew.util.ClientUtils.Flag;
import com.Empire.EmpireNew.util.Numbers;

/** After considerable pondering and research, ViewController's duties have shrunk to the following:<br>
 * 1. Responding to (client) navigation events and data-request events<br>
 * 2. Providing application-flow logic (in response to navigation events)<br>
 * 3. Delegating data-requests to the appropriate model object (via Controller in backend)<br>
 * * adapted from "HMVC: The layered pattern for developing strong client tiers",<br>
 * * http://www.javaworld.com/article/2076128/design-patterns/hmvc--the-layered-pattern-for-developing-strong-client-tiers.html<br>
 * <br>(despite the duties shrinking, the class somehow grew in size.  Oops.)<br>
 * Think of ViewController as a client-side controller, which makes requests of Controller (server-side).
 * @author robertbrako
 *
 */
public final class ViewController {
	
	private static int status = 0;
	private static boolean highlightHex = false;
	private static boolean popup = true;
	private static boolean options = false;
	
	private static float[] currYesXBounds, currNoXBounds, currYesNoYBounds;
	private static int[] currOrigin, currDimensions;
	private static float currHexSize;
	private static int currWidth, currHeight;
	
	private static int[] workingPiece = new int[2];
	private static int[] workingTile = new int[2];
	
	private static ViewController thisVC;
	private BasicGridView bgv;
	private MainBarView mbv;
	private GUIPlayer player;
	private OrderQueue orderQueue;
	
	private byte[][][] mapData;
	private int[] selection = { Numbers.NULL, Numbers.NULL }; // eventually tiles may be added with negative coordinates, so -1 won't do.
	
	/** Public Constructor/Accessor **/
	public static ViewController getViewController() {
		if (thisVC == null) {
			thisVC = new ViewController();
		}
		return thisVC;
	}
	/** An initialization supplement to link to the two View subclasses **/
	public void init(BasicGridView bgView, MainBarView mbView, GUIPlayer pl) {
		bgv = bgView;
		mbv = mbView;
		player = pl;
		player.init(mbv);
	}
	
	private ViewController() {
		orderQueue = new OrderQueue();
	}
	/** Um... how to explain this beast...
	 * ActionUp determines game state, reacts to click accordingly, and updates game state.  I guess that wasn't so hard to explain.
	 * The flow logic has been simplified, if you can believe that.
	 * @param x the raw x coord of where user clicked in BGV
	 * @param y the raw y coord of where user clicked in BGV
	 */
	public void bgvActionUp(float x, float y) {
		if (getStatus() == 0) {
			bgv.setMsg("Go through tutorial?");
			bgv.setYesNo(true);
			setStatus(1);
		}
		else if (getStatus() == 1) { //respond only to yes/no click (treats outside clicks as no)
			int decision = Locator.decide(currYesXBounds, currNoXBounds, currYesNoYBounds, x, y);
			if (decision == 1) {
				setInfo(new String[] { "Apologies, no tutorial present." });
			}
			else setInfo(new String[] { "Skipping tutorial." });
			popup = false;
			setStatus(2);
		}
		/* * VC vars modified in status 2: (point of this is to lay groundwork for a re-org)
		 * selection[0],selection[1], workingTile[0], workingTile[1], workingPiece[0], workingPiece[1]
		 * highlightHex, options, Controller.getController().getStatus()
		 * * External method calls:
		 * bgv.showMoveable(), bgv.select(), mbv.setText (via showInfo)
		 * * VC vars accessed, not modified:
		 * unitData
		*/
		else if (getStatus() == 2) {
			int[] coords = Locator.locate(currOrigin, currDimensions, currHexSize, x, y); // not sure if currDimensions needs more protection
			if (coords[0] != Numbers.NULL) {
				if (selection[0] == coords[0] && selection[1] == coords[1]) {
					highlightHex = false;
					deselect(); // change selection so that tests don't prevent you from re-selecting tile
					bgv.showMoveable(Numbers.NULL, Numbers.NULL, -1); // if we were at unit placement, remember to unhighlight moveable tiles
					options = false; // close options menu if it was open
					setInfo(new String[] {"Deselected Tile"});
				}
				else {
					selection[0] = coords[0];
					selection[1] = coords[1];
					highlightHex = true;
					bgv.select(coords[0], coords[1]);
					if (getGround(coords[0], coords[1]) != -1 && air(coords[0], coords[1]) != false) {
						//process to choose air or ground unit for the tile
						bgv.setMsg("Choose ground unit?");
						bgv.setYesNo(true);
						popup = true;
						setStatus(6);
					}
					else if (getGround(coords[0], coords[1]) != -1 || air(coords[0], coords[1]) != false) { //getGround now checks ownership
						byte groundUnit = getGround(coords[0], coords[1]);
						byte[] airUnit = getAir(coords[0], coords[1]); // airUnit is [typeData, player]
						// bgv actions if unit is present: show options menu if player owns piece
						String unitType = "", data1 = "", data2 = "";
						workingTile[0] = selection[0];
						workingTile[1] = selection[1]; // WARNING: be sure selection[] is the immediately preceding selection
						if (groundUnit != -1) {
							setWorkingPiece(selection[0], selection[1]);
							// mbv actions if unit is present
							unitType = "Unit Type: " + ClientUtils.interpret((byte) (groundUnit%32));
							data1 = "Moves Per Turn: " + GameUnit.getIntData((byte) (groundUnit%32), "movesPerTurn");
							data2 = "Current location: " + coords[0] + ", " + coords[1] + ".";
							if (player.id().toInt().equals(mapData[selection[0]][selection[1]][1]/32)) {
								options = true;
								setStatus(3);
							}	
							else
								data2 = "Err: PID " + player.id().toInt() + " but owner is " + mapData[selection[0]][selection[1]][1]/32 + "!";
						}
						if (air(workingTile[0], workingTile[1]) == true) { // WARNING: overrides ground unit!
							try {
								setWorkingPiece(selection[0], selection[1]);
								// mbv actions if unit is present
								unitType = "Unit Type: " + ClientUtils.interpret(airUnit[0]) + ", Player: " + airUnit[1];
								data1 = "Moves Per Turn: " + GameUnit.getIntData(airUnit[0], "movesPerTurn");
								data2 = "Current location: " + coords[0] + ", " + coords[1] + ".";
							} catch (IndexOutOfBoundsException ex) {
								unitType = "Unit Type: Exception";
								data1 = "Moves Per Turn: ";
								data2 = "Current location: ";
							}
							if (player.id().toInt().equals(mapData[selection[0]][selection[1]][2]/32)) {
								options = true;
								setStatus(3);
							} // else don't show (usual) options menu
						}
						setInfo(new String[] { unitType, data1, data2 });
					}
					else { //(if ground is empty and air is empty); just terrain
						setInfo(new String[] {""});
						setStatus(2); // should be appropriate for #2, #3, and #4
						//closeOptions();
					}
				}
			} // end if (coords is something usable)
			else {
				setInfo(new String[] {"", "", ""});
				closeOptions();
			}
		}
		/* VC vars in status 3
		 * options, Controller.getController().getStatus(), highlightHex
		 * selection[0], selection[1], workingPiece[0], workingPiece[1]
		 * unitData
		 * bgv.showMoveable() mbv.setText()
		 * processOrders
		 * re-process this bgvActionUp()
		 */
		else if (getStatus() == 3) { // main objective: handle click within options menu
			switch(Locator.locateMenu(x, y, currWidth, currHeight)) {
			case 1: // sentry
				break;
			case 2: // move
				options = false;
				setStatus(4);
				if (mapData[workingPiece[0]][workingPiece[1]][1] > 0) {
					if ((byte) (mapData[workingPiece[0]][workingPiece[1]][1]%32) == RuleManager.CITY) {
						setInfo(new String[] { "Cannot move City." });
						closeOptions();
					}
					else
						bgv.showMoveable(selection[0],selection[1], GameUnit.getIntData((byte) (mapData[workingPiece[0]][workingPiece[1]][1]%32), "movesPerTurn"));
				}
				else if (mapData[workingPiece[0]][workingPiece[1]][2] > 0)
					bgv.showMoveable(selection[0],selection[1], RuleManager.getIntData((byte) (mapData[workingPiece[0]][workingPiece[1]][2]%32), (byte) 1));
				else closeOptions();
				break;
			case 3://load // save(testing)
				Controller.getController().save();
				break;
			case 4://move to
				break;
			case 5://patrol //exec(testing)
				closeOptions();
				processOrders();
				break;
			case 6://close
				closeOptions();
				break;
			default: // if user didn't click in options space, proceed as if they're selecting a new tile
				closeOptions();
				setStatus(2);
				bgvActionUp(x,y); // exception probably comes here...
			}
		}
		else if (getStatus() == 4) { // try to place unit, otherwise exit mode and return to neutral
			int[] coords = Locator.locate(currOrigin, currDimensions, currHexSize, x, y);
			boolean moveable = false; // maybe move this up to a class var
			int fromX = workingPiece[0];
			int fromY = workingPiece[1];
			byte[] workingUnitType = mapData[fromX][fromY];
			if (fromX > Numbers.NULL && coords[0] != Numbers.NULL && workingUnitType[1] != RuleManager.CITY) {// CAUTION: applies to both air & ground
				moveable = RuleManager.movePiece(workingUnitType[1], fromX, fromY, coords[0], coords[1], mapData[coords[0]][coords[1]]);
				if (workingUnitType[2] > 0)
					moveable = RuleManager.movePiece(RuleManager.BOMBER, fromX, fromY, coords[0], coords[1], mapData[coords[0]][coords[1]]);
				// WARNING: implementation moves whatever piece is here, regardless of ground/sea/air
			}
			if (moveable) {
				// quick conflict check added:
				String conflict = "";
				int[] conflicts = orderQueue.checkDests(); // list of destination orders thus far
				for (int i=0; i<conflicts.length; i+=2) {
					if (coords[0] == conflicts[i] && coords[1] == conflicts[i+1])
						conflict = "Possible conflict";
				}
				if (workingUnitType[0] != 0) {
					orderQueue.add("move", new byte[] { workingUnitType[0] }, new int[] { fromX, fromY, coords[0], coords[1] }, null);
				}
				else
					orderQueue.add("move", new byte[] { RuleManager.BOMBER }, new int[] { fromX, fromY, coords[0], coords[1] }, null);
				//
				setInfo(new String[] {"Allowable move", "New move orders to " + coords[0] + "," + coords[1] +".", conflict});
			}
			else
				setInfo(new String[] {"Move not allowed", "", ""});
			//updateUnitVisibility(); // we shouldn't trigger actual move yet
			bgv.showMoveable(Numbers.NULL, Numbers.NULL, -1);
			clearWorkingPiece();
			setStatus(2);
			closeOptions(); // no chance to pick again if movePiece() returns false; user must reselect & try again
		}
		else if (getStatus() == 6) {
			int decision = Locator.decide(currYesXBounds, currNoXBounds, currYesNoYBounds, x, y);
			if (decision == 1) {
				//
			}
			else { // TODO: fix exception!  I managed to get a stack overflow, probably bouncing between status 2 and 6.
				deselect();
				//bgvActionUp(x, y); // what you really want is sthg like selectUnit(coords[]) or openOptionsMenu()
			}
			popup = false;
			setStatus(2);
		}
		bgv.invalidate();
	}
	/** Update local variables before processing click within yes/no dialog
	 * @param yesXBounds [beginning,end] of yes box in x direction
	 * @param noXBounds [beginning,end] of no box in x direction
	 * @param yesNoYBounds [beginning,end] of both yes and no box in y direction
	 */
	public static void updateYN(float[] yesXBounds, float[] noXBounds, float[] yesNoYBounds) { // used by BasicGridView
		currYesXBounds = yesXBounds;
		currNoXBounds = noXBounds;
		currYesNoYBounds = yesNoYBounds;
	}
	/** Update local variables before processing click within options menu, whose size depends on:
	 * @param width = current width of BasicGridView
	 * @param height = current height of BasicGridView
	 */
	public static void updateWH(int width, int height) { // used by BasicGridView
		currWidth = width;
		currHeight = height;
	}
	/** Update local variables before processing click within overall BGV area
	 * @param origin = current X0,Y0 within BGV
	 * @param dimensions = rows and columns (for now this is fixed, not really variable)
	 * @param hexSize = current hexSize from BGV, which is basis for zoom level
	 */
	public static void updateBoard(int[] origin, int[] dimensions, float hexSize) { // used by BasicGridView
		currOrigin = origin;
		currDimensions = dimensions;
		currHexSize = hexSize;
	}

	/** Used by BGV to know what to draw
	 * @param f the Flag to check { HIGHLIGHT, POPUP, OPTIONS }
	 * @return boolean of Flag's status
	 */
	public static boolean checkFlag(Flag f) { // used by BasicGridView
		switch (f) {
		case HIGHLIGHT:
			return highlightHex;
		case POPUP:
			return popup;
		case OPTIONS:
			return options;
		default:
			return false;
		}
	}
	/** Used at game-start and turn-change to update view with visible tiles **/
	public void updateTileVisibility() { // called by MainActivity on startup and internally upon unit placement/movement?
		if (mapData == null)
			mapData = Controller.getController().requestMapData(player);
		else {
			byte[][][] updatedData = Controller.getController().requestMapData(player); // can't be smaller size & shouldn't be larger
			for (int i=0; i<mapData.length; i++) {
				for (int j=0; j<mapData[i].length; j++) {
					if(mapData[i][j][0] == (byte) 0)
						mapData[i][j] = updatedData[i][j];
					// else leave terrain visibility data intact!
					// but, always update units:
					mapData[i][j][1] = updatedData[i][j][1];
					mapData[i][j][2] = updatedData[i][j][2];
				}
			}
		}
		bgv.setMapData(mapData);
	}
	/** Don't clone me! **/
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	/** Don't snoop around using toString ! **/
	@Override
	public String toString() {
		return "ViewController";
	}
	/** get current status **/
	public static int getStatus() {
		return status;
	}
	////////////////////////////////////////////// PRIVATE //////////////////////////////////
	private void setStatus(int newStatus) {
		status = newStatus;
	}
	
	private void processOrders() {
		Controller.getController().processOrders(orderQueue.orderNames, orderQueue.byteArgs, orderQueue.intArgs, orderQueue.stringArgs, player);
		orderQueue.clear();
		updateTileVisibility(); // should be called from Controller
		//bgv.invalidate();
	}
	
	private void setInfo(String[] text) {
		mbv.setText(text);
	}
	
	private void setWorkingPiece(int x, int y) {
		workingPiece[0] = x;
		workingPiece[1] = y;
	}
	
	private void clearWorkingPiece() {
		workingPiece[0] = Numbers.NULL;
		workingPiece[1] = Numbers.NULL;
	}
	
	private byte getGround(int arg0, int arg1) {
		return mapData[arg0][arg1][1];
	}
	
	private boolean air(int arg0, int arg1) {
		if (mapData[arg0][arg1][2] > 0) // CAUTION: doesn't distinguish players, but I think it's no problem yet
			return true;
		return false;
	}
	private byte[] getAir(int arg0, int arg1) { // returns [type & owner]
		byte[] result = { 0, 0 };
		if (mapData[arg0][arg1][2] > 0) {
			switch(mapData[arg0][arg1][2]%32) {
			case 3: // fighter
			case 4: // bomber
			case 5: // satellite
				result[0] = (byte) (mapData[arg0][arg1][2]%32);
				break;
			}
			result[1] = (byte) (mapData[arg0][arg1][2]/32);
		}
		return result;
	}
	/** at present closeOptions does not change status nor clear "selection": caller should adjust appropriately **/
	private void closeOptions() {
		highlightHex = false;
		//selection[0] = NULL; // deselect should be separate
		options = false;
		//setStatus(2);
	}
	
	private void deselect() {
		selection[0] = Numbers.NULL; // deselect should be separate
	}
}
