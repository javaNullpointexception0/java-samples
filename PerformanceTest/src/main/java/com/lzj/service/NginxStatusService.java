package com.lzj.service;

import com.lzj.entity.NginxStatus;

import java.util.List;
import java.util.Map;

public interface NginxStatusService {

	public Object saveNginxStatusList(List<NginxStatus> nginxStatusList);

	public Map<String, Object> getNginxInfo(String serverIp, String startDate, String endDate, Integer groupType);
}
