package com.rmbcorp.empire.Objects;

import com.Empire.EmpireNew.API.ID;
import com.Empire.EmpireNew.API.PlayerID;

public final class GenericPlayerID implements PlayerID {
	static Integer nextID = 0;
	private Integer contents;
	
	public GenericPlayerID() {
		contents = nextID++;
	}
	
	@Override
	public String toString() {
		return contents.toString();
	}
	
	public Integer toInt() {
		return Integer.valueOf(contents);
	}
	
	@Override
	public boolean equals(ID that) { // ID class is required, don't forget!
		if(that==null) {
			return false;
		} else if(this.toString().equals(that.toString())) {
			return true;
		}
		return false;
	}
}
