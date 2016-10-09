package edu.upenn.cis.cis455.testServlets;



import javax.servlet.*;
import javax.servlet.http.*;

import java.io.*;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;

public class DemoServlet extends HttpServlet {
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<HTML><HEAD><TITLE>Simple Servlet</TITLE></HEAD><BODY>");
		out.println("<P>Hello!</P>");
		out.println("</BODY></HTML>");	
	}
}
		
