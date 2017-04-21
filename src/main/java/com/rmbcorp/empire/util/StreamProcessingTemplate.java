package com.rmbcorp.empire.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/** StreamProcessingTemplate is adapted from Jenkov's InputStreamProcessingTemplate<br>
 * see http://tutorials.jenkov.com/java-exception-handling/exception-handling-templates.html
 * Add more info! It's hardly two weeks later and I'm not sure what I was doing!!!
 * @author Aspire
 */
public class StreamProcessingTemplate {
	
	/** Handles IO issues so that processor can focus on logic
	 * 
	 * @param connParams a String[4] of { JDBC driver, database URL, user name, password }
	 * @param processor the (anonymous) processor to handle data
	 * @return -1 upon error, 0 if all-OK, and 1 if save-OK but close() failed.
	 */
	public static int process(String[] connParams, HibernateOutputStreamProcessor processor) {
		//initial setup
		int result = -1;
		Connection conn = null;
		Statement stmt = null;
		try {
			Class.forName(connParams[0]);
			conn = DriverManager.getConnection(connParams[1], connParams[2], connParams[3]);
			stmt = conn.createStatement();
		} catch (ClassNotFoundException e) {
			return -1;
		} catch (SQLException e) {
			return -1;
		}
		//actual processing
		try {
			processor.process(stmt);
		} catch(SQLException e) { result = -1; }
		result = 0;
		//close resources
		try {
			stmt.close();
		} catch (SQLException e) { result = (result == -1) ? -1 : 1; } // result should stay -1 if -1
		try {
			conn.close();
		} catch (SQLException e) { result = (result == -1) ? -1 : 1; } // result should stay -1 if -1
		return result;
	}
	/** Handles IO issues so that processor can focus on logic
	 * 
	 * @param fileParams a String[2] of { local directory, filename (without extension) }
	 * @param processor the processor to handle data
	 * @return 0 if all-OK, -1 if save but close failed, -2 if partial/no save but close OK, 
	 * -3 if partial/no save & close failed, and -4 if nothing done
	 */
	public static int process(String[] fileParams, LocalOutputStreamProcessor processor) {
		//initial setup
		int result = -1;
		FileOutputStream fos;
		File f;
		String exten = ".esd";
		try {
			f = new File("" + fileParams[1]); // ignore params[0] (dir)
			int append = 1;
			while(f.exists()) {
				f = new File(fileParams[1] + append + exten);
				append++;
				if (append > 1024) {
					System.err.println("Please remove one or more files of name " + fileParams[1] + "X" + exten);
					return -4;
				}
			}
			fos = new FileOutputStream(f);
		} catch (IOException e) {
			System.err.println("no processing occurred. IO Exception.");
			e.printStackTrace();
			return -4;
		}
		//actual processing
		try {
			processor.process(fos);
			result = 0;
		} catch (IOException e1) {
			result = -2;
		}
		// close resources
		try {
			fos.close();
		} catch (IOException e) {
			System.err.println("Attempt to close failed.  Decrementing result shortly");
			result--;
		}
		return result;
	}
}
interface HibernateOutputStreamProcessor {
	public void process(Statement stmt) throws SQLException;
}
interface LocalOutputStreamProcessor {
	public void process(FileOutputStream fos) throws IOException;
}
