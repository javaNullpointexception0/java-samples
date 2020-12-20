package com.lzj.entity;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ThreadPoolInfo {
	//线程名
	private String name;
	
	private Integer connectionCount;
	private Integer keepAliveCount;
	
	//当前线程数
	private Integer currentThreadCount;
	//繁忙线程数
	private Integer currentThreadsBusy;
	
	//半连接队列当前队列连接大小
	private Integer syncQueueSize;
	//全连接队列当前队列连接大小
	private Integer acceptQueueSize;
}
