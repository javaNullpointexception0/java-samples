package com.lzj.entity;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ConfigParam {

	private String serverIp;
	private Integer backlog;
	private Integer acceptCount;
	
	private Integer maxConnections;
	
	//最大线程数
	private Integer maxThreads;
	private Integer minSpareThreads;
}
