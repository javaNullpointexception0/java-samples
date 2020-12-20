package com.lzj.task.starting;

import com.lzj.config.AppConfiguration;

public interface IAppStartingTask {

	/**
	 * 程序启动时需要执行的任务
	 * @param appConfiguration
	 * @return
	 */
	public Object start(AppConfiguration appConfiguration);
}
