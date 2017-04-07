/******************************************************************************
 * Copyright (C) 2016 Dksj WorkRoom
 * All Rights Reserved.
 * 本软件为深圳大咖时尚设计有限公司开发研制。未经本公司正式书面同意，其他任何个人、团体不得使用、
 * 复制、修改或发布本软件.
 ******************************************************************************/
/**   
 * @Title: Test.java 
 * @Package com.dksj 
 * @Description: (用一句话描述该文件做什么) 
 * @author yuanhualiang  
 * @date 2017年4月5日 下午5:48:12 
 * @since JDK 1.7
 * @version 
 **/
package com.dksj;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @ClassName: Test
 * @Description: (这里用一句话描述这个类的作用)
 * @author yuanhualiang
 * @date 2017年4月5日 下午5:48:12
 * @since JDK 1.7
 */
public class testConcurrentExecutor {

	private static ExecutorService executor = Executors.newFixedThreadPool(200);

	public static void main(String[] args) {
//		new Thread(new Runnable() {
//
//			@Override
//			public void run() {
//
//				testSelfConcorrectFuture();
//
//			}
//		}).start();
//		new Thread(new Runnable() {
//
//			@Override
//			public void run() {
//
//				testConcorrectFuture();
//
//			}
//		}).start();
		// testFuture();
		testSelfConcorrectFuture();
//		testInvokeAll();
	}
	
	/**
	 * 
	 * @Title: testInvokeAll 
	 * @Description: executor.invokeAll执行任务集合 invokeAll按照任务集合中迭代器的顺序将所有的Future添加到返回的集合中
	 * @param     设定文件 
	 * @return void    返回类型 
	 * @author yuanhualiang
	 * @throws
	 */
	public static void testInvokeAll() {
		long start = new Date().getTime();
		System.out.println("testInvokeAll start..." + start);
		List<String> srcList = new ArrayList<String>();
		List<Callable<String>> tasks = new ArrayList<Callable<String>>();
		for (int i = 0; i < 1000; i++) {
			final String src = "s" + i;
			srcList.add(src);
			
			tasks.add(new Callable<String>() {

				@Override
				public String call() throws Exception {
					Thread.sleep(1000);
					return "t_" + src;
				}
			});
		}
		try {
			List<Future<String>> results = executor.invokeAll(tasks);
			for (int i = 0; i < 1000; i++) {
				String target = results.get(i).get();
				System.out.println(srcList.get(i) + ":" + target);
			}
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		long end = new Date().getTime();
		System.out.println("testInvokeAll speed time:"
				+ (end - start));
	}

	/**
	 * 
	 * @Title: testSelfConcorrectFuture 
	 * @Description: 通过多线程并发执行Future任务
	 * @param     设定文件 
	 * @return void    返回类型 
	 * @author yuanhualiang
	 * @throws
	 */
	public static void testSelfConcorrectFuture() {
		long start = new Date().getTime();
		System.out.println("testSelfConcorrectFuture start..." + start);
		List<String> srcList = new ArrayList<String>();
		for (int i = 0; i < 1000; i++) {
			srcList.add("s" + i);
		}

		List<Future<String>> targetList = new ArrayList<Future<String>>();
		for (final String src : srcList) {

			Future<String> target = executor.submit(new Callable<String>() {
				@Override
				public String call() throws Exception {
					Thread.sleep(1000);
					return "t_" + src;
				}
			});
			targetList.add(target);
		}
		int count = 0;
		for (int i = 0; i < srcList.size(); i++) {
			Future<String> f = null;
			try {
				f = targetList.get(i);
				String target = f.get();
				// System.out.println(srcList.get(i) + ":" + target);
			} catch (InterruptedException | ExecutionException e) {

				System.out.println("timeout:" + count++);
				f.cancel(true);

			}
		}
		long end = new Date().getTime();
		System.out.println("testSelfConcorrectFuture speed time:"
				+ (end - start));
	}

	/**
	 * 
	 * @Title: testSelfConcorrectFuture 
	 * @Description: 通过ExecutorCompletionService并发执行Future任务，ExecutorCompletionService重写done方法把结果放到队列返回 take方法获取队列结果
	 * @param     设定文件 
	 * @return void    返回类型 
	 * @author yuanhualiang
	 * @throws
	 */
	public static void testConcorrectFuture() {
		long start = new Date().getTime();
		System.out.println("testConcorrectFuture start..." + start);
		List<String> srcList = new ArrayList<String>();
		for (int i = 0; i < 1000; i++) {
			srcList.add("s" + i);
		}
		CompletionService<String> completionService = new ExecutorCompletionService<>(
				executor);
		for (final String src : srcList) {
			completionService.submit(new Callable<String>() {

				@Override
				public String call() throws Exception {
					Thread.sleep(1000);
					return "t_" + src;
				}
			});
		}
		for (String src : srcList) {
			try {
				Future<String> f = completionService.take();
				String target = f.get();
				// System.out.println(src + ":" + target);
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();

			}
		}
		long end = new Date().getTime();
		System.out.println("testConcorrectFuture speed time:" + (end - start));
	}

	/**
	 * 
	 * @Title: testFuture 
	 * @Description: 单线程执行Future任务集合，当任务执行时间慢时，性能提高不大
	 * @param     设定文件 
	 * @return void    返回类型 
	 * @author yuanhualiang
	 * @throws
	 */
	public static void testFuture() {
		long start = new Date().getTime();
		System.out.println("starting task.......");

		Callable<List<String>> task = new Callable<List<String>>() {

			@Override
			public List<String> call() throws Exception {
				List<String> data = new ArrayList<String>();
				for (int i = 0; i < 10; i++) {
					Thread.sleep(1000);
					data.add("r" + i);
				}
				return data;
			}
		};
		Future<List<String>> future = executor.submit(task);
		try {
			System.out.println("starting get result.......");
			List<String> data = future.get();
			System.out.println("output result.....");
			for (String s : data) {
				System.out.println("result:" + s);
			}
		} catch (InterruptedException | ExecutionException e) {
		}
		long end = new Date().getTime();
		System.out.println("testFuture speed time:" + (end - start));
	}

	public static void testExecutor() {
		int count = 0;
		long start = new Date().getTime();
		while (true) {
			count++;
			Runnable r = new Runnable() {

				@Override
				public void run() {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();

					}
					System.out.println("name:"
							+ Thread.currentThread().getName());
				}
			};
			executor.execute(r);
			if (count > 10000) {
				break;
			}

			long end = new Date().getTime();
			System.out.println("speed time:" + (end - start));
			if (end - start > 10) {
				System.out.println("interrupt executor");
				executor.shutdownNow();
				break;
			}
		}
	}
}
