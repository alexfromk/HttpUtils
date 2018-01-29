package ua.ak.utils.model;

import java.io.Serializable;
import java.util.Map;

/**
 * информация о http-соединении
 * @author alexk
 *
 */
public class HTTPTransportInfo implements Serializable
{
	private static final long serialVersionUID = -8223485565582098240L;

	/**хост**/
	String host="";
	
	/**порт**/	
	int port=8080;
	
	/**протокол**/
	String protocol="http";
	
	/**Using protocol HTTP/1.0*/
	boolean isVersionHttp10=false;
	
    /**
     * Defines the socket timeout (<code>SO_TIMEOUT</code>) in seconds,
     * A timeout value of zero is interpreted as an infinite timeout.
     */
    int socketTimeoutSec=20;

    /**
     * Determines the timeout in seconds until a connection is established.
     * A timeout value of zero is interpreted as an infinite timeout.
     */
    int connectTimeoutSec=100;
	
    String login="";
    
    String password="";
    
    String userAgent="Crm java agent - default";
    
    String charset="utf-8";
    
  //Дополнительные заголовки(формат (имя заголовка) - (значение))
  	Map<String,String> addHeaders = null;
  	
  	/**использовать прокси-сервер**/
  	boolean useProxy=false;
	/** прокси-сервер - хост**/
	String proxyHost="";
	
	/** прокси-сервер - порт**/	
	int proxyPort=3128;

	/**прокси-протокол**/
	String proxyProtocol="http";
	
	/** прокси-сервер - логин**/
    String proxyLogin="";
    
    /** прокси-сервер - пароль**/
    String proxyPassword="";

	/**
	 * @return the host
	 */
	public String getHost()
	{
		return host;
	}

	/**
	 * @param host the host to set
	 */
	public void setHost(String host)
	{
		this.host = host;
	}

	/**
	 * @return the port
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

	/**
	 * @return the protocol
	 */
	public String getProtocol()
	{
		return protocol;
	}

	/**
	 * @param protocol the protocol to set
	 */
	public void setProtocol(String protocol)
	{
		this.protocol = protocol;
	}

	/**
	 * @return the socketTimeoutSec
	 */
	public int getSocketTimeoutSec()
	{
		return socketTimeoutSec;
	}

	/**
	 * @param socketTimeoutSec the socketTimeoutSec to set
	 */
	public void setSocketTimeoutSec(int socketTimeoutSec)
	{
		this.socketTimeoutSec = socketTimeoutSec;
	}

	/**
	 * @return the connectTimeoutSec
	 */
	public int getConnectTimeoutSec()
	{
		return connectTimeoutSec;
	}

	/**
	 * @param connectTimeoutSec the connectTimeoutSec to set
	 */
	public void setConnectTimeoutSec(int connectTimeoutSec)
	{
		this.connectTimeoutSec = connectTimeoutSec;
	}

	/**
	 * @return the login
	 */
	public String getLogin()
	{
		return login;
	}

	/**
	 * @param login the login to set
	 */
	public void setLogin(String login)
	{
		this.login = login;
	}

	/**
	 * @return the password
	 */
	public String getPassword()
	{
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password)
	{
		this.password = password;
	}

	/**
	 * @return the userAgent
	 */
	public String getUserAgent()
	{
		return userAgent;
	}

	/**
	 * @param userAgent the userAgent to set
	 */
	public void setUserAgent(String userAgent)
	{
		this.userAgent = userAgent;
	}

	/**
	 * @return the charset
	 */
	public String getCharset()
	{
		return charset;
	}

	/**
	 * @param charset the charset to set
	 */
	public void setCharset(String charset)
	{
		this.charset = charset;
	}

	/**
	 * @return the addHeaders
	 */
	public Map<String, String> getAddHeaders()
	{
		return addHeaders;
	}

	/**
	 * @param addHeaders the addHeaders to set
	 */
	public void setAddHeaders(Map<String, String> addHeaders)
	{
		this.addHeaders = addHeaders;
	}

	/**
	 * @return the useProxy
	 */
	public boolean isUseProxy()
	{
		return useProxy;
	}

	/**
	 * @param useProxy the useProxy to set
	 */
	public void setUseProxy(boolean useProxy)
	{
		this.useProxy = useProxy;
	}

	/**
	 * @return the proxyHost
	 */
	public String getProxyHost()
	{
		return proxyHost;
	}

	/**
	 * @param proxyHost the proxyHost to set
	 */
	public void setProxyHost(String proxyHost)
	{
		this.proxyHost = proxyHost;
	}

	/**
	 * @return the proxyPort
	 */
	public int getProxyPort()
	{
		return proxyPort;
	}

	/**
	 * @param proxyPort the proxyPort to set
	 */
	public void setProxyPort(int proxyPort)
	{
		this.proxyPort = proxyPort;
	}

	/**
	 * @return the proxyLogin
	 */
	public String getProxyLogin()
	{
		return proxyLogin;
	}

	/**
	 * @param proxyLogin the proxyLogin to set
	 */
	public void setProxyLogin(String proxyLogin)
	{
		this.proxyLogin = proxyLogin;
	}

	/**
	 * @return the proxyPassword
	 */
	public String getProxyPassword()
	{
		return proxyPassword;
	}

	/**
	 * @param proxyPassword the proxyPassword to set
	 */
	public void setProxyPassword(String proxyPassword)
	{
		this.proxyPassword = proxyPassword;
	}

	/**
	 * @return the proxyProtocol
	 */
	public String getProxyProtocol()
	{
		return proxyProtocol;
	}

	/**
	 * @param proxyProtocol the proxyProtocol to set
	 */
	public void setProxyProtocol(String proxyProtocol)
	{
		this.proxyProtocol = proxyProtocol;
	}

	/**
	 * @return the isVersionHttp10
	 */
	public boolean isVersionHttp10()
	{
		return isVersionHttp10;
	}

	/**
	 * @param isVersionHttp10 the isVersionHttp10 to set
	 */
	public void setVersionHttp10(boolean isVersionHttp10)
	{
		this.isVersionHttp10 = isVersionHttp10;
	}
	
	public boolean getIsVersionHttp10()
	{
		return isVersionHttp10;
	}
	
	public void setIsVersionHttp10(boolean isVersionHttp10)
	{
		this.isVersionHttp10 = isVersionHttp10;
	}
}
