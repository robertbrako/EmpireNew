package com.rmbcorp.empire.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public final class LocalSaver extends Saver {
	public static LocalSaver getSaver() {
		if (thisS == null || !thisS.getClass().equals(LocalSaver.class)) {
			thisS = new LocalSaver();
		}
		return (LocalSaver) thisS;
	}
	
	private LocalSaver() {
		saveData = new ArrayList<>();
	}
	
	/** @see com.Empire.EmpireNew.util.Saver#add() for requisite add method **/
	
	@Override
	public boolean out() {
		String DIR = ""; // o auch '/'
		String filename = "saveData" + "." + "ESD"; // R.string.extension; // .esd (or whatever) will be appended
		
		// pay attention to what follows: it uses some important techniques.  Don't forget what you were doing!!
		int result = StreamProcessingTemplate.process(new String[] { DIR, filename }, 
				new LocalOutputStreamProcessor() {
			@Override
			public void process(FileOutputStream fos) throws IOException {
				String line;
				for (SaveData sd : saveData) {
					while (sd.isNotEmpty()) {
						line = sd.getLn(); // do some kind of processing/interpretation/something...
						fos.write(line.getBytes());
					}
				}
				fos.close();
			}
		});
		System.out.println("Save result (CODE): " + result);
		return (result == 0) ? true : false;
	}
}
