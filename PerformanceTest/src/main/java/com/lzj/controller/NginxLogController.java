package com.lzj.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lzj.service.NginxLogService;

@RestController
@RequestMapping("/nginxLogController")
public class NginxLogController {

	@Autowired
	private NginxLogService nginxLogService;

	/**
	 * 通过nginx access.log分析日志信息（客户端ip/访问时间/访问资源/上游服务器响应时间/HTTP响应时间等）入库
	 * @return
	 */
	@RequestMapping("/explainNginxLog")
	public String explainNginxLog() {
		nginxLogService.explainNginxLog();
		return "正在分析nginx日志";
	}
	
	@RequestMapping("/findTextFromFile")
	public String findTextFromFile(String text) {
		String resultFilePath = nginxLogService.findTextFromFile(text);
		return "分析结果：" + resultFilePath;
	}
}
