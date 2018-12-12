package com.pine.chown;

import com.google.common.collect.Lists;
import com.pine.chown.http.HttpProxy;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
	public static void main (String[] args) throws Exception
	{
		//模拟并发数
		int concurrencyNumber = 200;

		List<String> resList = Lists.newArrayList ();

		//执行线程池
		ExecutorService ex = Executors.newFixedThreadPool (100);

		for (int i = 0; i < concurrencyNumber; i++)
		{
			Future<String> callRes = ex.submit (new App ("http://www.baidu.com", ""));
			resList.add (i + ">>>" + callRes.get ());
		}

		ex.shutdown ();

		for (String s : resList)
		{
			System.out.println ("------>" + s);
		}
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
		return HttpProxy.getInstance ().doGet (url, queryString, null);
	}
}
