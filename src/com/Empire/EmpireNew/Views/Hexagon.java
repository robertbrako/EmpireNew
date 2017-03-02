package com.Empire.EmpireNew.Views;

//import android.util.FloatMath;

/** Use this class to create drawable Hexagons.<br>
 * Basic idea: column and row are fixed for each Hexagon on creation.<br>
 * It seemed I may as well fully initialize each Hexagon upon creation,<br>
 * so constructor takes 5 variables.<br>
 * The key is to call setHexPoints(X0,Y0,hexSize).  At that point, we<br>
 * update position & scale info, and we thus know all we need in order<br>
 * to actually draw each Hexagon.
 * 
 * @author robertbrako
 * 
 */
public class Hexagon {

	private int myRow;
	private int myCol;
	private float r, h;
	private float side, disp;
	
	private float[] points;
	
	// ------ Hexagon Preliminary Calculations ------ //
	/** convert degrees to radians - basic math.  **/
    private static double DegreesToRadians(double degrees) {
        return degrees * 3.14159f / 180; // read that Math.PI is a double; don't need that much precision. Function not called often though
    }
    /** returns vertical-only distance from top-left point of hex to apex.  maybe I should just let this return 0.5*side... **/
    static float CalculateH(float side) {
        return (float) (Math.sin(DegreesToRadians(30)) * side);
    }
    /** returns horizontal-only distance from top-left point of hex to center. **/ 
    static float CalculateR(float side) {
        return (float) (Math.cos(DegreesToRadians(30)) * side);
    } 
    // ---------------------------------------------- //
	
    // ----------------Constructor(s)------------------- //
    /** Constructor fully initializes hexagon coordinates
     * 
     * @param row row of this hexagon.  Will not change!
     * @param col column of this hexagon.  Will not change!
     * @param x0 global X0 to help figure x position
     * @param y0 global Y0 to help figure y position
     * @param size global size (as measured by vertical sideLength) to help figure size
     */
    public Hexagon(int row, int col, int x0, int y0, float size) { // each hexagon object should have a fixed col and row
		myRow = row;
		myCol = col;
		points = new float[7];
		setHexPoints(x0, y0, size);
	}
	/** returns row of this hexagon; used in one spot in MBV - try to remove this **/
	public int getRow() {
		return myRow;
	}
	/** returns col of this hexagon; used in one spot in MBV - try to remove this **/
	public int getCol() {
		return myCol;
	}
	/** This delivers, for given Hexagon, a 7-point array of x and y points.
	 * Usage: construct a hexagon with following points indices:
	 * [0,3] [1,4] [2,3] [2,5] [1,6] [0,5]
	 * @return points a float[] of { x0, x1, x2, y0, y1, y2, y3 }
	 */
	public float[] getHexPoints() {
		return points;
	}
	/** used to modify coordinates so that BGV can draw this hex at right spot
	 *  
	 * @param x0 the global X0 coordinate that determines displacement in x direction
	 * @param y0 the global Y0 coordinate that determines displacement in y direction
	 * @param sideLength the global size coordinate that determines hexSize (i.e. zoom level)
	 * @return float[] of drawable coordinates; getHexPoints() has details on the array contents
	 */
	public float[] setHexPoints(int x0, int y0, float sideLength) {
		side = sideLength;
		h = CalculateH(side);
		r = CalculateR(side);
		disp = side + h;

		float distFactor = (myCol)*(r+r);
		float rowFactor = r*(myRow%2); // square-ish version
		//float rowFactor = r*(myRow); // cascading-version
		
		// x points
		points[0] = x0 + distFactor + rowFactor;
		points[1] = points[0] + r;
		points[2] = points[1] + r;
		
		// y points
		points[3] = y0 + (myRow)*(disp);
		points[4] = points[3] - h;
		points[5] = points[3] + side;
		points[6] = points[5] + h;

		return points;
	}
}
