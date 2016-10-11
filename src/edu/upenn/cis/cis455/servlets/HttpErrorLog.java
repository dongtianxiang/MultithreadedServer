package edu.upenn.cis.cis455.servlets;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the class for error log
 * @author cis555
 *
 */
public class HttpErrorLog {
	public static List<String> errorLog = new ArrayList<>();

	public HttpErrorLog(){}
	
	/**
	 * Using this method to add error into list
	 * @param errorStatement
	 */
	public static void addError(String errorStatement){
		errorLog.add(errorStatement);
	}
	
	public static List<String> getErrorLog(){
		return errorLog;
	}
}
