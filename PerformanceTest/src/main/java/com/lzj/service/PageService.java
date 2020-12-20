package com.lzj.service;

import java.util.Map;

public interface PageService {

	public Map<String, Object> getThreadInfo(String serverIp, String startDate, String endDate);
	
	
	public Map<String, Object> getRequestInfo(String serverIp, String startDate, String endDate);
}
