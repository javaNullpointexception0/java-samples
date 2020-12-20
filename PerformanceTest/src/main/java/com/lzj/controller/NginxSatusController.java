package com.lzj.controller;

import com.lzj.config.AppConfiguration;
import com.lzj.entity.TomcatStatus;
import com.lzj.service.NginxStatusService;
import com.lzj.tomcat.TomcatStatusHarvester;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/nginxSatus")
public class NginxSatusController {
	
	@Autowired
	private NginxStatusService nginxStatusService;

	/**
	 * 从数据库查询Nginx服务监控数据（监控数据已通过监控程序保存到数据库中）
	 * @return
	 */
	@RequestMapping("/getNginxSatus")
	public Map<String, Object> getTomcatRunStatus(String serverIp,
												  String startDate,
												  String endDate,
												  Integer groupType) {
		return nginxStatusService.getNginxInfo(serverIp, startDate, endDate, groupType);
	}
}
