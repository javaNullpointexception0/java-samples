package com.lzj.entity;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SystemInfo {
	//虚拟机名
	private String name;
	//厂商
	private String vendor;
	//版本
	private String version;
	//启动时间
	private Date startTime;
	//在线时长
	private Long uptime;
}
