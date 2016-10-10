package edu.upenn.cis.cis455.servlets;

import java.util.ArrayList;
import java.util.List;

public class HttpErrorLog {
	public static List<String> errorLog = new ArrayList<>();

	public HttpErrorLog(){}
	
	public static void addError(String errorStatement){
		errorLog.add(errorStatement);
	}
	
	public static List<String> getErrorLog(){
		return errorLog;
	}
}
