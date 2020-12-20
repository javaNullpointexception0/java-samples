package com.lzj.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.lzj.service.TomcatStatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.lzj.config.AppConfiguration;
import com.lzj.entity.TomcatStatus;
import com.lzj.task.starting.IAppStartingTask;
import com.lzj.task.stoping.IAppStopingTask;
import com.lzj.tomcat.TomcatStatusHarvester;
import com.lzj.util.ApplicationContextUtil;
import org.springframework.util.StringUtils;

@Component
public class MonitorTomcatStatusTask implements IAppStartingTask, IAppStopingTask {

	private static final Logger log = LoggerFactory.getLogger(MonitorTomcatStatusTask.class);
	
	private List<TomcatStatusHarvester> tomcatStatusHarvesters = new ArrayList<TomcatStatusHarvester>();
	private ThreadPoolExecutor threadPoolExecutor;
	private ConcurrentLinkedQueue<TomcatStatus> waitingSaveQueue = new ConcurrentLinkedQueue<TomcatStatus>();
	private AtomicInteger counter = new AtomicInteger(0);
	private TomcatStatusService tomcatStatusService;

	Timer monitorTimer = new Timer();

	Timer saveTimer = new Timer();
	
	@Override
	public Object start(AppConfiguration appConfiguration) {
		if (appConfiguration.getMonitorServerEnable() <= 0 || StringUtils.isEmpty(appConfiguration.getMonitorServerInfos())) {
			return null;
		}
		String[] monitorServerInfos = appConfiguration.getMonitorServerInfos().split(",");
		int len = monitorServerInfos.length;
		for (int i = 0; i < len; i++) {
			String monitorServerInfo = monitorServerInfos[i];
			List<String> serverInfo = new ArrayList<String>(8);
			int startIndex = 0;
			int endIndex = -1;
			while ((endIndex = monitorServerInfo.indexOf("/", startIndex)) != -1) {
				if (endIndex != monitorServerInfo.length()) {
					String value = monitorServerInfo.substring(startIndex, endIndex);
					serverInfo.add(value.length() == 0 ? null : value);
					startIndex = endIndex + 1;
				}
				if (startIndex >= monitorServerInfo.length()){
					serverInfo.add(null);
				}
			}
			TomcatStatusHarvester tomcatStatusHarvester = new TomcatStatusHarvester(serverInfo.get(0), 
					Integer.valueOf(serverInfo.get(1)), serverInfo.get(2), serverInfo.get(3),
					Integer.valueOf(serverInfo.get(4)), Integer.valueOf(serverInfo.get(5)), 
					serverInfo.get(6), serverInfo.get(7), appConfiguration);
			tomcatStatusHarvesters.add(tomcatStatusHarvester);
		}
		if (tomcatStatusHarvesters.size() <= 0) {
			return null;
		}

		tomcatStatusService = (TomcatStatusService)ApplicationContextUtil.getBean(TomcatStatusService.class);
		
		threadPoolExecutor = new ThreadPoolExecutor(tomcatStatusHarvesters.size(), tomcatStatusHarvesters.size() * 2,
                60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
		
		monitorTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
            	long startTime = System.currentTimeMillis();
            	int times = counter.addAndGet(1);
            	log.info("开始第{}次获取{}台服务器运行信息......", times, tomcatStatusHarvesters.size());
            	List<Future<?>> futures = new ArrayList<Future<?>>();
            	for (TomcatStatusHarvester tomcatStatusHarvester : tomcatStatusHarvesters) {
            		Future<?> future = threadPoolExecutor.submit(new Runnable() {
    					@Override
    					public void run() {
    						try {
    							TomcatStatus tomcatStatus = tomcatStatusHarvester.getTomcatStatus(false);
    							tomcatStatus.setMonitorTime(new Date());
								tomcatStatus.setServerIp(tomcatStatusHarvester.getServerIp());
								waitingSaveQueue.add(tomcatStatus);
							} catch (Exception e) {
								e.printStackTrace();
							}
    					}
    				});
            		futures.add(future);
        		}
            	boolean finish = true;
            	while(true) {
            		try {
            			for (Future<?> future : futures) {
                			if (!future.isDone()) {
                				finish = false;
                				Thread.sleep(10);
                				break;
                			} else {
                				finish = true;
                			}
                		}
            			if (finish) {
            				long endTime = System.currentTimeMillis();
            				log.info("第{}次获取{}台服务器运行信息完成，耗时：{}", times, tomcatStatusHarvesters.size(), (endTime - startTime));
            				break;
            			}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
            	}
            }
        }, 0, appConfiguration.getMonitorServerIntervalMs());

		saveTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				if (waitingSaveQueue.size() > 0) {
					long startTime = System.currentTimeMillis();
					saveTomcatStatus();
					long endTime = System.currentTimeMillis();
					log.info("保存nginx服务器运行信息完成，耗时：{}", (endTime - startTime));
				}
			}
		}, 0, appConfiguration.getMonitorServerResultSaveDelayMs());
		return null;
	}

	@Override
	public Object stop(AppConfiguration appConfiguration) {
		if (monitorTimer != null) {
			monitorTimer.cancel();
		}
		if (threadPoolExecutor != null) {
			threadPoolExecutor.shutdown();
			log.info("线程池关闭，剩余{}个监控任务未执行完成，等待监控任务执行完毕...", threadPoolExecutor.getActiveCount());
			while(threadPoolExecutor.getActiveCount() > 0) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		if (waitingSaveQueue.size() > 0) {
			saveTomcatStatus();
		}
		log.info("线程池关闭完成，所有监控任务执行完毕");
		
		log.info("关闭{}个服务远程连接", tomcatStatusHarvesters.size());
		for (TomcatStatusHarvester tomcatStatusHarvester : tomcatStatusHarvesters) {
			tomcatStatusHarvester.releaseReource();
		}
		log.info("关闭{}个服务远程连接完成，程序正常退出", tomcatStatusHarvesters.size());
		return null;
	}
	
	public List<TomcatStatusHarvester> getTomcatStatusHarvesters() {
		return tomcatStatusHarvesters;
	}

	private void saveTomcatStatus() {
		List<TomcatStatus> saveTomcatStatusList = new ArrayList<TomcatStatus>();
		while(waitingSaveQueue.size() > 0) {
			saveTomcatStatusList.add(waitingSaveQueue.poll());
		}
		tomcatStatusService.saveTomcatStatusList(saveTomcatStatusList);
	}
}
