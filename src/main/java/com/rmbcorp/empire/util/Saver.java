package com.rmbcorp.empire.util;

import java.util.ArrayList;

/** To put in practice object-oriented principles, I spent some time developing this abstract class.<br>
 * I determined which methods "should" or "must not" be overridden.  I have the following subclasses:<br><br>
 * 1. HibernateSaver: uses Hibernate to save to a database<br>
 * 2. LocalSaver: the usual local save-to-disk stuff<br><br>
 * The difference in subclasses is in the out() implementation, of course.<br>
 * Currently, subclasses are forced to use SaveData as a uniform way of collecting save data from views,<br>
 * game objects, etc.  We'll see if I revisit this approach.<br>
 * <br>
 * PLEASE USE close() WHEN FINISHED!!! Until I say otherwise.
 * @author Aspire
 *
 */
public abstract class Saver {
	//gameData (turn, board, etc)
	
	//playerID (per player)
	////pieceData
	//////location
	//////type
	//////HP, upgrades, etc
	
	protected static Saver thisS;
	protected ArrayList<SaveData> saveData;
	
	/** All subclasses must construct with singleton! **/
	protected static Saver getSaver() {
		return thisS;
	}
	protected Saver() { }
	
	/** All subclasses must use add this way!
	 * @param sd an instance of save data to be added to list
	 */
	public void add(SaveData sd) {
		saveData.add(sd);
	}
	
	/** All subclasses must close this way! Dereferences singleton**/
	public void close() {
		thisS = null;
	}
	
	/** Output save data to file, database, or whatever
	 * All subclasses must override this!
	 * Subclasses are "encouraged" to return true if a-ok, false otherwise.
	**/
	protected boolean out() {
		return false;
	}
}
