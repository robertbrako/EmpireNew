/**
 * 
 */
package com.Empire.EmpireNew;

/**Enforces game rules on client side so that they can place orders that a game server won't reject.
 * Quite likely that this class will be split up (possibly converted into an abstract or interface).
 * Class is hard-coded with same vars as backend; probably should request the vars from backend instead.
 * @author Aspire
 *
 */
public final class RuleManager {
	// these are user copies of the PieceType unit constants.  Client can hack if they want, but it won't do any good
	public static final byte ARMY = 2;
	public static final byte FIGHTER = 3;
	public static final byte BOMBER = 4;
	public static final byte SATELLITE = 5;
		
	public static final byte PATROL_BOAT = 6;
	public static final byte TRANSPORT = 7;
	public static final byte DESTROYER = 8;
	public static final byte SUBMARINE = 9;
	public static final byte BATTLESHIP = 10;
	public static final byte CARRIER = 11;
		
	public static final byte CITY = 1;
	
	private static final byte AIR = 3;
	private static final byte LAND = 2;
	private static final byte SEA = 1;

	
	private static final byte MOVES = 1;
	//private static final byte HITS = 2;
	//private static final byte POWER = 3;
	private static final byte TERRAIN = 4;
	//private static final byte FUEL = 5;
	//private static final byte COST = 6;
	
	private static byte[][] typeMatrix = { // values, such as cost, cannot exceed 127.
			new byte[] { FIGHTER, 8, 1, 1, AIR, 32, 10 },	// typeMatrix[0] is Fighter[]
			new byte[] { ARMY, 1, 1, 1, LAND, 0, 5 },		// typeMatrix[1] is Army[]
			new byte[] { BOMBER, 5, 2, 4, AIR, 32, 25 },	// typeMatrix[2] is Bomber[]
			new byte[] { PATROL_BOAT, 4, 1, 1, SEA, 0, 15 },
			new byte[] { TRANSPORT, 2, 1, 1, SEA, 0, 30 },	// typeMatrix[x][0] = type
			new byte[] { DESTROYER, 2, 3, 1, SEA, 0, 20 },	// typeMatrix[x][1] = movesPerTurn
			new byte[] { SUBMARINE, 2, 2, 3, SEA, 0, 20 },	// typeMatrix[x][2] = hits
			new byte[] { BATTLESHIP, 2, 10, 2, SEA, 0, 40 },// typeMatrix[x][3] = power
			new byte[] { CARRIER, 2, 8, 1, SEA, 0, 30 },	// typeMatrix[x][4] = terrain
			new byte[] { SATELLITE, 10, 0, 0, AIR, 0, 50 }	// typeMatrix[x][5] = fuel
	};														// typeMatrix[x][6] = cost
	/** Move piece from A to B.  Simple, right?
	 * 1. If moving air & ground to same destination, pieces will stack.
	 * 2. If stacked, only ground will move, though air stats display
	 * 3. 
	 * @param workingUnitType code that indicates desired unit, as above (e.g. ARMY = 38)
	 * @param fromX x coordinate of selected unit
	 * @param fromY y coordinate of selected unit
	 * @param toX x coordinate of destination tile
	 * @param toY y coordinate of destination tile
	 * @param mapData [ visibility data, ground unit data, air unit data ] at dest tile
	 * @return true if piece moved successfully, false otherwise.
	 */
	public static boolean movePiece(byte workingUnitType, int fromX, int fromY, int toX, int toY, byte[] mapData) {
		//first, check if dest is within range
		int distAllowed = 0;
		distAllowed = getData(workingUnitType, MOVES);

		// in-range routine
		int[] allowables = getAllowables(fromX, fromY, distAllowed);
		boolean success = false;
		for(int i=0; i<allowables.length; i+=2) {
			if(allowables[i] == toX && allowables[i+1] == toY) {
				success = true;
			}
		}
		if (success != true)
			return false;
		//second, check terrain type compatibility
		if (mapData[0] != getData(workingUnitType, TERRAIN) && getData(workingUnitType, TERRAIN) != AIR) {
			return false;
		}
		else { } // terrain check passed
		
		//last, check for units, if I feel like it
		if (mapData[1] > 0 || mapData[2] > 0)
			return false; // let's just say don't mix any units for now
		return true;
	}
	/** A substantial routine for getting which tiles a piece can presently move to
	 * @param originX selected piece's x location
	 * @param originY selected piece's y location
	 * @param distAllowed number of tiles that selected piece can move per turn
	 * @return int matrix of appropriate tiles; contents are a list of x,y pairs for the acceptable tiles
	 */
	public static int[] getValidTiles(int originX, int originY, int distAllowed) {
		return getAllowables(originX, originY, distAllowed);
	}
	
	private static int[] getAllowables(int fromX, int fromY, int distAllowed) {
		int[] allowables = new int[8];
		int aCount = 0;
		int currX = fromX;
		int currY = fromY-1;
		int mod = currX%2; // even row produces 0, odd row produces 1
		int[] dirX = new int[] { -1, 0, 1, 1, 0, -1};
		int[] dirY = new int[] { mod, 1, mod, -1+mod, -1, -1+mod};
		
		for (int i=0; i<distAllowed; i++) {
			for (int j=0; j<6; j++) {
				for (int k=0;k<=i;k++) {
					mod = (currX < 0) ? (-currX)%2 : (currX)%2;
					dirY[0] = mod;
					dirY[2] = mod;
					dirY[3] = -1+mod;
					dirY[5] = -1+mod;
					currX+=dirX[j];
					currY+=dirY[j];
					try {
						allowables[aCount] = currX;
						allowables[aCount+1] = currY;
					} catch (ArrayIndexOutOfBoundsException e) {
						int[] newList = new int[allowables.length*2];
						for (int l=0;l<allowables.length;l++) {
							newList[l] = allowables[l];
						}
						newList[aCount] = currX;
						newList[aCount+1] = currY;
						allowables = newList;
					}
					aCount+=2;
				}
			}
			currY--;
		}
		// trim array before returning
		int[] newList2 = new int[aCount];
		for (int m=0; m<aCount; m++) {
			newList2[m] = allowables[m];
		}
		allowables = newList2;
		return allowables;
	}
	/** Queries type data and returns int.  Returns 0 if miss
	 * @param unitType (byte) code for unit.  E.g. ARMY = 38.  See public fields at top of this class.
	 * @param data (byte) code for data sought.  E.g. TERRAIN = 4.  Fields at top should probably change from private to public.
	 * @return (int) requested data, or 0 if a parameter received was no good
	 */
	public static int getIntData(byte unitType, byte data) {
		return getData(unitType, data);
	}
	
	private static byte getData(byte unitType, byte data) {
		byte result = 0;
		for (int i=0; i<typeMatrix.length; i++) {
			if (typeMatrix[i][0] == unitType) {
				result = typeMatrix[i][data];
				System.out.println("Retrieved unit typeId " + typeMatrix[i][0]);
			}
		}
		return result;
	}
}
