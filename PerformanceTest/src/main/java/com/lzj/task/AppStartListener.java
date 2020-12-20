package com.lzj.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.lzj.config.AppConfiguration;
import com.lzj.task.starting.IAppStartingTask;
import com.lzj.task.stoping.IAppStopingTask;
import com.lzj.util.ApplicationContextUtil;

@Component
public class AppStartListener implements ApplicationRunner {

	@Autowired
	private AppConfiguration appConfiguration;
	
	@Override
	public void run(ApplicationArguments args) throws Exception {
		String[] beanNames = ApplicationContextUtil.getBeanNamesForType(IAppStartingTask.class);
		for (String beanName : beanNames) {
			IAppStartingTask appStartingTask = (IAppStartingTask)ApplicationContextUtil.getBean(beanName);
			appStartingTask.start(appConfiguration);
		}
		
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				String[] beanNames = ApplicationContextUtil.getBeanNamesForType(IAppStopingTask.class);
				for (String beanName : beanNames) {
					IAppStopingTask appStartingTask = (IAppStopingTask)ApplicationContextUtil.getBean(beanName);
					appStartingTask.stop(appConfiguration);
				}
			}
		}));
	}

}
