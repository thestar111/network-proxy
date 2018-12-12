/**
 * 文 件 名:  HttpProxy
 * 版    权:  Quanten Technologies Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  zping
 * 修改时间:  2017/9/15 0015
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.pine.chown.http;

import org.apache.commons.codec.Charsets;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * <HTTP 发送请求工具>
 *
 * @author Pine Chown
 * @version 2017/9/15 0015
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public final class HttpProxy
{
	/**
	 * HTTP CLIENT 连接对象
	 */
	private static CloseableHttpClient httpClient;

	/**
	 * 请求数据类型
	 */
	private static final String APPLICATION_JSON = "application/json";

	/**
	 * 数据类型：octet-stream
	 */
	private static final String APPLICATION_OCTET_STREAM = "application/octet-stream";

	/**
	 * 默认编码
	 */
	private static final String CONTENT_ENCODE_TYPE = "UTF-8";

	/**
	 * 知识点1：路由(MAX_PER_ROUTE)是对最大连接数（MAX_TOTAL）的细分，整个连接池的限制数量实际使用DefaultMaxPerRoute并非MaxTotal。
	 * 设置过小无法支持大并发(ConnectionPoolTimeoutException: Timeout waiting for connection from pool)
	 */
	private static final int DEFAULT_MAX_TOTAL = 20;

	/**
	 * 针对某个域名的最大连接数
	 */
	private static final int DEFAULT_MAX_PER_ROUTE = 10;

	/**
	 * 知识点2：跟目标服务建立连接超时时间，根据自己的业务调整
	 */
	private static final int DEFAULT_CONNECTION_TIMEOUT = 6000;

	/**
	 * 知识点3：请求的超时时间（建联后，获取response的返回等待时间）
	 */
	private static final int DEFAULT_SOCKET_TIMEOUT = 6000;

	/**
	 * 知识点4：从连接池中获取连接的超时时间
	 */
	private static final int DEFAULT_TIMEOUT = 500;

	/**
	 * 私有构造器
	 */
	private HttpProxy ()
	{
		ConnectionConfig config = ConnectionConfig.custom ().setCharset (Charsets.UTF_8).build ();

		RequestConfig defaultRequestConfig = RequestConfig.custom ().setConnectTimeout (DEFAULT_CONNECTION_TIMEOUT)
				.setSocketTimeout (DEFAULT_SOCKET_TIMEOUT).setConnectionRequestTimeout (DEFAULT_TIMEOUT).build ();

		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager ();
		cm.setDefaultMaxPerRoute (DEFAULT_MAX_PER_ROUTE);
		cm.setDefaultConnectionConfig (config);

		httpClient = HttpClients.custom ().setMaxConnPerRoute (DEFAULT_MAX_PER_ROUTE)
				.setMaxConnTotal (DEFAULT_MAX_TOTAL)
				/*.setRetryHandler((exception, executionCount, context) -> executionCount <= 3 && (exception instanceof NoHttpResponseException
						|| exception instanceof ClientProtocolException
						|| exception instanceof SocketTimeoutException
						|| exception instanceof ConnectTimeoutException))*/
				.setConnectionManager (cm)
				.setDefaultRequestConfig (defaultRequestConfig)
				.build ();
	}

	/**
	 * @param url    请求地址
	 * @param xml    xml报文
	 * @param header 请求头
	 * @return 请求返回json
	 * @throws Exception
	 */
	public String doPost (String url, String xml, Map<String, String> header) throws Exception
	{
		CloseableHttpResponse response = null;
		HttpPost post = null;
		HttpEntity entity = null;
		try
		{
			post = new HttpPost (url);

			post.addHeader (HTTP.CONTENT_TYPE, APPLICATION_JSON);
			StringEntity se = new StringEntity (xml, Consts.UTF_8);

			if (null != header && 0 < header.size ())
			{
				Set<String> headerNames = header.keySet ();
				Iterator<String> its = headerNames.iterator ();
				while (its.hasNext ())
				{
					String heardName = its.next ();
					post.addHeader (heardName, header.get (heardName));
				}
			}

			post.setEntity (se);
			response = httpClient.execute (post);

			if (HttpStatus.SC_OK == response.getStatusLine ().getStatusCode ())
			{
				entity = response.getEntity ();
				if (null != entity)
				{
					String responseStr = EntityUtils.toString (entity, CONTENT_ENCODE_TYPE);
					return responseStr;
				}
			}
			else
			{
				return "";
			}
		}
		catch (HttpHostConnectException e)
		{
			e.printStackTrace ();
			if (null != entity)
			{
				EntityUtils.consume (entity);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace ();
			if (null != entity)
			{
				EntityUtils.consume (entity);
			}
		}
		finally
		{
			if (null != post)
			{
				post.releaseConnection ();
			}

			if (null != entity)
			{
				EntityUtils.consume (entity);
			}

			if (response != null)
			{
				try
				{
					response.close ();
				}
				catch (Exception e)
				{
					e.printStackTrace ();
				}
			}
		}
		return "";
	}

	/**
	 * @param url         请求地址
	 * @param queryString xml报文
	 * @param header      请求头
	 * @return 请求返回json
	 * @throws Exception
	 */
	public String doGet (String url, String queryString, Map<String, String> header) throws Exception
	{
		CloseableHttpResponse response = null;
		HttpGet httpGet = null;
		HttpEntity entity = null;
		try
		{
			httpGet = new HttpGet (url);

			httpGet.addHeader (HTTP.CONTENT_TYPE, APPLICATION_JSON);

			if (null != header && 0 < header.size ())
			{
				Set<String> headerNames = header.keySet ();
				Iterator<String> its = headerNames.iterator ();
				while (its.hasNext ())
				{
					String heardName = its.next ();
					httpGet.addHeader (heardName, header.get (heardName));
				}
			}

			response = httpClient.execute (httpGet);

			if (HttpStatus.SC_OK == response.getStatusLine ().getStatusCode ())
			{
				entity = response.getEntity ();
				if (null != entity)
				{
					String responseStr = EntityUtils.toString (entity, CONTENT_ENCODE_TYPE);
					return responseStr;
				}
			}
			else
			{
				return "";
			}
		}
		catch (HttpHostConnectException e)
		{
			e.printStackTrace ();
			if (null != entity)
			{
				EntityUtils.consume (entity);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace ();
			if (null != entity)
			{
				EntityUtils.consume (entity);
			}
		}
		finally
		{
			if (null != httpGet)
			{
				httpGet.releaseConnection ();
			}

			if (null != entity)
			{
				EntityUtils.consume (entity);
			}

			if (response != null)
			{
				try
				{
					response.close ();
				}
				catch (Exception e)
				{
					e.printStackTrace ();
				}
			}
		}
		return "";
	}

	/**
	 * 静态内部类
	 */
	private static class SingletonHolder
	{
		private static final HttpProxy HTTP_PROXY = new HttpProxy ();
	}

	/**
	 * 获取单例
	 *
	 * @return MultiThreadHttpManager
	 */
	public static final HttpProxy getInstance ()
	{
		return SingletonHolder.HTTP_PROXY;
	}
}
