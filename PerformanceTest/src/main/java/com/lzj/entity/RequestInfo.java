package com.lzj.entity;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RequestInfo {

	private Long requestCount;
	private Long errorCount;
	private Long maxTime;
	private Long ProcessionTime;
}
