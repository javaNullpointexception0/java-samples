package com.lzj.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lzj.config.AppConfiguration;
import com.lzj.entity.TomcatStatus;
import com.lzj.tomcat.TomcatStatusHarvester;

@RestController
@RequestMapping("/tomcatRunSatus")
public class TomcatRunSatusController {
	
	@Autowired
	private AppConfiguration appConfiguration;

	/**
	 * 从数据库查询Java 服务监控数据（监控数据已通过监控程序保存到数据库中）
	 * @return
	 */
	@RequestMapping("/getTomcatRunStatus")
	public List<TomcatStatus> getTomcatRunStatus() {
		String[] monitorServerInfos = appConfiguration.getMonitorServerInfos().split(",");
		List<TomcatStatus> tomcatStatusList = new ArrayList<TomcatStatus>();
		int len = monitorServerInfos.length;
		for (int i = 0; i < len; i++) {
			String monitorServerInfo = monitorServerInfos[i];
			List<String> serverInfo = new ArrayList<String>(8);
			int startIndex = 0;
			int endIndex = -1;
			while ((endIndex = monitorServerInfo.indexOf("/", startIndex)) != -1) {
				if (endIndex != monitorServerInfo.length()) {
					String value = monitorServerInfo.substring(startIndex, endIndex);
					serverInfo.add(value.length() == 0 ? null : value);
					startIndex = endIndex + 1;
				}
				if (startIndex >= monitorServerInfo.length()){
					serverInfo.add(null);
				}
			}
			TomcatStatusHarvester tomcatStatusHarvester = new TomcatStatusHarvester(serverInfo.get(0), 
					Integer.valueOf(serverInfo.get(1)), serverInfo.get(2), serverInfo.get(3),
					Integer.valueOf(serverInfo.get(4)), Integer.valueOf(serverInfo.get(5)), 
					serverInfo.get(6), serverInfo.get(7), appConfiguration);
			TomcatStatus tomcatStatus = tomcatStatusHarvester.getTomcatStatus(true);
			tomcatStatusList.add(tomcatStatus);
		}
		return tomcatStatusList;
	}
}
