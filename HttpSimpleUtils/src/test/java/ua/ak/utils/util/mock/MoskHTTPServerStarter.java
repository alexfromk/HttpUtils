package ua.ak.utils.util.mock;

import static org.junit.Assert.fail;

import ua.ak.utils.model.HTTPTransportInfo;

/**
 * Легкий класс для старта MoskHTTPServer, для применения в юнит-тестах 
 * @author alexk
 *
 */
public class MoskHTTPServerStarter
{
	public static final MockHttpServer server=new MockHttpServer();
	
	public static void startServer()
	{
		if(!server.IsStarted())
		{
			try
			{
				server.start();		
				int waitMs=10;
				int delay=0;
				while(!server.getServer().isStarted() && delay<1000)
				{
					Thread.sleep(waitMs);
					delay+=waitMs;
				}
				System.out.println("MockHttpServer - started by "+delay+" ms");
			}
			catch (Exception e)
			{
				e.printStackTrace();
				fail(e.getMessage());
			}
		}
	}
	
	public static HTTPTransportInfo getLocalHTTPTransportInfo()
	{
		HTTPTransportInfo httpTransportInfo=new HTTPTransportInfo();
		httpTransportInfo.setHost("localhost");
		httpTransportInfo.setPort(MoskHTTPServerStarter.server.getPort());
		return httpTransportInfo;
	}
}
