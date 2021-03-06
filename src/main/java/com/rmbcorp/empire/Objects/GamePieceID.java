package com.rmbcorp.empire.Objects;

import com.rmbcorp.empire.API.ID;
import com.rmbcorp.empire.API.PieceID;

public class GamePieceID implements PieceID {

	static Integer nextID = 0;
	private Integer contents;
	
	public GamePieceID() {
		contents = nextID++;
	}
	
	@Override
	public String toString() {
		return contents.toString();
	}
	
	public int toInt() {  // this way I can get an Id without having to parseInt each time
		return contents;
	}
	
	@Override
	public boolean equals(ID that) {
		if(that==null) {
			return false;
		} else if(this.toString().contentEquals(that.toString())) {
			return true;
		}
		return false;
	}

}
