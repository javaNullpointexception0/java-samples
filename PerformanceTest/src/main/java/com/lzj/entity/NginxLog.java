package com.lzj.entity;

import java.util.Date;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NginxLog {

	private String serverIp;
	private String remoteAddr;
	private Date timeLocal;
	private String httpMethod = "POST";
	private String requestUri;
	private int status;
	private float requestTime;
	private float upstreamResponseTime;
	
	//一秒钟内访问次数
	private LongAdder secondRequestTimes = new LongAdder();
	//一秒钟内访问总耗时
	private DoubleAdder secondRequestTotalTime = new DoubleAdder();
	//一秒钟内，服务响应总耗时
	private DoubleAdder secondUpstreamResponseTime = new DoubleAdder();
}
