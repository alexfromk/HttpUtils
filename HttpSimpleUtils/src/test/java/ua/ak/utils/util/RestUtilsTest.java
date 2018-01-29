package ua.ak.utils.util;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.Test;

import ua.ak.utils.base.CustomRuntimeException;
import ua.ak.utils.model.HTTPTransportInfo;
import ua.ak.utils.util.RestUtils.ResponseFromHTTP;
import ua.ak.utils.util.mock.MoskHTTPServerStarter;

public class RestUtilsTest
{
	@Test
	public void testMakeGet()
	{
		String uri="/cgi-bin/pay/ws_get.act";
		MoskHTTPServerStarter.startServer();
		MoskHTTPServerStarter.server.setHandler(uri,new GetHandler());
		HTTPTransportInfo transportInfo = MoskHTTPServerStarter.getLocalHTTPTransportInfo();
		try
		{
			Packer packer=new Packer();
			//1
			ResponseFromHTTP res = RestUtils.getInstance(transportInfo)
			.setMethodGet()
			.setUri(uri)
			.makeRequest();			
			assertNotNull(res);
			assertEquals(200,res.getResultCode());
			
			Map<String, String[]> params = packer.unpackParams(res.getBody());
			Map<String, String[]> headers = packer.unpackHeaders(res.getBody());
			
			assertEquals(0,params.size());
			assertTrue(headers.size()>0);
			assertEquals(transportInfo.getUserAgent(),headers.get(HttpHeaders.USER_AGENT)[0]);
			assertEquals("GET",headers.get("Method")[0]);
			
			//2
			res = RestUtils.getInstance(transportInfo)
			.setMethodGet()
			.setUri(uri)
			.addParameter("xxx","val1")
			.addParameter("xxx","val2")
			.addParameter("xxx2","val20")
			.addHeader("hh1","qqq")
			.makeRequest();			
			
			assertNotNull(res);
			assertEquals(200,res.getResultCode());
			
			params = packer.unpackParams(res.getBody());
			headers = packer.unpackHeaders(res.getBody());
			
			assertEquals(2,params.size());
			assertEquals("val2",params.get("xxx")[0]);
			assertEquals("val20",params.get("xxx2")[0]);
			
			assertTrue(headers.size()>0);
			assertEquals(transportInfo.getUserAgent(),headers.get(HttpHeaders.USER_AGENT)[0]);
			assertEquals("qqq",headers.get("hh1")[0]);
			assertEquals("GET",headers.get("Method")[0]);

			//2.5
			res = RestUtils.getInstance(transportInfo)
			.setMethodGet()
			.setUri(uri+"?aa1=100&aa2=2000")
			.addParameter("xxx","val1")
			.addParameter("xxx","val2")
			.addParameter("xxx2","val20")
			.addParameter("aa2","10zzz")
			.addHeader("hh1","qqq")
			.makeRequest();			
			
			assertNotNull(res);			
			assertEquals(200,res.getResultCode());
			assertNull(res.getBodyBin());
			
			params = packer.unpackParams(res.getBody());
			headers = packer.unpackHeaders(res.getBody());
			
			assertEquals(4,params.size());
			assertEquals("val2",params.get("xxx")[0]);
			assertEquals("val20",params.get("xxx2")[0]);
			assertEquals("100",params.get("aa1")[0]);
			assertEquals("10zzz",params.get("aa2")[0]);
			
			assertTrue(headers.size()>0);
			assertEquals(transportInfo.getUserAgent(),headers.get(HttpHeaders.USER_AGENT)[0]);
			assertEquals("qqq",headers.get("hh1")[0]);
			assertEquals("GET",headers.get("Method")[0]);

			//2.5.5 binary body
			res = RestUtils.getInstance(transportInfo)
			.setMethodGet()
			.setUri(uri+"?aa1=100&aa2=2000")
			.addParameter("xxx","val1")
			.addHeader("hh1","qqq")
			.setBinaryResponseBody(true)
			.makeRequest();			
			
			assertNotNull(res);
			assertEquals(200,res.getResultCode());
			assertEquals("",res.getBody());
			assertNotNull(res.getBodyBin());
			String bodyFromBin=new String(res.getBodyBin(),"UTF-8");
			params = packer.unpackParams(bodyFromBin);
			headers = packer.unpackHeaders(bodyFromBin);
			
			assertEquals(3,params.size());
			assertEquals("val1",params.get("xxx")[0]);
			assertEquals("100",params.get("aa1")[0]);
			assertEquals("2000",params.get("aa2")[0]);
			
			assertTrue(headers.size()>0);
			assertEquals(transportInfo.getUserAgent(),headers.get(HttpHeaders.USER_AGENT)[0]);
			assertEquals("qqq",headers.get("hh1")[0]);
			assertEquals("GET",headers.get("Method")[0]);

			//3
			res = RestUtils.getInstance(transportInfo)
			.setMethodGet()
			.setUri(uri+"ccccc")
			.addParameter("xxx","val1")
			.addParameter("xxx","val2")
			.addParameter("xxx2","val20")
			.addHeader("hh1","qqq")
			.makeRequest();			
			assertEquals(404,res.getResultCode());
			
			//4 login-pw
			uri="/cgi-bin/pay/ws_get_pw.act";
			MoskHTTPServerStarter.server.setHandler(uri,new PostHandler3());
			transportInfo.setLogin("login1");
			transportInfo.setPassword("1234567");
			res = RestUtils.getInstance(transportInfo)
					.setUri(uri)
					.setMethodGet()
					.makeRequest();
			assertEquals(200,res.getResultCode());
			headers = packer.unpackHeaders(res.getBody());
			String[] auth=headers.get("Authorization");
			String[] authDetail=auth[0].split("[\\s]+");
			assertEquals("Basic",authDetail[0]);
			System.out.println(new String(Base64.fromString(authDetail[1])));
			assertEquals(transportInfo.getLogin()+":"+transportInfo.getPassword(),new String(Base64.fromString(authDetail[1])));
			//5 постоянные заголовки 			
			transportInfo.setAddHeaders(new HashMap<String,String>());
			transportInfo.getAddHeaders().put("header_test","vvaalluuee 1111");
			transportInfo.getAddHeaders().put("header_test2","vvaalluuee 2");
			transportInfo.getAddHeaders().put("header_test3","");
			res = RestUtils.getInstance(transportInfo)
					.setUri(uri)
					.setMethodGet()
					.makeRequest();
			assertEquals(200,res.getResultCode());
			headers = packer.unpackHeaders(res.getBody());
			assertEquals("vvaalluuee 1111",headers.get("header_test")[0]);
			assertEquals("vvaalluuee 2",headers.get("header_test2")[0]);
			assertNull(headers.get("header_test3"));
			
		}
		catch(UnsupportedEncodingException e)
		{
			e.printStackTrace();
			fail(UtilsCommon.fetchExceptionMessageWithCause(e));			
		}
		catch (CustomRuntimeException e)
		{
			e.printStackTrace();
			fail(UtilsCommon.fetchExceptionMessageWithCause(e));
		}
		
		
		
	}
	
	@Test
	public void testMakePost()
	{
		String uri="/cgi-bin/pay/ws_postt.act";
		String uri2="/cgi-bin/pay/ws_postt2.act";
		MoskHTTPServerStarter.startServer();
		PostHandler postHandler=new PostHandler();
		MoskHTTPServerStarter.server.setHandler(uri,postHandler);
		MoskHTTPServerStarter.server.setHandler(uri2,new PostHandler2());
		HTTPTransportInfo transportInfo = MoskHTTPServerStarter.getLocalHTTPTransportInfo();
		transportInfo.setSocketTimeoutSec(600);
		try
		{
			Packer packer=new Packer();
			//1
			ResponseFromHTTP res = RestUtils.getInstance(transportInfo)
			.setMethodPost()
			.setUri(uri)
			.makeRequest();			
			assertNotNull(res);
			assertEquals(200,res.getResultCode());
			
			Map<String, String[]> params = packer.unpackParams(res.getBody());
			Map<String, String[]> headers = packer.unpackHeaders(res.getBody());
			
			assertEquals(0,params.size());
			assertTrue(headers.size()>0);
			assertEquals(transportInfo.getUserAgent(),headers.get(HttpHeaders.USER_AGENT)[0]);
			assertEquals("POST",headers.get("Method")[0]);
			
			//2
			res = RestUtils.getInstance(transportInfo)
			.setMethodPost()
			.setUri(uri)
			.addParameter("xxx","val1")
			.addParameter("xxx","val2")
			.addParameter("xxx2","val20")
			.addHeader("hh1","qqq")
			.makeRequest();			
			
			assertNotNull(res);
			assertEquals(200,res.getResultCode());
			
			params = packer.unpackParams(res.getBody());
			headers = packer.unpackHeaders(res.getBody());
			
			assertEquals(2,params.size());
			assertEquals("val1",params.get("xxx")[0]);
			assertEquals("val2",params.get("xxx")[1]);
			assertEquals("val20",params.get("xxx2")[0]);
			
			assertTrue(headers.size()>0);
			assertEquals(transportInfo.getUserAgent(),headers.get(HttpHeaders.USER_AGENT)[0]);
			assertEquals("qqq",headers.get("hh1")[0]);
			assertEquals("POST",headers.get("Method")[0]);
			
			assertEquals(uri,postHandler.getUri());

			//2.5 - not useOriginalUri
			res = RestUtils.getInstance(transportInfo)
			.setMethodPost()
			.setUri(uri+"?ppp1=99&zz=3")
			.addParameter("xxx","val1")
			.addParameter("xxx","val2")
			.addParameter("xxx2","val20")
			.addHeader("hh1","qqq")
			.makeRequest();			
			
			assertNotNull(res);
			assertEquals(200,res.getResultCode());
			
			params = packer.unpackParams(res.getBody());
			headers = packer.unpackHeaders(res.getBody());
			
			assertEquals(4,params.size());
			assertEquals("val1",params.get("xxx")[0]);
			assertEquals("val2",params.get("xxx")[1]);
			assertEquals("val20",params.get("xxx2")[0]);
			assertEquals("99",params.get("ppp1")[0]);
			assertEquals("3",params.get("zz")[0]);
			
			assertTrue(headers.size()>0);
			assertEquals(transportInfo.getUserAgent(),headers.get(HttpHeaders.USER_AGENT)[0]);
			assertEquals("qqq",headers.get("hh1")[0]);
			assertEquals("POST",headers.get("Method")[0]);

			assertEquals(uri,postHandler.getUri());
			
			//3 raw post
			res = RestUtils.getInstance(transportInfo)
			.setMethodPost()
			.setUri(uri2)
			.addParameter("xxx","val1")
			.addParameter("xxx","val2")
			.addParameter("xxx2","val20")
			.setPostBody("zzz=1024&zzz2=111")
			.addHeader("hh1","qqq")
			.makeRequest();			
			
			assertNotNull(res);
			assertEquals(200,res.getResultCode());
			assertTrue("Body not start with prefix. Body: "+res.getBody(),res.getBody().startsWith("zzz=1024&zzz2=111"));
			
			//4 useOriginalUri
			String nextUri = uri+"?ppp1=99&zz=3";
			res = RestUtils.getInstance(transportInfo)
			.setMethodPost()
			.setUseOriginalURI(true)
			.setUri(nextUri)
			.addParameter("xxx","val1")
			.addParameter("xxx","val2")
			.addParameter("xxx2","val20")
			.addHeader("hh1","qqq")
			.makeRequest();			
			
			assertNotNull(res);
			assertEquals(200,res.getResultCode());
			
			params = packer.unpackParams(res.getBody());
			headers = packer.unpackHeaders(res.getBody());
			
			assertEquals(4,params.size());
			assertEquals("val1",params.get("xxx")[0]);
			assertEquals("val2",params.get("xxx")[1]);
			assertEquals("val20",params.get("xxx2")[0]);
			assertEquals("99",params.get("ppp1")[0]);
			assertEquals("3",params.get("zz")[0]);
			assertEquals(1,params.get("zz").length);
			
			assertTrue(headers.size()>0);
			assertEquals(transportInfo.getUserAgent(),headers.get(HttpHeaders.USER_AGENT)[0]);
			assertEquals("qqq",headers.get("hh1")[0]);
			assertEquals("POST",headers.get("Method")[0]);
			
			assertEquals(nextUri,postHandler.getUri());
			
		}
		catch (CustomRuntimeException e)
		{
			e.printStackTrace();
			fail(UtilsCommon.fetchExceptionMessageWithCause(e));
		}
		
		
		
	}

	@Test
	public void testMakeDelete()
	{
		String uri="/cgi-bin/pay/ws_delete.act";
		MoskHTTPServerStarter.startServer();
		MoskHTTPServerStarter.server.setHandler(uri,new DeleteHandler());
		HTTPTransportInfo transportInfo = MoskHTTPServerStarter.getLocalHTTPTransportInfo();
		try
		{
			Packer packer=new Packer();
			//1
			ResponseFromHTTP res = RestUtils.getInstance(transportInfo)
			.setMethodDelete()
			.setUri(uri)
			.makeRequest();			
			assertNotNull(res);
			assertEquals(200,res.getResultCode());
			
			Map<String, String[]> params = packer.unpackParams(res.getBody());
			Map<String, String[]> headers = packer.unpackHeaders(res.getBody());
			
			assertEquals(0,params.size());
			assertTrue(headers.size()>0);
			assertEquals(transportInfo.getUserAgent(),headers.get(HttpHeaders.USER_AGENT)[0]);
			assertEquals("DELETE",headers.get("Method")[0]);
			
			//2
			res = RestUtils.getInstance(transportInfo)
			.setMethodDelete()
			.setUri(uri)
			.addParameter("xxx","val1")
			.addParameter("xxx","val2")
			.addParameter("xxx2","val20")
			.addHeader("hh1","qqq")
			.makeRequest();			
			
			assertNotNull(res);
			assertEquals(200,res.getResultCode());
			
			params = packer.unpackParams(res.getBody());
			headers = packer.unpackHeaders(res.getBody());
			
			assertEquals(2,params.size());
			assertEquals("val2",params.get("xxx")[0]);
			assertEquals("val20",params.get("xxx2")[0]);
			
			assertTrue(headers.size()>0);
			assertEquals(transportInfo.getUserAgent(),headers.get(HttpHeaders.USER_AGENT)[0]);
			assertEquals("qqq",headers.get("hh1")[0]);
			assertEquals("DELETE",headers.get("Method")[0]);
			
			//3
			res = RestUtils.getInstance(transportInfo)
			.setMethodDelete()
			.setUri(uri+"ccccc")
			.addParameter("xxx","val1")
			.addParameter("xxx","val2")
			.addParameter("xxx2","val20")
			.addHeader("hh1","qqq")
			.makeRequest();			
			assertEquals(404,res.getResultCode());
			
		}
		catch (CustomRuntimeException e)
		{
			e.printStackTrace();
			fail(UtilsCommon.fetchExceptionMessageWithCause(e));
		}
		
		
		
	}

	@Test
	public void testMakeOptions()
	{
		String uri="/cgi-bin/pay/ws_options.act";
		MoskHTTPServerStarter.startServer();
		MoskHTTPServerStarter.server.setHandler(uri,new OptionsHandler());
		HTTPTransportInfo transportInfo = MoskHTTPServerStarter.getLocalHTTPTransportInfo();
		try
		{
			Packer packer=new Packer();
			//1
			ResponseFromHTTP res = RestUtils.getInstance(transportInfo)
			.setMethodOptions()
			.setUri(uri)
			.makeRequest();			
			assertNotNull(res);
			assertEquals(200,res.getResultCode());
			
			Map<String, String[]> params = packer.unpackParams(res.getBody());
			Map<String, String[]> headers = packer.unpackHeaders(res.getBody());
			
			assertEquals(0,params.size());
			assertTrue(headers.size()>0);
			assertEquals(transportInfo.getUserAgent(),headers.get(HttpHeaders.USER_AGENT)[0]);
			assertEquals("OPTIONS",headers.get("Method")[0]);
			
			//2
			res = RestUtils.getInstance(transportInfo)
			.setMethodOptions()
			.setUri(uri)
			.addParameter("xxx","val1")
			.addParameter("xxx","val2")
			.addParameter("xxx2","val20")
			.addHeader("hh1","qqq")
			.makeRequest();			
			
			assertNotNull(res);
			assertEquals(200,res.getResultCode());
			
			params = packer.unpackParams(res.getBody());
			headers = packer.unpackHeaders(res.getBody());
			
			assertEquals(2,params.size());
			assertEquals("val2",params.get("xxx")[0]);
			assertEquals("val20",params.get("xxx2")[0]);
			
			assertTrue(headers.size()>0);
			assertEquals(transportInfo.getUserAgent(),headers.get(HttpHeaders.USER_AGENT)[0]);
			assertEquals("qqq",headers.get("hh1")[0]);
			assertEquals("OPTIONS",headers.get("Method")[0]);
			
			//3
			res = RestUtils.getInstance(transportInfo)
			.setMethodOptions()
			.setUri(uri+"ccccc")
			.addParameter("xxx","val1")
			.addParameter("xxx","val2")
			.addParameter("xxx2","val20")
			.addHeader("hh1","qqq")
			.makeRequest();			
			assertEquals(404,res.getResultCode());
			
		}
		catch (CustomRuntimeException e)
		{
			e.printStackTrace();
			fail(UtilsCommon.fetchExceptionMessageWithCause(e));
		}
		
	}

	@Test
	public void testMakePut()
	{
		String uri="/cgi-bin/pay/ws_put.act";
		MoskHTTPServerStarter.startServer();
		MoskHTTPServerStarter.server.setHandler(uri,new PutHandler());
		HTTPTransportInfo transportInfo = MoskHTTPServerStarter.getLocalHTTPTransportInfo();
		try
		{
			Packer packer=new Packer();
			//1
			ResponseFromHTTP res = RestUtils.getInstance(transportInfo)
			.setMethodPut()
			.setUri(uri)
			.makeRequest();			
			assertNotNull(res);
			assertEquals(200,res.getResultCode());
			
			Map<String, String[]> params = packer.unpackParams(res.getBody());
			Map<String, String[]> headers = packer.unpackHeaders(res.getBody());
			
			assertEquals(0,params.size());
			assertTrue(headers.size()>0);
			assertEquals(transportInfo.getUserAgent(),headers.get(HttpHeaders.USER_AGENT)[0]);
			assertEquals("PUT",headers.get("Method")[0]);
			
			//2
			res = RestUtils.getInstance(transportInfo)
			.setMethodPut()
			.setUri(uri)
			.addParameter("xxx","val1")
			.addParameter("xxx","val2")
			.addParameter("xxx2","val20")
			.addHeader("hh1","qqq")
			.makeRequest();			
			
			assertNotNull(res);
			assertEquals(200,res.getResultCode());
			
			params = packer.unpackParams(res.getBody());
			headers = packer.unpackHeaders(res.getBody());
			
			assertEquals(2,params.size());
			assertEquals("val2",params.get("xxx")[0]);
			assertEquals("val20",params.get("xxx2")[0]);
			
			assertTrue(headers.size()>0);
			assertEquals(transportInfo.getUserAgent(),headers.get(HttpHeaders.USER_AGENT)[0]);
			assertEquals("qqq",headers.get("hh1")[0]);
			assertEquals("PUT",headers.get("Method")[0]);
			
			//3 raw put
			String uri2="/cgi-bin/pay/ws_put2.act";
			MoskHTTPServerStarter.server.setHandler(uri2,new PutHandler2());
			res = RestUtils.getInstance(transportInfo)
			.setMethodPost()
			.setUri(uri2)
			.addParameter("xxx","val1")
			.addParameter("xxx","val2")
			.addParameter("xxx2","val20")
			.setPostBody("zzz=1024&zzz2=111")
			.addHeader("hh1","qqq")
			.makeRequest();			
			
			assertNotNull(res);
			assertEquals(200,res.getResultCode());
			assertTrue("Body not start with prefix. Body: "+res.getBody(),res.getBody().startsWith("zzz=1024&zzz2=111"));
			
		}
		catch (CustomRuntimeException e)
		{
			e.printStackTrace();
			fail(UtilsCommon.fetchExceptionMessageWithCause(e));
		}
		
	}

	@Test
	public void testPathParser()
	{
		RestUtils.PathParser p=new RestUtils.PathParser("");
		assertEquals("",p.getUri());
		assertEquals(0,p.getParams().size());
		
		p=new RestUtils.PathParser("/someUrl");
		assertEquals("/someUrl",p.getUri());
		assertEquals(0,p.getParams().size());
		
		p=new RestUtils.PathParser("/someUrl?a=10&b=11=1");
		assertEquals("/someUrl",p.getUri());
		assertEquals(2,p.getParams().size());
		assertEquals("a",p.getParams().get(0).getName());
		assertEquals("10",p.getParams().get(0).getValue());
		assertEquals("b",p.getParams().get(1).getName());
		assertEquals("11=1",p.getParams().get(1).getValue());

		p=new RestUtils.PathParser("/someUrl?a=10&b=11=1#11111");
		assertEquals("/someUrl",p.getUri());
		assertEquals(2,p.getParams().size());
		assertEquals("a",p.getParams().get(0).getName());
		assertEquals("10",p.getParams().get(0).getValue());
		assertEquals("b",p.getParams().get(1).getName());
		assertEquals("11=1",p.getParams().get(1).getValue());

		p=new RestUtils.PathParser("/someUrl?a=10&b#11111");
		assertEquals("/someUrl",p.getUri());
		assertEquals(2,p.getParams().size());
		assertEquals("a",p.getParams().get(0).getName());
		assertEquals("10",p.getParams().get(0).getValue());
		assertEquals("b",p.getParams().get(1).getName());
		assertEquals(null,p.getParams().get(1).getValue());

		p=new RestUtils.PathParser("/someUrl?#11111");
		assertEquals("/someUrl",p.getUri());
		assertEquals(0,p.getParams().size());

		p=new RestUtils.PathParser("/someUrl#11111");
		assertEquals("/someUrl",p.getUri());
		assertEquals(0,p.getParams().size());
		
		p=new RestUtils.PathParser("#11111");
		assertEquals("",p.getUri());
		assertEquals(0,p.getParams().size());

		p=new RestUtils.PathParser("/#11111");
		assertEquals("/",p.getUri());
		assertEquals(0,p.getParams().size());

		p=new RestUtils.PathParser("#");
		assertEquals("",p.getUri());
		assertEquals(0,p.getParams().size());

		p=new RestUtils.PathParser("?#");
		assertEquals("",p.getUri());
		assertEquals(0,p.getParams().size());

		p=new RestUtils.PathParser("/someUrl?a=10&b=11=1&c#11111");
		assertEquals("/someUrl",p.getUri());
		assertEquals(3,p.getParams().size());
		assertEquals("a",p.getParams().get(0).getName());
		assertEquals("10",p.getParams().get(0).getValue());
		assertEquals("b",p.getParams().get(1).getName());
		assertEquals("11=1",p.getParams().get(1).getValue());
		assertEquals("c",p.getParams().get(2).getName());
		assertEquals(null,p.getParams().get(2).getValue());

	}
	//@Test
	public void testMakeRequest()
	{
		HTTPTransportInfo httpTransportInfo=new HTTPTransportInfo();
		httpTransportInfo.setHost("scheduler-dev.office.liga.net");
		httpTransportInfo.setPort(8080);
		httpTransportInfo.setUserAgent("RestUtilsTest");
		
		try
		{
			//1
			ResponseFromHTTP res = RestUtils.getInstance(httpTransportInfo)
			.setUri("/lzE3Loader/scheduler")
			.addParameter("aa","wwww")
			.addHeader("head_test","head_aaaaa")
			.makeRequest();
			
			assertEquals(200,res.getResultCode());
			assertTrue(res.getAllHeaders().length>0);
			assertTrue(res.getBody().length()>0);
			for( Header h : res.getAllHeaders())
			{
				System.out.println(h.getName()+": "+h.getValue());
			}
			System.out.println(res.getBody());
			//2
			res = RestUtils.getInstance(httpTransportInfo)
			.setUri("/lzE3Loader/scheduler")
			.addParameter("aa","wwww")
			.addHeader("head_test","head_aaaaa")
			.setMethodGet()
			.makeRequest();
			
			assertEquals(200,res.getResultCode());
			assertTrue(res.getAllHeaders().length>0);
			assertTrue(res.getBody().length()>0);
			for( Header h : res.getAllHeaders())
			{
				System.out.println(h.getName()+": "+h.getValue());
			}
			System.out.println(res.getBody());

		}
		catch (CustomRuntimeException e)
		{
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	public static class Packer
	{
		public static final String delLine="\r\n";
		public static final String delKeyVal="==";
		public static final String delVals="~";

		public static final String paramBegin="<!--param begin-->";
		public static final String paramEnd="<!--param end-->";

		public static final String headersBegin="<!--headers begin-->";
		public static final String headersEnd="<!--headers end-->";

		String packParams(HttpServletRequest request)
		{
			StringBuffer sb=new StringBuffer();
			sb.append(paramBegin);
			@SuppressWarnings("unchecked")
			Enumeration<String> enumNames = request.getParameterNames();		
			while(enumNames.hasMoreElements())
			{
				String paramName=enumNames.nextElement();
				String[] paramVal=request.getParameterValues(paramName);
				sb.append(paramName);
				sb.append(delKeyVal);
				sb.append(StringUtils.join(paramVal,delVals));
				sb.append(delLine);
			}
			sb.append(paramEnd);
			return sb.toString();
		}

		Map<String,String[]> unpack(String params,String begin,String end)
		{
			Map<String,String[]> result=new HashMap<String,String[]>();
			if(StringUtils.isBlank(params))
				return result;

			int pos0=params.indexOf(begin);
			if(pos0<0)
				return result;

			int pos1=params.indexOf(end,begin.length());
			if(pos1<0)
				return result;

			params=params.substring(pos0+begin.length(),pos1);
			String[] lines=params.split(delLine);
			for(String line: lines)
			{
				String[] vals=line.split(delKeyVal);
				if(vals.length>=2)
				{
					String[] arr=result.get(vals[0]);
					String[] vals1=vals[1].split(delVals);
					if(arr!=null)
					{
						vals1=(String[])ArrayUtils.addAll(arr,vals1);
					}					
					result.put(vals[0],vals1);
				}			
			}
			return result;
		}

		Map<String,String[]> unpackParams(String params)
		{
			return unpack(params,paramBegin,paramEnd);
		}

		Map<String,String[]> unpackHeaders(String params)
		{
			return unpack(params,headersBegin,headersEnd);
		}

		String packHeaders(HttpServletRequest request)
		{
			StringBuffer sb=new StringBuffer();
			sb.append(headersBegin);	
			
			sb.append("Method");
			sb.append(delKeyVal);
			sb.append(request.getMethod());
			sb.append(delLine);

			@SuppressWarnings("unchecked")
			Enumeration<String> enumNames = request.getHeaderNames();
			if(enumNames!=null)
			{
				while(enumNames.hasMoreElements())
				{
					String paramName=enumNames.nextElement();
					String paramVal=request.getHeader(paramName);
					sb.append(paramName);
					sb.append(delKeyVal);
					sb.append(paramVal);
					sb.append(delLine);
				}
			}
			sb.append(headersEnd);
			return sb.toString();
		}

	}
	
	public class GetHandler extends AbstractHandler
	{
		@Override
	    public void handle(String target,Request baseRequest,HttpServletRequest request,HttpServletResponse response) 
	        throws IOException, ServletException
	    {
			Packer packer=new Packer();
			String params = packer.packParams(request);
			String headers=	packer.packHeaders(request);
			
	        response.setContentType(MoskHTTPServerStarter.server.getDefaultContentType());
	        response.setStatus(HttpServletResponse.SC_OK);
	        baseRequest.setHandled(true);	        
	        response.getWriter().println(params+"\r\n"+headers);
	    }
	}

	public class PostHandler extends AbstractHandler
	{
		String uri;
		@Override
	    public void handle(String target,Request baseRequest,HttpServletRequest request,HttpServletResponse response) 
	        throws IOException, ServletException
	    {	
			uri=request.getRequestURI();
			if(StringUtils.isNotBlank(request.getQueryString()))
			{
				uri+="?"+request.getQueryString();
			}
			
			Packer packer=new Packer();
			String params = packer.packParams(request);
			String headers=	packer.packHeaders(request);
					
	        response.setContentType(MoskHTTPServerStarter.server.getDefaultContentType());
	        response.setStatus(HttpServletResponse.SC_OK);
	        baseRequest.setHandled(true);	        
	        response.getWriter().println(params+"\r\n"+headers);
	    }
		/**
		 * @return the uri
		 */
		public String getUri()
		{
			return uri;
		}
		
	}

	public class PostHandler2 extends AbstractHandler
	{
		@Override
	    public void handle(String target,Request baseRequest,HttpServletRequest request,HttpServletResponse response) 
	        throws IOException, ServletException
	    {			
//			Packer packer=new Packer();
//			String params = packer.packParams(request);
//			String headers=	packer.packHeaders(request);
		
			String params=new String(IOUtils.toByteArray(request.getReader(),"utf-8"));
			
	        response.setContentType(MoskHTTPServerStarter.server.getDefaultContentType());
	        response.setStatus(HttpServletResponse.SC_OK);
	        baseRequest.setHandled(true);	        
	        response.getWriter().println(params);
	    }
	}

	public class PostHandler3 extends AbstractHandler
	{
		@Override
	    public void handle(String target,Request baseRequest,HttpServletRequest request,HttpServletResponse response) 
	        throws IOException, ServletException
	    {			
			Packer packer=new Packer();
			String params = packer.packParams(request);
			String headers=	packer.packHeaders(request);
					
	        response.setContentType(MoskHTTPServerStarter.server.getDefaultContentType());
	        response.setStatus(HttpServletResponse.SC_OK);
	        baseRequest.setHandled(true);	        
	        response.getWriter().println(params+"\r\n"+headers);
	    }
	}
	
	public class DeleteHandler extends AbstractHandler
	{
		@Override
	    public void handle(String target,Request baseRequest,HttpServletRequest request,HttpServletResponse response) 
	        throws IOException, ServletException
	    {
			Packer packer=new Packer();
			String params = packer.packParams(request);
			String headers=	packer.packHeaders(request);
			
	        response.setContentType(MoskHTTPServerStarter.server.getDefaultContentType());
	        response.setStatus(HttpServletResponse.SC_OK);
	        baseRequest.setHandled(true);	        
	        response.getWriter().println(params+"\r\n"+headers);
	    }
	}

	public class OptionsHandler extends AbstractHandler
	{
		@Override
	    public void handle(String target,Request baseRequest,HttpServletRequest request,HttpServletResponse response) 
	        throws IOException, ServletException
	    {
			Packer packer=new Packer();
			String params = packer.packParams(request);
			String headers=	packer.packHeaders(request);
			
	        response.setContentType(MoskHTTPServerStarter.server.getDefaultContentType());
	        response.setStatus(HttpServletResponse.SC_OK);
	        baseRequest.setHandled(true);	        
	        response.getWriter().println(params+"\r\n"+headers);
	    }
	}

	public class PutHandler extends AbstractHandler
	{
		@Override
	    public void handle(String target,Request baseRequest,HttpServletRequest request,HttpServletResponse response) 
	        throws IOException, ServletException
	    {
			Packer packer=new Packer();
			String params = packer.packParams(request);
			String headers=	packer.packHeaders(request);
			
	        response.setContentType(MoskHTTPServerStarter.server.getDefaultContentType());
	        response.setStatus(HttpServletResponse.SC_OK);
	        baseRequest.setHandled(true);	        
	        response.getWriter().println(params+"\r\n"+headers);
	    }
	}

	public class PutHandler2 extends AbstractHandler
	{
		@Override
	    public void handle(String target,Request baseRequest,HttpServletRequest request,HttpServletResponse response) 
	        throws IOException, ServletException
	    {			
		
			String params=new String(IOUtils.toByteArray(request.getReader(),"utf-8"));
			
	        response.setContentType(MoskHTTPServerStarter.server.getDefaultContentType());
	        response.setStatus(HttpServletResponse.SC_OK);
	        baseRequest.setHandled(true);	        
	        response.getWriter().println(params);
	    }
	}

}
