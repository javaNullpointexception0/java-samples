package com.lzj.task;


import com.lzj.config.AppConfiguration;
import com.lzj.entity.NginxStatus;
import com.lzj.nginx.NginxStatusHarvester;
import com.lzj.service.NginxStatusService;
import com.lzj.task.starting.IAppStartingTask;
import com.lzj.task.stoping.IAppStopingTask;
import com.lzj.util.ApplicationContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.*;

@Slf4j
@Component
public class MonitorNginxStatusTask implements IAppStartingTask, IAppStopingTask {

    private List<NginxStatusHarvester> nginxStatusHarvesters = new ArrayList<NginxStatusHarvester>();
    private ThreadPoolExecutor threadPoolExecutor;
    private ConcurrentLinkedQueue<NginxStatus> waitingSaveQueue = new ConcurrentLinkedQueue<NginxStatus>();

    private Timer monitorTimer = new Timer();

    private Timer saveTimer = new Timer();

    private NginxStatusService nginxStatusService;

    @Override
    public Object start(AppConfiguration appConfiguration) {
        if (appConfiguration.getMonitorNginxEnable()  <= 0 || StringUtils.isEmpty(appConfiguration.getMonitorNginxIpPorts())) {
            return null;
        }
        String[] nginxIpPorts = appConfiguration.getMonitorNginxIpPorts().split(",");
        for (String nginxIpPortStr : nginxIpPorts) {
            String[] nginxIpPort = nginxIpPortStr.split(":");
            nginxStatusHarvesters.add(new NginxStatusHarvester(nginxIpPort[0], Integer.valueOf(nginxIpPort[1])));
        }
        if (nginxStatusHarvesters.size() <= 0) {
            return null;
        }
        threadPoolExecutor = new ThreadPoolExecutor(nginxStatusHarvesters.size(), nginxStatusHarvesters.size() * 2,
                60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(10));

        nginxStatusService = ApplicationContextUtil.getBean(NginxStatusService.class);

        monitorTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long startTime = System.currentTimeMillis();
                log.info("开始获取nginx服务器运行信息，共{}个nginx服务", nginxStatusHarvesters.size());
                List<Future<?>> futures = new ArrayList<Future<?>>();
                for (NginxStatusHarvester nginxStatusHarvester : nginxStatusHarvesters) {
                    Future<?> future = threadPoolExecutor.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                NginxStatus nginxStatus = nginxStatusHarvester.getNginxStatus();
                                if (nginxStatus != null) {
                                    waitingSaveQueue.add(nginxStatus);
                                }
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
                            log.info("获取{}台nginx服务器运行信息完成，耗时：{}", nginxStatusHarvesters.size(), (endTime - startTime));
                            break;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 0, appConfiguration.getMonitorNginxIntervalMs());

        saveTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (waitingSaveQueue.size() > 0) {
                    long startTime = System.currentTimeMillis();
                    saveNginxStatus();
                    long endTime = System.currentTimeMillis();
                    log.info("保存nginx服务器运行信息完成，耗时：{}", (endTime - startTime));
                }
            }
        }, 0, appConfiguration.getMonitorNginxResultSaveDelayMs());
        return null;
    }

    private void saveNginxStatus() {
        if (waitingSaveQueue.size() <= 0) {
            return;
        }
        List<NginxStatus> saveNginxStatusList = new ArrayList<>();
        while(waitingSaveQueue.size() > 0) {
            saveNginxStatusList.add(waitingSaveQueue.poll());
        }
        //保存数据到数据库
        nginxStatusService.saveNginxStatusList(saveNginxStatusList);
    }

    @Override
    public Object stop(AppConfiguration appConfiguration) {
        if (monitorTimer != null) {
            monitorTimer.cancel();
        }
        if (threadPoolExecutor != null) {
            threadPoolExecutor.shutdown();
            log.info("线程池关闭，剩余{}个nginx监控任务未执行完成，等待监控任务执行完毕...", threadPoolExecutor.getActiveCount());
            while(threadPoolExecutor.getActiveCount() > 0) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        if (waitingSaveQueue.size() > 0) {
            saveNginxStatus();
        }
        return null;
    }
}
