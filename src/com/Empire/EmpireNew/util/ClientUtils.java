package com.Empire.EmpireNew.util;

import com.Empire.EmpireNew.RuleManager;
/** Minor helper.  Currently converts byte code for unit to string name of unit **/
public class ClientUtils {

	public enum Flag {
		HIGHLIGHT, POPUP, OPTIONS;
	}
	
	public static String interpret(byte type) {
		if (type == RuleManager.ARMY)
			return "ARMY";
		else if (type == RuleManager.BATTLESHIP)
			return "BATTLESHIP";
		else if (type == RuleManager.BOMBER)
			return "BOMBER";
		else if (type == RuleManager.CARRIER)
			return "CARRIER";
		else if (type == RuleManager.CITY)
			return "CITY";
		else if (type == RuleManager.DESTROYER)
			return "DESTROYER";
		else if (type == RuleManager.FIGHTER)
			return "FIGHTER";
		else if (type == RuleManager.PATROL_BOAT)
			return "PATROL_BOAT";
		else if (type == RuleManager.SATELLITE)
			return "SATELLITE";
		else if (type == RuleManager.SUBMARINE)
			return "SUBMARINE";
		else if (type == RuleManager.TRANSPORT)
			return "TRANSPORT";
		return "";
	}
}
