package com.lzj.dao;

import com.lzj.entity.NginxStatus;
import com.lzj.entity.TomcatStatus;

import java.util.List;
import java.util.Map;

public interface NginxStatusDao {
	
	public Object saveNginxStatusList(List<NginxStatus> nginxStatusList);

	public Map<String, Object> getNginxInfo(String serverIp, String startDate, String endDate, Integer groupType);
}
