package in.learnjavaskills.awslambda.utility;

import java.net.InetAddress;
import java.net.UnknownHostException;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class Utility 
{
	/**
	 * @return current machine ip address
	 */
	public static String getIpAddress()
	{
		String ipAddress = "0.0.0.0";
		try { ipAddress = InetAddress.getLocalHost().getHostAddress().trim(); }
		catch (UnknownHostException e) { log.error("Exception occur while fetching ip address ", e); }
		return ipAddress;
	}
}
