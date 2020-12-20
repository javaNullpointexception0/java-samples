package com.lzj.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lzj.config.AppConfiguration;
import com.lzj.entity.ConfigParam;
import com.lzj.service.PageService;
import com.lzj.task.MonitorTomcatStatusTask;
import com.lzj.tomcat.TomcatStatusHarvester;
import com.lzj.util.ApplicationContextUtil;

@RestController
@RequestMapping("/page")
public class PageController {
	
	@Autowired
	private AppConfiguration appConfiguration;
	@Autowired
	private PageService pageService;

	@RequestMapping("/getServerList")
	public List<String> getServerList() {
		List<String> serverList = new ArrayList<String>();
		String monitorServerInfos = appConfiguration.getMonitorServerInfos();
		if (!StringUtils.isEmpty(monitorServerInfos)) {
			String[] serverInfos = monitorServerInfos.split(",");
			for (String serverInfo : serverInfos) {
				serverList.add(serverInfo.substring(0, serverInfo.indexOf("/")).trim());
			}
		}
		return serverList;
	}
	
	@RequestMapping("/getConfig")
	public List<ConfigParam> getConfig() {
		List<ConfigParam> configList = new ArrayList<ConfigParam>();
		MonitorTomcatStatusTask monitorTomcatStatusTask = (MonitorTomcatStatusTask)ApplicationContextUtil.getBean(MonitorTomcatStatusTask.class);
		List<TomcatStatusHarvester> tomcatStatusHarvesters = monitorTomcatStatusTask.getTomcatStatusHarvesters();
		for (TomcatStatusHarvester tomcatStatusHarvester : tomcatStatusHarvesters) {
			configList.add(tomcatStatusHarvester.getConfigParam());
		}
		return configList;
	}
	
	@RequestMapping("/getThreadInfo")
	public Map<String, Object> getThreadInfo(String serverIp, String startDate, String endDate) {
		return pageService.getThreadInfo(serverIp, startDate, endDate);
	}
	
	@RequestMapping("/getRequestInfo")
	public Map<String, Object> getRequestInfo(String serverIp, String startDate, String endDate) {
		return pageService.getRequestInfo(serverIp, startDate, endDate);
	}
	
}
