package edu.upenn.cis.cis455.webserver;

import java.net.InetAddress;

public class HttpServerConfig {
	public String webInfPath;
	public String rootDir;
	public int port;
	public InetAddress serverIP;
	public String serverName;
	public String protocolSupported;
	public String hostName;
	
	public HttpServerConfig(int port, String rootDir, String webInfPath){
		this.port = port;
		this.rootDir = rootDir;
		this.webInfPath = webInfPath;
		this.serverIP = null;
		this.serverName = "Tianxiang";
		this.protocolSupported = "GET/HEAD/POST";
		this.hostName = "Tianxiang";
	}
}
