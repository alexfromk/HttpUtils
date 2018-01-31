package ua.ak.utils.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import ua.ak.utils.base.CustomRuntimeException;
import ua.ak.utils.model.HTTPTransportInfo;



/**
 * Utility class for simplified work with Apache HTTP Client  
 * Example for use:
 * 
 * HttpTransportInfo httpTransportInfo=new HttpTransportInfo();
 * //ini http transport parameters
 * 
 *  //make request
 * 			ResponseFromHTTP res = RestUtils.getInstance(httpTransportInfo)
			.setUri("/loader/scheduler")
			.addParameter("aa","wwww")
			.addHeader("head_test","head_aaaaa")
			.setMethodGet()
			.makeRequest();
 * @author alexk
 *
 */
@SuppressWarnings("deprecation")
public class RestUtils
{
	/**
	 * Request definition
	 */
	RequestDefinition restMethodDef=null;
	
	/**
	 * Transport definition
	 */
	HTTPTransportInfo httpTransportInfo=null;
	
	protected RestUtils(HTTPTransportInfo httpTransportInfo)
	{
		this.httpTransportInfo=httpTransportInfo;
		restMethodDef = new RequestDefinition();
	}
	
	
	/**
	 * Get new instance of RestUtils
	 * First method for begin work
	 * @param httpTransportInfo
	 * @return
	 */
	public static RestUtils getInstance(HTTPTransportInfo httpTransportInfo)
	{		
		return new RestUtils(httpTransportInfo);
	}
	
	
	/**
	 * @return
	 * @see ua.lz.crm.utilscommon.utils.RestUtils.RequestDefinition#getUri()
	 */
	public String getUri()
	{
		return restMethodDef.getUri();
	}


	/**
	 * установить текущий URI (путь без хоста и параметров :))
	 * @param uri
	 * @see ua.lz.crm.utilscommon.utils.RestUtils.RequestDefinition#setUri(java.lang.String)
	 */
	public RestUtils setUri(String uri)
	{
		restMethodDef.setUri(uri);
		return this;
	}


	/**
	 * Add new http-parameter
	 * @param name
	 * @param value
	 * @return
	 */
	public RestUtils addParameter(String name, String value)
	{
		restMethodDef.getParams().add(new BasicNameValuePair(name, value));
		return this;
	}

	/**
	 * Add new http-header
	 * @param name
	 * @param value
	 * @return
	 */
	public RestUtils addHeader(String name, String value)
	{
		restMethodDef.getHeaders().add(new BasicNameValuePair(name, value));
		return this;
	}

	/**
	 * Set request method - POST
	 * @return
	 */
	public RestUtils setMethodPost()
	{
		restMethodDef.setHttpMethodPost(true);
		return this;
	}

	/**
	 * Set request method - GET
	 * @return
	 */
	public RestUtils setMethodGet()
	{
		restMethodDef.setHttpMethodPost(false);
		return this;
	}

	/**
	 * Set request method - DELETE
	 * @return
	 */
	public RestUtils setMethodDelete()
	{
		restMethodDef.setHttpMethod(HttpMethod.DELETE);
		return this;
	}

	/**
	 * Set request method - OPTIONS
	 * @return
	 */
	public RestUtils setMethodOptions()
	{
		restMethodDef.setHttpMethod(HttpMethod.OPTIONS);
		return this;
	}

	/**
	 * Set request method - Put
	 * @return
	 */
	public RestUtils setMethodPut()
	{
		restMethodDef.setHttpMethod(HttpMethod.PUT);
		return this;
	}

	public String getPostBody()
	{
		return restMethodDef.getPostBody();
	}

	/**
	 * Set body for POST-request,
	 * Only for POST-request,
	 * If it was set, then will be ignored any call like addParameter(String, String)  
	 * @param postBody
	 */
	public RestUtils setPostBody(String postBody)
	{
		restMethodDef.setPostBody(postBody);
		return this;
	}

	
	/**
	 * Set option "obtain body in binary format" (will be save into ByteArray[])
	 * @param binaryResponseBody
	 */
	public RestUtils setBinaryResponseBody(boolean binaryResponseBody)
	{
		restMethodDef.setBinaryResponseBody(binaryResponseBody);
		return this;
	}

	/**
	 * Forced use original URI
	 * @param useOriginalURI
	 * ==false (default) uri will transformed by URIBuilder, all parameters will be extracted from URI
	 *         and are passed in accordance with the rules of the GET method, POST ...
	 * == true uri is NOT changed and sent to the request as is,
	 * for NOT POST requests - all addParams are ignored (they must be passed to the URI)
	 * for POST requests - all addParam are processed and passed according to the rules of POST requests	 * @return
	 */
	public RestUtils setUseOriginalURI(boolean useOriginalURI)
	{
		restMethodDef.setUseOriginalURI(useOriginalURI);
		return this;
	}


	/**
	 * Main final method  - make HTTP-request
	 * @return
	 * @throws ExceptionLZCommon
	 */
	public ResponseFromHTTP makeRequest() throws CustomRuntimeException
	{
		ResponseFromHTTP result=null;
		CloseableHttpClient httpclient=null;
		CloseableHttpResponse response=null;
		boolean needAuth=false;
		boolean viaProxy=httpTransportInfo.isUseProxy() && StringUtils.isNotBlank(httpTransportInfo.getProxyHost());

		HttpHost target = new HttpHost(httpTransportInfo.getHost(), httpTransportInfo.getPort(),httpTransportInfo.getProtocol());
		//basic auth for request
		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		if(StringUtils.isNotBlank(httpTransportInfo.getLogin()) && 
				StringUtils.isNotBlank(httpTransportInfo.getPassword()))
		{
			credsProvider.setCredentials(
					new AuthScope(target.getHostName(), target.getPort()),
					new UsernamePasswordCredentials(httpTransportInfo.getLogin(), httpTransportInfo.getPassword()));
			needAuth=true;
		}

		//proxy auth?
		if(viaProxy && StringUtils.isNotBlank(httpTransportInfo.getProxyLogin()) &&
				StringUtils.isNotBlank(httpTransportInfo.getProxyPassword()))
		{
			//proxy auth setting
			credsProvider.setCredentials(
					new AuthScope(httpTransportInfo.getProxyHost(), httpTransportInfo.getProxyPort()),
					new UsernamePasswordCredentials(httpTransportInfo.getProxyLogin(), httpTransportInfo.getProxyPassword()));
		}

		try
		{

			HttpClientBuilder httpclientBuilder = HttpClients.custom()
					.setDefaultCredentialsProvider(credsProvider);
			//https
			if("https".equalsIgnoreCase(httpTransportInfo.getProtocol()))
			{
				//A.K. - Set stub for check certificate - deprecated from 4.4.1
				SSLContextBuilder sslContextBuilder=SSLContexts.custom();
				TrustStrategyLZ trustStrategyLZ=new TrustStrategyLZ();
				sslContextBuilder.loadTrustMaterial(null,trustStrategyLZ);
				SSLContext sslContext = sslContextBuilder.build();
				SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext,SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
				httpclientBuilder.setSSLSocketFactory(sslsf);
			}

			httpclient=httpclientBuilder.build();

			//timeouts
			Builder builder = RequestConfig.custom()
					.setSocketTimeout(httpTransportInfo.getSocketTimeoutSec()*1000)
					.setConnectTimeout(httpTransportInfo.getConnectTimeoutSec()*1000);

			//via proxy?
			if(viaProxy)
			{
				//proxy setting
				HttpHost proxy = new HttpHost(httpTransportInfo.getProxyHost(),httpTransportInfo.getProxyPort(),httpTransportInfo.getProxyProtocol());
				builder.setProxy(proxy);
			}

			RequestConfig requestConfig=builder.build();

			// Create AuthCache instance
			AuthCache authCache = new BasicAuthCache();
			// Generate BASIC scheme object and add it to the local
			// auth cache
			BasicScheme basicAuth = new BasicScheme();
			authCache.put(target, basicAuth);

			// Add AuthCache to the execution context
			HttpClientContext localContext = HttpClientContext.create();
			localContext.setAuthCache(authCache);

			PathParser parsedPath=new PathParser(getUri());
			if(restMethodDef.isUseOriginalURI())
			{
				//in this case params from URI will not add to all params
				parsedPath.getParams().clear();
			}
			parsedPath.getParams().addAll(getRequestDefinition().getParams());
			//prepare URI
			URIBuilder uriBuilder = new URIBuilder().setPath(parsedPath.getUri());
			if(!getRequestDefinition().isHttpMethodPost())
			{
				//form's parameters - GET/DELETE/OPTIONS/PUT
				for(NameValuePair nameValuePair : parsedPath.getParams())
				{
					uriBuilder.setParameter(nameValuePair.getName(),nameValuePair.getValue());
				}					
			}
			
			URI resultUri =(restMethodDef.isUseOriginalURI())?new URI(getUri()):uriBuilder.build();

			//			HttpRequestBase httpPostOrGet=(getRequestDefinition().isHttpMethodPost())?
			//					new HttpPost(resultUri):
			//					new HttpGet(resultUri);

			HttpRequestBase httpPostOrGetEtc=null;
			//
			switch(getRequestDefinition().getHttpMethod())
			{
				case POST: httpPostOrGetEtc=new HttpPost(resultUri);break;
				case DELETE: httpPostOrGetEtc=new HttpDelete(resultUri);break;
				case OPTIONS: httpPostOrGetEtc=new HttpOptions(resultUri);break;
				case PUT: httpPostOrGetEtc=new HttpPut(resultUri);break;

				default: httpPostOrGetEtc=new HttpGet(resultUri);break;
			}

			//Specifie protocol version
			if(httpTransportInfo.isVersionHttp10())
				httpPostOrGetEtc.setProtocolVersion(HttpVersion.HTTP_1_0);

			//user agent
			httpPostOrGetEtc.setHeader(HttpHeaders.USER_AGENT,httpTransportInfo.getUserAgent());
			//заголовки из запроса
			if(getRequestDefinition().getHeaders().size()>0)
			{
				for(NameValuePair nameValuePair : getRequestDefinition().getHeaders())
				{
					if(org.apache.commons.lang.StringUtils.isNotBlank(nameValuePair.getName()) &&
							org.apache.commons.lang.StringUtils.isNotBlank(nameValuePair.getValue()))
					{
						httpPostOrGetEtc.setHeader(nameValuePair.getName(),nameValuePair.getValue());
					}					
				}
			}

			//Additional HTTP headers from httpTransportInfo
			if(httpTransportInfo.getAddHeaders()!=null)
			{
				for(Map.Entry<String, String> entry:httpTransportInfo.getAddHeaders().entrySet())
				{
					if(org.apache.commons.lang.StringUtils.isNotBlank(entry.getKey()) &&
							org.apache.commons.lang.StringUtils.isNotBlank(entry.getValue()))
					{
						httpPostOrGetEtc.setHeader(entry.getKey(),entry.getValue());
					}
				}
			}
			httpPostOrGetEtc.setConfig(requestConfig);
			//Form's parameters, request POST
			if(getRequestDefinition().isHttpMethodPost())
			{
				if(getPostBody()!=null)
				{
					StringEntity stringEntity=new StringEntity(getPostBody(),httpTransportInfo.getCharset());
					((HttpPost) (httpPostOrGetEtc)).setEntity(stringEntity);
				}
				else if(parsedPath.getParams().size()>0)
				{
					UrlEncodedFormEntity entityForm = new UrlEncodedFormEntity(parsedPath.getParams(), httpTransportInfo.getCharset());
					((HttpPost) (httpPostOrGetEtc)).setEntity(entityForm);
				}
			}

			//Body for PUT
			if(getRequestDefinition().getHttpMethod()==HttpMethod.PUT)
			{
				if(getPostBody()!=null)
				{
					StringEntity stringEntity=new StringEntity(getPostBody(),httpTransportInfo.getCharset());
					((HttpPut) (httpPostOrGetEtc)).setEntity(stringEntity);
				}
			}

			response = httpclient.execute(target, httpPostOrGetEtc, (needAuth)?localContext:null);
			//response.
			HttpEntity entity = response.getEntity();
			if(entity!=null)
			{
				//charset
				ContentType contentType = ContentType.get(entity);
				String currentCharSet=httpTransportInfo.getCharset();
				if(contentType!=null)
				{
					//String mimeType = contentType.getMimeType();
					if(contentType.getCharset()!=null)
						currentCharSet=contentType.getCharset().name();
				}
				InputStream inputStream=entity.getContent();
				if(getRequestDefinition().isBinaryResponseBody())
				{
					//binary content
					byte[] bodyBin = IOUtils.toByteArray(inputStream);
					result=new ResponseFromHTTP(response.getAllHeaders(),bodyBin,response.getStatusLine().getStatusCode());
				}
				else
				{
					//copy content to string
					StringWriter writer = new StringWriter();
					IOUtils.copy(inputStream, writer, currentCharSet);
					result=new ResponseFromHTTP(response.getAllHeaders(),writer.toString(),response.getStatusLine().getStatusCode());
				}
				inputStream.close();
			}
		}
		catch (Exception e)
		{
			throw new CustomRuntimeException("fetchData over http uri: "+getUri(),e);
		}
		finally
		{
			try
			{
				if(response!=null)
					response.close();
				if(httpclient!=null)
					httpclient.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}        	
		}		
		return result;

	}
	
	/**
	 * Path parser to resource
	 * @author alexk
	 *
	 */
	public static class PathParser
	{
		String uri;
		List<NameValuePair> params=new ArrayList<NameValuePair>();

		public PathParser(String uri)
		{
			super();
			String pathWithoutAnchor = withoutAnchor(uri);
			if(StringUtils.isBlank(pathWithoutAnchor))
			{
				this.uri = pathWithoutAnchor;
			}
			else
			{
				String[] parts = pathWithoutAnchor.split("\\?",2);
				this.uri=parts[0];
				if(parts.length>1 && StringUtils.isNotBlank(parts[1]))
				{
					String[] pairs = parts[1].split("&");
						for(String pair : pairs)
						{
							String[] keyValue = pair.split("=",2);
							NameValuePair item=null; 
							if(keyValue.length==1)
							{
								item=new BasicNameValuePair(keyValue[0], null);
							}
							else if(keyValue.length>1) 
							{								
								item=new BasicNameValuePair(keyValue[0],keyValue[1]);
							}
							if(item!=null)
								params.add(item);
						}
				}
			}
		}
		
		String withoutAnchor(String path)
		{
			if(StringUtils.isBlank(path))
				return path;
			String[] partsWithoutAnchor = path.split("#",2);
			return partsWithoutAnchor[0];
		}
		/**
		 * @return the uri
		 */
		public String getUri()
		{
			return uri;
		}
		/**
		 * @return the params
		 */
		public List<NameValuePair> getParams()
		{
			return params;
		}				
	}
	
	/**
	 * Data transfer object - request definition
	 * @author alexk
	 *
	 */
	public static class RequestDefinition
	{
		/**
		 * URI for request
		 */
		String uri="";
		
		/**Parameter list for transmission**/		
		List<NameValuePair> params=new ArrayList<NameValuePair>();
		
		/**Map of http-headers for transmission**/
		List<NameValuePair> headers=new ArrayList<NameValuePair>();
		
		/**HTTP-method **/
		HttpMethod httpMethod=HttpMethod.POST;
		
		/**
		 * Request body for POST / PUT
		 * has a higher priority over post-parameters		 
		 */
		String postBody=null;
		
		/** get response body in binary form**/
		boolean binaryResponseBody=false;
		
		/**
		 * Use the original URI
		 * == false (default) uri is converted via URIBuilder, all the parameters from the URI are extracted
		 * and are passed in accordance with the rules of the GET method, POST ...
		 * == true uri is NOT changed and sent to the request as is,
		 * for NOT POST requests - all addParams are ignored (they must be passed to the URI)
		 * for POST requests - all addParam are processed and passed according to the rules of POST requests
		 */
		boolean useOriginalURI=false;
		/**
		 * @return the params
		 */
		public List<NameValuePair> getParams()
		{
			return params;
		}

		/**
		 * @param params the params to set
		 */
		public void setParams(List<NameValuePair> params)
		{
			this.params = params;
		}

		/**
		 * @return the headers
		 */
		public List<NameValuePair> getHeaders()
		{
			return headers;
		}

		/**
		 * @param headers the headers to set
		 */
		public void setHeaders(List<NameValuePair> headers)
		{
			this.headers = headers;
		}

		/**
		 * @return the httpMethodPost
		 */
		public boolean isHttpMethodPost()
		{
			return httpMethod.equals(HttpMethod.POST);
		}

		/**
		 * @param httpMethodPost the httpMethodPost to set
		 */
		public void setHttpMethodPost(boolean httpMethodPost)
		{
			if(httpMethodPost)
				httpMethod=HttpMethod.POST;
			else
				httpMethod=HttpMethod.GET;
		}

		/**
		 * @return the uri
		 */
		public String getUri()
		{
			return uri;
		}

		/**
		 * @param uri the uri to set
		 */
		public void setUri(String uri)
		{
			this.uri = uri;
		}

		public String getPostBody()
		{
			return postBody;
		}

		/**
		 * set the body of the POST request,
		 * Only for POST type requests
		 * If it is set, the operations addParameter (String, String)		 * @param postBody
		 */
		public void setPostBody(String postBody)
		{
			this.postBody = postBody;
		}

		public HttpMethod getHttpMethod()
		{
			return httpMethod;
		}

		public void setHttpMethod(HttpMethod httpMethod)
		{
			this.httpMethod = httpMethod;
		}

		/**
		 * @return the binaryResponseBody
		 */
		public boolean isBinaryResponseBody()
		{
			return binaryResponseBody;
		}

		/**
		 * @param binaryResponseBody the binaryResponseBody to set
		 */
		public void setBinaryResponseBody(boolean binaryResponseBody)
		{
			this.binaryResponseBody = binaryResponseBody;
		}

		/**
		 * @return the useOriginalURI
		 */
		public boolean isUseOriginalURI()
		{
			return useOriginalURI;
		}

		/**
		 * @param useOriginalURI the useOriginalURI to set
		 */
		public void setUseOriginalURI(boolean useOriginalURI)
		{
			this.useOriginalURI = useOriginalURI;
		}
		
	}

	public static class ResponseFromHTTP
	{
		/**Return code by http**/
		int resultCode=-1;
		/**HTTP Headers**/
		Header[] allHeaders=null;
		String body="";
		/**body for binary responses**/
		byte[] bodyBin=null;
		/**is binary body - **/
		boolean binaryBody=false;
		
		public ResponseFromHTTP()
		{
			super();
		}
		
		public ResponseFromHTTP(Header[] allHeaders, String body,int resultCode)
		{
			super();
			this.allHeaders = (allHeaders==null)? new Header[0]:Arrays.copyOf(allHeaders,allHeaders.length);
			this.body = body;
			this.resultCode=resultCode;
		}

		public ResponseFromHTTP(Header[] allHeaders, byte[] bodyBin,int resultCode)
		{
			super();
			this.allHeaders = (allHeaders==null)? new Header[0]:Arrays.copyOf(allHeaders,allHeaders.length);
			this.bodyBin = bodyBin;
			this.resultCode=resultCode;
			this.binaryBody=this.bodyBin!=null;
		}

		/**
		 * get a list of headers by name
		 * @param name
		 * @return
		 * ==null - not found
		 */
		public List<Header>getHeaderByName(String name)
		{
			if(allHeaders==null)
				return null;
			List<Header> result=null;
			for(Header header : allHeaders)
			{
				if(header.getName().equalsIgnoreCase(name))
				{
					if(result==null)
						result=new ArrayList<Header>();
					result.add(header);
				}
			}
			return result;
		}
		
		/**
		 * get a list of items by name and item name
		 * @param headerName
		 * @param elementName - name of the element (header parameter)
		 * if == null - then all the elements are given
		 * @return
		 * if == null - there is no such 
		 */
		public List<String>getHeaderElementByName(String headerName,String elementName)
		{
			List<Header>headers=getHeaderByName(headerName);
			if(headers==null)
				return null;
			List<String> result=null;
			for(Header header : headers)
			{
				for(HeaderElement he : header.getElements())
				{	
					if(he.getName().equalsIgnoreCase(elementName) && he.getValue()!=null)
					{
						if(result==null)
							result=new ArrayList<String>();
						result.add(he.getValue());
					}						
				}
			}
			return result;
		}
		
		/**
		 * @return the allHeaders
		 */
		public Header[] getAllHeaders()
		{
			return allHeaders;
		}
		/**
		 * @param allHeaders the allHeaders to set
		 */
		public void setAllHeaders(Header[] allHeaders)
		{
			this.allHeaders = allHeaders;
		}
		/**
		 * @return the body
		 */
		public String getBody()
		{
			return body;
		}
		/**
		 * @param body the body to set
		 */
		public void setBody(String body)
		{
			this.body = body;
		}

		/**
		 * @return the resultCode
		 */
		public int getResultCode()
		{
			return resultCode;
		}

		/**
		 * @param resultCode the resultCode to set
		 */
		public void setResultCode(int resultCode)
		{
			this.resultCode = resultCode;
		}

		/**
		 * @return the bodyBin
		 */
		public byte[] getBodyBin()
		{
			return bodyBin;
		}

		/**
		 * @param bodyBin the bodyBin to set
		 */
		public void setBodyBin(byte[] bodyBin)
		{
			this.bodyBin = bodyBin;
		}

		/**
		 * @return the binariBody
		 */
		public boolean isBinaryBody()
		{
			return binaryBody;
		}

		/**
		 * @param binariBody the binariBody to set
		 */
		public void setBinaryBody(boolean binaryBody)
		{
			this.binaryBody = binaryBody;
		}
	
		
	}

	public static enum HttpMethod
	{
		GET,POST,DELETE,OPTIONS,PUT;
	}
	
	public static class TrustStrategyLZ implements TrustStrategy
	{

		public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException
		{
			// all trusted
			return true;
		}
		
	}
	/**
	 * @return the httpTransportInfo
	 */
	public HTTPTransportInfo getHttpTransportInfo()
	{
		return httpTransportInfo;
	}

	/**
	 * @param httpTransportInfo the httpTransportInfo to set
	 */
	public void setHttpTransportInfo(HTTPTransportInfo httpTransportInfo)
	{
		this.httpTransportInfo = httpTransportInfo;
	}

	/**
	 * @return the restMethodDef
	 */
	public RequestDefinition getRequestDefinition()
	{
		return restMethodDef;
	}
	
	
}
