package com.pine.chown;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.pine.chown.http.HttpProxy;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Hello world!
 */
public class App implements Callable<String>
{
	/**
	 * 请求地址
	 */
	private String url;

	/**
	 * 请求参数
	 */
	private String queryString;

	/**
	 * @param url
	 * @param queryString
	 */
	public App (String url, String queryString)
	{
		this.url = url;
		this.queryString = queryString;
	}

	/**
	 * 程序入口
	 *
	 * @param args
	 */
	public static void main (String[] args)
	{
		//模拟并发数
		int concurrencyNumber = 200;

		List<String> resList = Lists.newArrayList ();

		//执行线程池
		ExecutorService ex = Executors.newFixedThreadPool (100);

		try
		{
			for (int i = 0; i < concurrencyNumber; i++)
			{
				Future<String> callRes = ex
						.submit (new App ("http://openapi.ethio-play.com/cms/catalogRelation/queryPromotionIndex", ""));
				resList.add (i + ">>>" + callRes.get ());
			}
		}
		catch (InterruptedException e)
		{
			e.printStackTrace ();
		}
		catch (ExecutionException e)
		{
			e.printStackTrace ();
		}

		System.out.println ("总数：------>" + resList.size ());

		for (String s : resList)
		{
			System.out.println ("------>" + s);
		}

		ex.shutdown ();
	}

	/**
	 * Computes a result, or throws an exception if unable to do so.
	 *
	 * @return computed result
	 * @throws Exception if unable to compute a result
	 */
	@Override
	public String call () throws Exception
	{
		Map<String, String> header = Maps.newHashMap ();
		header.put ("X-Language-Id", "en_US");
		header.put ("X-Request-ID", "sdwerw90fslkdj3290irpskdffikopwseifps");
		header.put ("timestamp", "1527858928393");
		return HttpProxy.getInstance ().doGet (url, queryString, header);
	}
}
