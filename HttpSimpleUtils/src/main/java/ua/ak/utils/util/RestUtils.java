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
 * утилиты работы с HTTP-клиентом
 * - удобно для потребителей REST-сервисов
 * - удобно для http-запросов  
 * Пример использования:
 * 			ResponseFromHTTP res = RestUtils.getInstance(httpTransportInfo)
			.setUri("/lzE3Loader/scheduler")
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
	 * описание метода REST
	 */
	RequestDefinition restMethodDef=null;
	
	/**
	 * описание транспорта REST
	 */
	HTTPTransportInfo httpTransportInfo=null;
	
	protected RestUtils(HTTPTransportInfo httpTransportInfo)
	{
		this.httpTransportInfo=httpTransportInfo;
		restMethodDef = new RequestDefinition();
	}
	
	
	/**
	 * получить новый экземпляр утилит для работы- начинаем с этого :)
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
	 * добавить новый http-параметр
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
	 * добавить новый http-заголовок
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
	 * установить тип запроса POST
	 * @return
	 */
	public RestUtils setMethodPost()
	{
		restMethodDef.setHttpMethodPost(true);
		return this;
	}

	/**
	 * установить тип запроса GET
	 * @return
	 */
	public RestUtils setMethodGet()
	{
		restMethodDef.setHttpMethodPost(false);
		return this;
	}

	/**
	 * установить тип запроса DELETE
	 * @return
	 */
	public RestUtils setMethodDelete()
	{
		restMethodDef.setHttpMethod(HttpMethod.DELETE);
		return this;
	}

	/**
	 * установить тип запроса OPTIONS
	 * @return
	 */
	public RestUtils setMethodOptions()
	{
		restMethodDef.setHttpMethod(HttpMethod.OPTIONS);
		return this;
	}

	/**
	 * установить тип запроса Put
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
	 * установить тело запроса POST,
	 * Толькто для запросов типа POST
	 *  Если оно установлено, то игнорируются операции addParameter(String, String)  
	 * @param postBody
	 */
	public RestUtils setPostBody(String postBody)
	{
		restMethodDef.setPostBody(postBody);
		return this;
	}

	
	/**
	 * установить признак "тело ответа получить в бинарном виде"
	 * @param binaryResponseBody
	 */
	public RestUtils setBinaryResponseBody(boolean binaryResponseBody)
	{
		restMethodDef.setBinaryResponseBody(binaryResponseBody);
		return this;
	}

	/**
	 * Использовать оригинальный URI
	 * @param useOriginalURI
	 * ==false (default) uri преобразуется через URIBuilder, все праметры из URI извлекаются
	 *         и передаются в соответствии с правилами метода GET, POST ...
	 * ==true  uri НЕ изменяется и передается в запрос как есть, 
	 * для НЕ POST запросов - все addParam  игнорируются(они должны передаться в URI) 
	 * для POST запросов - все addParam  обрабатываются и передаются по правилам POST запросов 
	 * @return
	 */
	public RestUtils setUseOriginalURI(boolean useOriginalURI)
	{
		restMethodDef.setUseOriginalURI(useOriginalURI);
		return this;
	}


	/**
	 * Главный метод  - сделать HTTP-запрос
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
				//A.K. - заглушили проверку сертификата - deprecated from 4.4.1
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
				//в этом случае параметры из URI не добавляются ко всем параметрам
				parsedPath.getParams().clear();
			}
			parsedPath.getParams().addAll(getRequestDefinition().getParams());
			//prepare URI
			URIBuilder uriBuilder = new URIBuilder().setPath(parsedPath.getUri());
			if(!getRequestDefinition().isHttpMethodPost())
			{
				//параметры формы - GET/DELETE/OPTIONS/PUT
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

			//Дополнительные заголовки из httpTransportInfo
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
			//параметры формы POST
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

			//Body формы PUT
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
	 * парсер пути к ресурсу
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
	 * модель - описание запроса для http, например клиента метода REST-сервиса
	 * @author alexk
	 *
	 */
	public static class RequestDefinition
	{
		/**
		 * URI запроса
		 */
		String uri="";
		
		/**Список параметров для передачи**/		
		List<NameValuePair> params=new ArrayList<NameValuePair>();
		
		/**Карта http-заголовков для передачи**/
		List<NameValuePair> headers=new ArrayList<NameValuePair>();
		
		/**HTTP-метод **/
		HttpMethod httpMethod=HttpMethod.POST;
		
		/**
		 *тело запроса для POST/PUT
		 *имеет более высокий приоритет перед пост-параметрами 
		 */
		String postBody=null;
		
		/**получить response body в бинарном виде**/
		boolean binaryResponseBody=false;
		
		/**
		 * Использовать оригинальный URI
		 * ==false (default) uri преобразуется через URIBuilder, все праметры из URI извлекаются
		 *         и передаются в соответствии с правилами метода GET, POST ...
		 * ==true  uri НЕ изменяется и передается в запрос как есть, 
		 * для НЕ POST запросов - все addParam  игнорируются(они должны передаться в URI) 
		 * для POST запросов - все addParam  обрабатываются и передаются по правилам POST запросов 
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
		 * установить тело запроса POST,
		 * Толькто для запросов типа POST
		 *  Если оно установлено, то игнорируются операции addParameter(String, String)  
		 * @param postBody
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
		/**Код возврата по http**/
		int resultCode=-1;
		/**Заголовки HTTP**/
		Header[] allHeaders=null;
		String body="";
		/**body для бинарных ответов**/
		byte[] bodyBin=null;
		/**body - бинарное**/
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
		 * получить список заголовков по имени
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
		 * получить список элементов по имени заголовка и имени элемента
		 * @param headerName
		 * @param elementName - имя элемента (параметра заголовка)
		 * если == null - то все элементы отдаем
		 * @return
		 * если == null - нет такого 
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
