package com.Empire.EmpireNew.Views;

import com.Empire.EmpireNew.util.Numbers;


//import android.view.MotionEvent;

/**  /** Here's my beloved battery of algorithms to identify a hexagon in 2D space.
 *  Basically, I figured out the math and logic needed to take raw x,y click data,
 *  and convert it into appropriate column and row data to select a hexagon,
 *  regardless of zoom level and regardless of where the map has been panned. 
 *  
 * @author Aspire
 *
 */
public class Locator {
	
	private static final int YES = 1;
	private static final int NO = 0;
	private static final int IDK = -1;

	/** Main function of this class: 
	 * 
	 * @param origin is X0,Y0, which comes in from ViewController, which came in from BGV
	 * @param gridDimensions is rows,cols, which come in from VC, which came from BGV, which came from main. Umm...
	 * @param hexSize is the basic unit for controlling zoom, which came from same place as X0,Y0
	 * @param eX is click location's x, which came from VC, which came from listener, which came from...
	 * @param eY is as above
	 * @return result of (row,col) corresponding to eX,eY, or { Numbers.NULL, Numbers.NULL } if out-of-range.
	 */
	public static int[] locate(int[] origin, int[] gridDimensions, float hexSize, float eX, float eY) {
		int[] result = {Numbers.NULL, Numbers.NULL };
		
		float x = eX - origin[0];
		float y = eY - origin[1];
		
		float h = Hexagon.CalculateH(hexSize);
		float r = Hexagon.CalculateR(hexSize);
		
		float regionValue = y % (hexSize + hexSize + h + h);
		float[] bounds = { hexSize, hexSize+h, hexSize+h+hexSize, hexSize+h+hexSize+h };
		
		if (regionValue >= 0 && regionValue < bounds[0]) { // easy region
			result[0] = (int) (y / (hexSize+h));
			result[1] = (int) (x / (r+r));
		}
		else if (regionValue >= bounds[0] && regionValue < bounds[1]) { // difficult region

			if (x % (r+r) >= 0 && x % (r+r) < r) {
				/** This will really be helpful in the future:
				 * 1. y is already corrected for Y0, so don't worry about that
				 * 2. we have a modular x region test above, which breaks down the problem
				 * 3. we basically want a line y = mx + b, where result depends on if above or below the line
				 * 4. "y" is actually "y % bounds[3]", so that y zeroes out every 2 rows
				 * 5. "y % bounds[3]" is actually that minus hexSize, to zero y out in this particular strip
				 * * Got it?  Good.  Now for the other half the equation:
				 * 6. "m" the slope is h/r, since y rises by h, x runs by r, until zeroing and repeating
				 * 7. "x" is actually "x % r+r", so that x zeroes out after every column
				 * 8. "b" is 0, luckily.
				 * 9. if y >= mx, then we're above the line, and we calculate the row appropriately
				 */
				if ((y % bounds[3]) - hexSize >= (h/r*(x%(r+r)))) { // diagonal line equation needed
					result[0] = (int) (y / (hexSize+h)) + 1;
					result[1] = (int) (x / (r+r)) - 1;
				}
				else {
					result[0] = (int) (y / (hexSize+h));
					result[1] = (int) (x / (r+r));
				}
			}
			else if (x % (r+r) >= r && x % (r+r) < r+r) {
				//this works and I hope it never needs to be touched
				//(I said that last time, which didn't end well...)
				if ((y % bounds[3]) - hexSize >= h - h/r*(x%(r+r)-r)) { // slightly different diagonal line equation
					
					result[0] = (int) (y / (hexSize+h)) + 1;
					result[1] = (int) (x / (r+r));
				}
				else {
					result[0] = (int) (y / (hexSize+h));
					result[1] = (int) (x / (r+r));
				}
			} // these x tests should be exhaustive; no need for else
		}
		else if (regionValue >= bounds[1] && regionValue < bounds[2]) { // another easy region
			result[0] = (int) (y / (hexSize+h));
			result[1] = (int) ((x+r) / (r+r)) - 1;
		}
		else if (regionValue >= bounds[2] && regionValue < bounds[3]) { // difficult region
			float testX = (x-r);
			if (testX % (r+r) >= 0 && testX % (r+r) < r) {
				
				if ((y % bounds[3]) - (hexSize+h+hexSize) >= (h/r*(testX%(r+r)))) { // diagonal line equation needed
					result[0] = (int) (y / (hexSize+h)) + 1;
					result[1] = (int) (x / (r+r));
				}
				else {
					result[0] = (int) (y / (hexSize+h));
					result[1] = (int) (x / (r+r));
				}
			}
			else if (testX % (r+r) >= r && testX % (r+r) < r+r) {
				
				if ((y % bounds[3]) - (hexSize+h+hexSize) >= h - h/r*(x%(r+r))) { // slightly different diagonal line equation
					result[0] = (int) (y / (hexSize+h)) + 1;
					result[1] = (int) (testX / (r+r)) + 1;
				}
				else {
					result[0] = (int) (y / (hexSize+h));
					result[1] = (int) (testX / (r+r));
				}
			} // these x tests should be exhaustive; no need for else
		}
		//... no need for a catch-all else at the end
		System.out.println("Suggested row = " + result[0] + ", suggested col = " + result[1]);

		if (result[0] < 0 || result[0] >= gridDimensions[0] || result[1] < 0 || result[1] >= gridDimensions[1])
			result[0] = Numbers.NULL;
		return result;
	}
	/** Used to interpret clicks for yes/no dialogs
	 * 
	 * @param yesXBounds boundaries of yes box in x direction
	 * @param noXBounds boundaries of no box in x direction
	 * @param yesNoYBounds boundaries of both yes and no box in y direction (we assume they have same y position)
	 * @param x incoming x detected by listener
	 * @param y incoming y detected by listener
	 * @return code for result: YES=1, NO=0, or IDK=-1
	 */
	public static int decide(float[] yesXBounds, float[] noXBounds, float[] yesNoYBounds, float x, float y) {
		if (y >= yesNoYBounds[0] && y < yesNoYBounds[1]) {
			if (x >= yesXBounds[0] && x < yesXBounds[1]) {
				return YES;
			}
			else if (x >= noXBounds[0] && x < noXBounds[1]) {
				return NO;
			}
		}
		return IDK;
	}
	/** Used if unit options menu is up
	 * @param eX is x coordinate of event
	 * @param eY is y coordinate of event
	 * @param width is window width, obtained from VC, from BGV
	 * @param height is window height, obtained from VC, from BGV
	 * @return int code: 1=sentry, 2=move, 3=load?, 4=move_to, 5=exec(patrol?), 6=close, -999000=default
	 */
	public static int locateMenu(float eX, float eY, int width, int height) {
		if (eX >= width*.2f && eX < width*.55f) {
			if (eY >= height*.65f && eY < height*.75f) {//sentry
				return 1;
			}
			if (eY >= height*.75f && eY < height*.85f) {//move
				return 2;
			}
			if (eY >= height*.85f && eY < height*.95f) {//load
				return 3;
			}
		}
		else if (eX >= width*.55f && eX < width*.95f) {
			if (eY >= height*.65f && eY < height*.75f) {//move to
				return 4;
				
			}
			if (eY >= height*.75f && eY < height*.85f) {//patrol
				return 5;
			}
			if (eY >= height*.85f && eY < height*.95f) {//close
				return 6;
			}
		}
		return Numbers.NULL;
	}
}
