package ua.ak.utils.util.mock;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

/**
 * встроенный HTTP-server для имитации HTTP - запросов
 * @author alexk
 *
 */
public class MockHttpServer
{
	String defaultContentType="text/html;charset=utf-8";
	
	int port=8089;
	/**
	 * обработчика uri - Handler
	 */
	Map<String,AbstractHandler> handlers=null;

	DefaultHandler defaultHandler=new DefaultHandler(); 
	
	Server server =null;
	
	public void start() throws Exception
	{
		stop();
		server=new Server(port);
		server.setHandler(new DispatchHandler());
		ServerStarter serverStarter=new ServerStarter(server);
		serverStarter.getThread().start();
	}

	public void stop() throws Exception
	{
		if(server!=null)
		{
			server.stop();
			server=null;
		}
	}

	public class ServerStarter implements Runnable
	{
		Thread thread;
		Server server;

		public ServerStarter(Server server)
		{
			super();
			this.server = server;
			this.thread = new Thread(this);
		}

		@Override
		public void run()
		{
			try
			{
				server.start();
				server.join();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}			
		}

		/**
		 * @return the server
		 */
		public Server getServer()
		{
			return server;
		}

		/**
		 * @param server the server to set
		 */
		public void setServer(Server server)
		{
			this.server = server;
		}

		/**
		 * @return the thread
		 */
		public Thread getThread()
		{
			return thread;
		}
	}
	
	/**
	 * обработчик - диспетчер
	 * @author alexk
	 *
	 */
	public class DispatchHandler extends AbstractHandler
	{
		@Override
	    public void handle(String target,Request baseRequest,HttpServletRequest request,HttpServletResponse response) 
	        throws IOException, ServletException
	    {
			String uri=baseRequest.getRequestURI();
			AbstractHandler handler1=getHandler(uri);
			if(handler1==null)
				defaultHandler.handle(target,baseRequest,request,response);
			else
				handler1.handle(target,baseRequest,request,response);
	    }
	}

	
	public class DefaultHandler extends AbstractHandler
	{
		@Override
	    public void handle(String target,Request baseRequest,HttpServletRequest request,HttpServletResponse response) 
	        throws IOException, ServletException
	    {
	        response.setContentType(defaultContentType);
	        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
	        //response.setStatus(HttpServletResponse.SC_OK);
	        baseRequest.setHandled(true);
	        response.getWriter().println("MockHttpServer - URL not found: "+request.getRequestURI());
	    }
	}


	/**
	 * @return the defaultContentType
	 */
	public String getDefaultContentType()
	{
		return defaultContentType;
	}

	/**server
	 * @param defaultContentType the defaultContentType to set
	 */
	public void setDefaultContentType(String defaultContentType)
	{
		this.defaultContentType = defaultContentType;
	}

	/**
	 * @return the portsetHandler
	 */
	public int getPort()
	{
		return port;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(int port)
	{
		this.port = port;
	}

	/**HelloHandler
	 * @return the handlers
	 */
	public Map<String, AbstractHandler> getHandlers()
	{
		if(handlers==null)
			handlers=new HashMap<String, AbstractHandler>();
		return handlers;
	}

	/**
	 * @param handlers the handlers to set
	 */
	public void setHandlers(Map<String, AbstractHandler> handlers)
	{
		this.handlers = handlers;
	}

	/**
	 * Obtain handler for uri
	 * @param uri
	 * @return
	 * ==null - not found
	 */
	public AbstractHandler getHandler(String uri)
	{
		return getHandlers().get(uri.toLowerCase());
	}

	/**
	 * @param handlers the handlers to set
	 */
	public void setHandler(String uri, AbstractHandler handler)
	{
		getHandlers().put(uri.toLowerCase(),handler);
	}
	
	/**
	 * @return the server
	 */
	public Server getServer()
	{
		return server;
	}
	
	public class HelloHandler extends AbstractHandler
	{
	    public void handle(String target,Request baseRequest,HttpServletRequest request,HttpServletResponse response) 
	        throws IOException, ServletException
	    {
	        response.setContentType("text/html;charset=utf-8");
	        response.setStatus(HttpServletResponse.SC_OK);
	        baseRequest.setHandled(true);
	        String[] a=request.getParameterValues("a");
	        String[] sd=request.getParameterValues("SD");
	        response.getWriter().println("<h1>Hello World</h1>");
	        if(a==null)
	        {
	        	response.getWriter().println("<br/>a=null");
	        }
	        else
	        {
	        	response.getWriter().print("<br/>a=");
	        	for(String aVal : a)
	        	{
	        		response.getWriter().print(aVal+",");
	        	}
	        }
	        
	        if(a==null)
	        {
	        	response.getWriter().println("<br/>sd=null");
	        }
	        else
	        {
	        	response.getWriter().print("<br/>sd=");
	        	for(String aVal : sd)
	        	{
	        		response.getWriter().print(aVal+",");
	        	}
	        }

	        //response.flushBuffer();
	    }
	}

	public boolean IsStarted()
	{
		return server!=null && server.isStarted();
	}
}
