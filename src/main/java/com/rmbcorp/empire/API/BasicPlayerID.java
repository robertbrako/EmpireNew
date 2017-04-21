package com.rmbcorp.empire.API;

public class BasicPlayerID implements PlayerID {

	static Integer nextID = 0;
	private Integer contents;
	
	public BasicPlayerID() {
		contents = nextID++;
	}
	
	@Override
	public String toString() {
		return contents.toString();
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
