package com.rmbcorp.empire.Objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

import com.rmbcorp.empire.API.BackendTile;
import com.rmbcorp.empire.API.GameExceptions;
import com.rmbcorp.empire.API.Player;
import com.rmbcorp.empire.API.Unit;
import com.rmbcorp.empire.API.GameExceptions.InvalidMoveException;

/**GameUnit represents the majority of in-game pieces and provides type data using the aptly-named TypeData inner class.
 * It contains a rudimentary algorithm for checking tiles reachable by a moveTo command via recursion.
 * As a math fan, I may sketch out some equations to perform that function instead.<br><br>
 * We need private data for each instance... probably do this soon!
 *   
 * @author Aspire
 *
 */
public class GameUnit extends GamePiece implements Unit {

	private static final Map<String,TypeData> typeData = createTypeData();
	
	@SuppressWarnings("synthetic-access")
	private static Map<String, TypeData> createTypeData() {
		Map<String, TypeData> result = new HashMap<String, TypeData>();
		result.put("FIGHTER", new TypeData('F', 8, 1, 1, Terrain.AIR, 32, 10));
		result.put("ARMY", new TypeData('A', 1, 1, 1, Terrain.LAND, 0, 5));
		result.put("BOMBER", new TypeData('O', 5, 2, 4, Terrain.AIR, 32, 25));
		result.put("PATROL_BOAT", new TypeData('P', 4, 1, 1, Terrain.SEA, 0, 15));
		result.put("TRANSPORT", new TypeData('T', 2, 1, 1, Terrain.SEA, 0, 30));
		result.put("DESTROYER", new TypeData('D', 2, 3, 1, Terrain.SEA, 0, 20));
		result.put("SUBMARINE", new TypeData('S', 2, 2, 3, Terrain.SEA, 0, 20));
		result.put("BATTLESHIP", new TypeData('B', 2, 10, 2, Terrain.SEA, 0, 40));
		result.put("CARRIER", new TypeData('C', 2, 8, 1, Terrain.SEA, 0, 30));
		result.put("SATELLITE", new TypeData('Z', 10, 0, 0, Terrain.AIR, 0, 50));
		return Collections.unmodifiableMap(result);
	}
	
	public GameUnit(BackendTile startLocation, Player ownerP, byte type) throws InvalidMoveException {
		location = startLocation;
		owner = ownerP;
		this.type = type;
		id = new GamePieceID();
		//try {
			location.addUnit(this);
		//} catch(InvalidMoveException ex) { }
			// fail silently...unit just won't be added to list at that space, making this buggy but acceptable
			//EDIT: removed this try/catch; PieceManager will handle the exceptions.
	}
	
	public static String interpret(byte type) { // same as interpret() in ClientUtil; duplicated for access control reasons
		if (type == PieceType.ARMY)
			return "ARMY";
		else if (type == PieceType.BATTLESHIP)
			return "BATTLESHIP";
		else if (type == PieceType.BOMBER)
			return "BOMBER";
		else if (type == PieceType.CARRIER)
			return "CARRIER";
		else if (type == PieceType.CITY)
			return "CITY";
		else if (type == PieceType.DESTROYER)
			return "DESTROYER";
		else if (type == PieceType.FIGHTER)
			return "FIGHTER";
		else if (type == PieceType.PATROL_BOAT)
			return "PATROL_BOAT";
		else if (type == PieceType.SATELLITE)
			return "SATELLITE";
		else if (type == PieceType.SUBMARINE)
			return "SUBMARINE";
		else if (type == PieceType.TRANSPORT)
			return "TRANSPORT";
		return "";
	}
	
	/** Umm...
	 * 
	 * @param type a constant you can get from PieceType
	 * @param search is one of: "productionCost, fuel, power, hits, movesPerTurn, symbol"
	 * @return int version of data, or -999000 upon failure (please change to Numbers.NULL)
	 */
	public static int getIntData(byte type, String search) {
		if (type == PieceType.CITY) //I need to shift some things around...
			return 9999;
		if (type <= 0)
			return -1; // shouldn't I return -999000?
		String key = interpret(type);
		switch (search) {
			case "productionCost":
				return typeData.get(key).productionCost;
			case "fuel":
				return typeData.get(key).fuel;
			case "power":
				return typeData.get(key).power;
			case "hits":
				return typeData.get(key).hits;
			case "movesPerTurn":
				return typeData.get(key).movesPerTurn;
			case "symbol":
				return typeData.get(key).symbol;
		}
		return -999000;
	}
	
	public byte getType() {
		return this.type;
	}
	
	@Override
	public boolean isMobile() {
		return true;
	}
	
	@Override
	public boolean isUnit() {
		return true;
	}
	
	public void moveTo(BackendTile destination) {
		if (!inRange((GameTile) destination)) {
			System.out.println("Out of range Error");
			return;
		}
		try {
			destination.addUnit(this);
		} catch(GameExceptions.InvalidMoveException ex) {
			return; //silent failure here, but prevents fall-through to partial move 
		}
		BackendTile oldLocation = location;
		location = destination;
		oldLocation.removeUnit(this);
	}
	
	@Override
	public Terrain terrain() {
		return typeData.get(interpret(this.type)).canTravel;
	}
	
	@Override
	public GamePiece executeNextOrder() { // necessary to override GamePiece.executeNextOrder() because GamePiece lacks moveTo
		if(orders.isEmpty()) { // to avoid out of range exceptions
			return null;
		}
		if(orders.get(0)==null) {
			return null;
		}
		if(orders.get(0).action(owner) == 8 && isMobile()) { // need code for "Goto"
			// TODO: make this move the unit only one tile, calculated with the shortest-path algorithm
			
		} else if(orders.get(0).action(owner) == 7 && isMobile()) { // need code for "MoveDir"
			BackendTile adjacent = location.nextG(Integer.valueOf(orders.get(0).target(owner)));
			if(adjacent!=null) {
				if(adjacent.isBlank()) {
					// fail silently
				} else {
					this.moveTo(adjacent);
				}
			}
		} else {
			orders.remove(0); // pop the order if it's not meaningful
		}
		// orders.remove(0); // pop the next order off whether it's meaningful or not
		return null;
	}
	
	private boolean inRange(GameTile target) {
		int i = typeData.get(interpret(type)).movesPerTurn;
		ArrayList<GameTile> range = new ArrayList<GameTile>();
		range.add((GameTile) this.location());
		populateRange(range, i);
		for (GameTile b : range) {
			//System.out.println("Current id " + b.id().toString() + ", target is " + target.id().toString());
			if (b.id().equals(target.id()))
				return true;
		}
		return false;
	}
	
	private void populateRange(ArrayList<GameTile> subject, int iterations) { // GameTile-based implementation
		if (iterations <= 0)
			return;
		int length = subject.size();
		for (int i=0; i< length; i++) {
			for (int k=0; k<6; k++) {
				if (!subject.get(i).nextG(k).isBlank() && !subject.contains(subject.get(i).nextG(k)))
					subject.add((GameTile) subject.get(i).nextG(k)); // see if this is infinite...
			}
		}
		populateRange(subject, iterations-1);
	}
	/** Takes byte code and outputs string label; will return "" if no match
	 * 
	 * @param type the byte code for the unit
	 * @return String representation of the unit's type
	 */
	
	private static class TypeData {
		public char symbol;
		public int movesPerTurn;
		public int hits;
		public int power;
		public Terrain canTravel;
		public int fuel;
		public int productionCost;
		
		private TypeData(char symbol, int movesPerTurn, int hits, int power, Terrain canTravel, int fuel, int cost) {
			this.symbol = symbol;
			this.movesPerTurn = movesPerTurn;
			this.hits = hits;
			this.power = power;
			this.canTravel = canTravel;
			this.fuel = fuel;
			this.productionCost = cost;
		}
	}
	
}