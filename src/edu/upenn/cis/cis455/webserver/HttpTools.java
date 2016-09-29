package edu.upenn.cis.cis455.webserver;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The class includes necessary tools to be used by server
 * @author Tianxiang Dong
 *
 */
public class HttpTools {
	public static final String DATE_FORMAT1 = "EEE, dd MMM yyyy HH:mm:ss z";
	public static final String DATE_FORMAT2 = "EEEE, dd-MMM-yy HH:mm:ss z";
	public static final String DATE_FORMAT3 = "EEE MMM dd HH:mm:ss yyyy";
	
	/**
	 * The static class used to parse the String into Date.
	 * @param formatedDate The String to be converted into Date.
	 * @return The Date object of the input String. Null if the input string is not legal.
	 */
	public static Date parseDateFormat(String formatedDate) {
			
			SimpleDateFormat format1 = new SimpleDateFormat(DATE_FORMAT1);
			SimpleDateFormat format2 = new SimpleDateFormat(DATE_FORMAT2);
			SimpleDateFormat format3 = new SimpleDateFormat(DATE_FORMAT3);
			Date ret = null;
			try {
				ret = format1.parse(formatedDate);
			} catch (ParseException e1) {
				try {
					ret = format2.parse(formatedDate);
				} catch (ParseException e2) {
					try {
						ret = format3.parse(formatedDate);
					} catch (ParseException e3) {		
						return null;
					}	
				}
			}
			return ret;
	}

}