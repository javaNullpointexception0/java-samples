package com.lzj.task.stoping;

import com.lzj.config.AppConfiguration;

public interface IAppStopingTask {

	/**
	 * 程序启动时需要执行的任务
	 * @param appConfiguration
	 * @return
	 */
	public Object stop(AppConfiguration appConfiguration);
}
