package com.rmbcorp.empire.util;

public class SaveData { //convert to interface and move to API folder; subclass PlayerSaveData, TileSaveData, etc
	
	private boolean isNotEmpty;
	private StringBuilder contents;
	private int bufferAt;
	
	public SaveData() {
		isNotEmpty = false;
	}
	
	public SaveData(StringBuilder contents) {
		this.contents = contents;
		bufferAt = 0;
		isNotEmpty = true;
	}
	
	public String getLn() {
		int start = bufferAt;
		while(contents.charAt(bufferAt) != '\n') {
			bufferAt++;
		}
		if(bufferAt+1 >= contents.length())
			isNotEmpty = false; // it works (test pending), but consider renaming the variable to reflect the use here
		return contents.substring(start, bufferAt++);
	}
	
	public boolean isNotEmpty() {
		return isNotEmpty;
	}
}
