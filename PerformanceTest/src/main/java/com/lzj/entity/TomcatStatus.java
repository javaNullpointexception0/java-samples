package com.lzj.entity;

import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TomcatStatus {
	private String serverIp;
	private ConfigParam configParam;
	private MemoryInfo memoryInfo;
	private SystemInfo systemInfo;
	private List<ThreadPoolInfo> threadPoolInfos;
	private List<RequestInfo> requestInfos;
	private Date monitorTime;
}
