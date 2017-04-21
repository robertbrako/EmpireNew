package com.rmbcorp.empire.Views;
/** NOTICE: class is deprecated until further notice
 * the highlight matrix functionality, which highlighted tiles that a selected piece could move to,
 * was sent to RuleManager.  Look for it there.
 * @author Aspire
 *
 */
public class BGVSpecialOps { // By decree, BasicGridView shall be transferring its computational tasks here

	public static int[][] getHighlightMatrix(int[] seed, int rows, int cols, int moves) {
		int[][] result = new int[rows][cols]; // result could be boolean, but int could allow for variable alpha levels
		for (int i=0; i<rows; i++) {
			for (int j=0; j<cols; j++) {
				//TODO: DO NOT calculate square root each time if at all possible!!!
				if (Math.sqrt((i-seed[0])*(i-seed[0])+(j-seed[1])*(j-seed[1])) < moves) // just a good old square root of (x^2 + y^2)
					result[i][j] = 1;
				else
					result[i][j] = -1;
			}
		}
		return result;
	}
}
