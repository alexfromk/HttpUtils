package ua.ak.utils.http.filter;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.*;


/**
 * Simple encode filter for HTTP-servlet
 * Can be used in common(sharet) lib, for example in tomcat/lib
 * 
 * Set in  web.xml
 *      <filter>
 *		<filter-name>CharacterEncodingFilter</filter-name>
 *		<filter-class>ua.ak.utils.http.filter.EncodeFilter</filter-class>
 *		<init-param>
 *			<param-name>requestEncoding</param-name>
 *			<param-value>UTF-8</param-value>
 *		</init-param>
 *	</filter>
 * @author Alex
 *
 */
		
public class EncodeFilter implements Filter
{	
	//Default encoding
	private static String defaultEncoding = "UTF-8";

	//Map web-application(hashCode of filter) - encoding
	private static HashMap<String, String> enc = new HashMap<String, String>();

	private static final String attrNameFilterEncoding ="FilterEncoding1";
	
	/**
	 * Get default encoding
	 */
	public static String getCurrentEncoding()
	{
		return defaultEncoding;
	}

	/**
	 * Get encoding for current web-application
	 */
	public static String getCurrentEncoding(ServletRequest request)
	{
		String res=(String)request.getAttribute(attrNameFilterEncoding);
		if(res==null)
			res=defaultEncoding;
		return res;
	}

	void setCurrentEncoding(String currentEncoding)
	{
		synchronized (enc)
		{
			enc.put(getKeyThisFilter(),currentEncoding);
		}						
	}

	String getCurrentFilterEncoding()
	{
		String res=enc.get(getKeyThisFilter());
		if(res==null)
			res=defaultEncoding;
		return res;
	}
	
	String getKeyThisFilter()
	{
		return "f-"+Integer.toString(this.hashCode());
	}
	
	public void init(FilterConfig config) throws ServletException
	{
		String encoding = config.getInitParameter("requestEncoding");

		if ((encoding != null) && (encoding.length() > 0))
		{
			//Store encoding for current filter to map
			setCurrentEncoding(encoding);
		}					
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain next) throws IOException, ServletException
	{
		String enc1 = request.getCharacterEncoding();
		String currEnc=getCurrentFilterEncoding();
		if ((enc1 == null)||(enc1.compareToIgnoreCase(currEnc) != 0))
		{
			request.setCharacterEncoding(currEnc);
		}
		//Current encoding, save to attrubutes
		request.setAttribute(attrNameFilterEncoding,currEnc);
		//Debug-check map.size
		//System.out.println("EncodeFilter-enc.size()="+enc.size());
		next.doFilter(request,response);
	}

	public void destroy()
	{
	}
}
