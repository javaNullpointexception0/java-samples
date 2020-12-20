package com.lzj.dao;

import java.util.Map;

public interface PageDao {

	public Map<String, Object> getThreadInfo(String serverIp, String startDate, String endDate);
	
	
	public Map<String, Object> getRequestInfo(String serverIp, String startDate, String endDate);
}
