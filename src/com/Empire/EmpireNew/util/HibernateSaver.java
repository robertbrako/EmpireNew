package com.Empire.EmpireNew.util;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public final class HibernateSaver extends Saver {
	
	public static HibernateSaver getSaver() {
		if (thisS == null) {
			thisS = new HibernateSaver();
		}
		return (HibernateSaver) thisS;
	}
	
	private HibernateSaver() {
		saveData = new ArrayList<>();
	}
	
	/** @see com.Empire.EmpireNew.util.Saver#add() for requisite add method **/
	
	@Override
	public boolean out() {
		String JDBC_DRIVER = "com.mysql.jdbc.Driver";
		String DB_URL = "jdbc:mysql://localhost/"; // consider http://localhost/
		String USER = "root";
		String PASS = "";
		
		int result = StreamProcessingTemplate.process(new String[] { JDBC_DRIVER, DB_URL, USER, PASS }, 
				new HibernateOutputStreamProcessor() {
			@Override
			public void process(Statement stmt) throws SQLException {
				String line;
				for (SaveData sd : saveData) {
					while (sd.isNotEmpty()) {
						line = sd.getLn(); // and do some kind of thing...processing or parsing or whatever
						stmt.executeUpdate("INSERT" + line.toString());
					}
				}
			}
		});
		return (result == 0) ? true : false;
	}
}
