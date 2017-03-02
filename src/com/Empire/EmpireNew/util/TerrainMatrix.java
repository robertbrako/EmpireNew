package com.Empire.EmpireNew.util;

import java.util.Arrays;
import java.util.Random;

import com.Empire.EmpireNew.MainActivity;
import com.Empire.EmpireNew.Objects.Terrain;

/**
 * Another nifty creation that:
 * 1. randomly seeds at least two starting land areas, accounting for grid size
 * 2. assigns a "river" that's "fair": may slightly favor one player, but overall odds are equal
 * CAUTION: To properly seed start locations, grid size needs to be 8x8.  However, there is a
 * failsafe that populates the grid with land only if grid is smaller than that.
 * Algorithm can seed a maximum of 4 players to 4 quadrants
 * 
 * @author Aspire
 *
 */
public class TerrainMatrix {
	
	private static final int seedDist = 2; // constant that determines how close seed can be to boundary
	private static int[] startLocation;
	private static byte[] quadMap = { 1, 2, 3, 4};
	/** At beginning of game, sets up tiles to be land or water/sea.
	 * 
	 * @param rows number of rows in game grid
	 * @param cols number of columns in game grid
	 * @return matrix of Terrain[rows][cols], where each entry is Terrain.LAND or Terrain.SEA
	 */
	public static Terrain[][] spawn(int rows, int cols) {
		int playerCount = MainActivity.getPlayers(); //  maybe create a player manager to deal with this
		Terrain[][] result = new Terrain[rows][cols];
		startLocation = new int[playerCount*2];
		Random r = new Random();
		
		// PHASE 1: Return all-land if grid is too small... which probably will never happen
		if (rows <= 7 || cols <= 7) {
			for (int i=0; i < rows; i++) {
				for (int j=0; j < cols; j++) {
					result[i][j] = Terrain.LAND;
				}
			}
			startLocation = new int[] { 1, 1, 2, 2 };
			return result;
		}
		// PHASE 2: Randomly assign land or sea to whole grid initially
		for (int i=0; i < rows; i++) {
			for (int j=0; j < cols; j++) {
				if (r.nextFloat()>0.33f)
					result[i][j] = Terrain.LAND;
				else result[i][j] = Terrain.SEA;
			}
		}
		// PHASE 3: Draw a river that cuts the grid in two
		int[] riverCoords = new int[cols*2]; // good setup for a horizontal river
		//so far, implementation has a bias if rows are even (or odd, but I think even)
		for (int i=0; i<cols*2; i+=2) {
			riverCoords[i] = rows/2+(r.nextInt(3)-1);;
			riverCoords[i+1] = i/2;
			result[riverCoords[i]][riverCoords[i+1]] = Terrain.SEA;
			if (riverCoords[i] != rows/2)
				result[rows/2][i/2] = Terrain.SEA;
		}
		// PHASE 4: Randomly pick seed tile, subject to constraints, and make surrounding tiles land tiles
		// Remark: there's a chance that it will overlap the river algorithm for maps smaller than 16x16
		
		//stressTest(result);
		
		for (int i=0; i<playerCount*2; i+=2) {
			float[] quad = nextQuad(rows, cols); // warning: returns "float" based on integer divisions
			float distToQuadByRow = rows*.5f-quad[0]; // warning: this does integer division
			float distToQuadByCol = cols*.5f-quad[1]; // caution: this can be negative; no problem under current implementation
			float seedRow = quad[0]+(r.nextFloat()-0.5f)*distToQuadByRow; // we can move away from quad by up to 1/2 of quad dist to center 
			float seedCol = quad[1]+(r.nextFloat()-0.5f)*distToQuadByCol;
			// code below is a failsafe in case I mess with things, but above equation should be safe for grid >= 16x16
			if (seedRow < seedDist)
				seedRow = seedDist;
			if (rows-seedRow < seedDist)
				seedRow = rows-seedDist;
			if (seedCol < seedDist)
				seedCol = seedDist;
			if (cols-seedCol < seedDist)
				seedCol = cols-seedDist;
			// at this point, math is done.  Cast result to int to produce an index location
			startLocation[i] = (int) seedRow;
			startLocation[i+1] = (int) seedCol;
			
			result = initialSeeding(result, startLocation[i], startLocation[i+1]);
			// PHASE 5: Add additional land tiles (we know we can move up or down based on seedDist & minimum grid size assumptions)
			result = secondSeeding(result, startLocation[i], startLocation[i+1], r.nextBoolean());
		}
		// PHASE 5: Return the result of the seeding processes
		return result;
	}
	/** Returns start location for all players; e.g. { P1.x, P1.y, P2.x, P2.y, ... } **/
	public static int[] getStart() {
		System.out.println(Arrays.toString(startLocation));
		return startLocation; // starting point of all the players
	}
	
	private static Terrain[][] initialSeeding(Terrain[][] input, int seedRow, int seedCol) {
		boolean success = false;
		int fac = (1 - seedRow%2); // calculations may vary depending on the starting row
		
		while (success == false) { // fail-safe initialization process, possibly unnecessary
			input[seedRow][seedCol] = Terrain.LAND;
			input[seedRow+1][seedCol+1-fac] = Terrain.LAND; // direction 0
			input[seedRow][seedCol+1] = Terrain.LAND; // direction 1
			input[seedRow-1][seedCol+1-fac] = Terrain.LAND; // direction 2
			input[seedRow-1][seedCol-fac] = Terrain.LAND; // direction 3
			input[seedRow][seedCol-1] = Terrain.LAND; // direction 4
			input[seedRow+1][seedCol-fac] = Terrain.LAND; // direction 5
			success = true;
		}
		return input;
	}
	
	private static Terrain[][] secondSeeding(Terrain[][] input, int seedRow, int seedCol, boolean upDown) {
		int dir = 0;
		if (upDown) { // first look upwards (negative row direction)
			if (seedRow-2 > 0)
				dir = -1;
			else dir = 1;
		}
		else { // looks repetitive, but we want to follow the lead of the random variable upDown
			if (seedRow+2 < input.length)
				dir = 1;
			else dir = -1;
		} // thus, dir is either 1 or -1, and we can proceed accordingly
		//NOTE: it's guaranteed we can move left or right by one.  More than that can be dealt with later
		input[seedRow+2*dir][seedCol] = Terrain.LAND; // (comments below are with reference to this location)
		input[seedRow+2*dir][seedCol+1] = Terrain.LAND; // direction 1
		input[seedRow+2*dir][seedCol-1] = Terrain.LAND; // direction 4
		
		return input;
	}
	/** nextQuad does not adjust to index scheme: i.e. a 16x16 would return 4,4 instead of [3,3] **/
	private static float[] nextQuad(int rows, int cols) {
		float qRow, qCol;
		//first, pick from available quadrants
		int quadIndex = new Random().nextInt(quadMap.length);
		int quadrant = quadMap[quadIndex]; // will be 1, 2, 3, or 4
		// then, update map; we know the one we picked at this point
		quadMap[quadIndex] = 0;
		byte[] tempMap = new byte[quadMap.length-1];
		for (int i=0; i<tempMap.length; i++) {
			if (quadMap[i] != 0) {
				tempMap[i] = quadMap[i];
			}
		}
		quadMap = tempMap;
		// now, prepare appropriate coordinates and return them
		if (quadrant <= 2) // remember, quadrant can be 1, 2, 3, or 4
			qRow = rows*.25f; // need to subtract 1 because range is [0,length-1], not [1,length]
		else
			qRow = rows*.75f;
		if (quadrant%2 == 1)
			qCol = cols*.25f;
		else
			qCol = cols*.75f;
		System.out.println("QuadRow = " + qRow + " QuadCol = " + qCol);
		return new float[] { qRow, qCol };
	}
	/** Testing routines are starting to get a little more complex **/
	@SuppressWarnings("unused")
	private static void stressTest(Terrain[][] input) {
		Random r = new Random();
		//first assume the quad function works:
		int x0 = 3, y0 = 3; // assuming grid of 16x16, let's just use first quadrant
		float seedRow, seedCol;
		System.out.println("Initial Condition: Row = " + x0 + ", Col = " + y0);
		for (int i=0; i<100; i++) {
			seedRow = x0+(r.nextFloat()-0.5f)*(x0-seedDist);
			seedCol = y0+(r.nextFloat()-0.5f)*(y0-seedDist);
			System.out.println("Try " + i + ": Row = " + seedRow + ", Col = " + seedCol);
		}
		System.out.println("Now for other quadrant: Row = 11, Col = 11");
		x0=12;
		y0=12;
		for (int i=0; i<100; i++) {
			seedRow = x0+(r.nextFloat()-0.5f)*(input.length/2-x0);
			seedCol = y0+(r.nextFloat()-0.5f)*(input.length/2-y0);
			System.out.println("Try " + i + ": Row = " + seedRow + ", Col = " + seedCol);
		}
		System.out.println("End of stress test");
	}
}
