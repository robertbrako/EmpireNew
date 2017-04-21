/**
 * 
 */
package com.rmbcorp.empire;

import java.util.ArrayList;

/**Factored out of ViewController, this class will handle orders for all GenericPlayer subclasses.
 * @author Aspire
 *
 */
public class OrderQueue {
	public ArrayList<String> orderNames;
	public byte[][] byteArgs;
	public int[][] intArgs;
	public String[][] stringArgs;
	private int index = 0; 
	
	public OrderQueue() {
		orderNames = new ArrayList<String>();
		byteArgs = new byte[2][];
		intArgs = new int[2][];
		stringArgs = new String[2][];
	}
	
	public void add(String orderName, byte[] byteArg, int[] intArg, String[] stringArg) {
		//add order name to bundle
		orderNames.add(orderName);
		//add byte args, if any; WARNING: need null/error check soon
		try {
			byteArgs[index] = byteArg;
		}
		catch (IndexOutOfBoundsException e) {
			byte[][] tempArgs = new byte[byteArgs.length*2][];
			for (int i=0; i<byteArgs.length; i++) {
				tempArgs[i] = byteArgs[i];
			}
			tempArgs[index] = byteArg;
			byteArgs = tempArgs;
		}
		//add int args, if any; WARNING: need null/error check soon
		try {
			intArgs[index] = intArg;
		}
		catch (IndexOutOfBoundsException e) {
			int[][] tempArgs = new int[intArgs.length*2][];
			for (int i=0; i<intArgs.length; i++) {
				tempArgs[i] = intArgs[i];
			}
			tempArgs[index] = intArg;
			intArgs = tempArgs;
		}
		// add String args, if any
		try {
			stringArgs[index] = stringArg; // removed check if stringArg is null; it's a normal condition
		}
		catch (IndexOutOfBoundsException e) {
			String[][] tempArgs = new String[stringArgs.length*2][];
			for (int i=0; i<stringArgs.length; i++) {
				tempArgs[i] = stringArgs[i];
			}
			tempArgs[index] = stringArg;
			stringArgs = tempArgs;
		}
		index++;
	} // end add()
	
	public void clear() {
		for(int i=0; i<index; i++) {
			byteArgs[i] = null;
			intArgs[i] = null;
			stringArgs[i] = null;
		}
		orderNames.clear();
		index = 0;
	}
	
	public int[] checkDests() {
		int[] result = new int[intArgs.length*2];
		for (int i=0; i<intArgs.length; i+=2) {
			try {
				result[i] = intArgs[i/2][2];
				result[i+1] = intArgs[i/2][3];
			}
			catch (NullPointerException e) { }
		}
		return result;
	}
} // end OrderQueue
