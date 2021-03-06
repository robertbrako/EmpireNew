package com.rmbcorp.empire.Objects;

import com.rmbcorp.empire.API.ID;
import com.rmbcorp.empire.API.TileID;

public class GameTileID implements TileID {

	static Integer nextID = 0;
	private Integer contents;
	
	public GameTileID() {
		contents = nextID++;
	}
	
	@Override
	public String toString() {
		return contents.toString();
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
