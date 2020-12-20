package com.lzj.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@Getter
@Setter
public class AppConfiguration {

	@Value("${server.port}")
	private Integer port;
	@Value("${server.tomcat.max-threads:100}")
	private Integer maxThread;
	@Value("${server.tomcat.max-connections:1000}")
	private Integer maxConnections;
	@Value("${server.tomcat.accept-count:100}")
	private Integer acceptCount;
	
	@Value("${spring.datasource.driver-class-name}")
	private String dbDriverName;
	@Value("${spring.datasource.url}")
	private String dbUrl;
	@Value("${spring.datasource.username}")
	private String dbUserName;
	@Value("${spring.datasource.password}")
	private String dbPassword;


	@Value("${monitor.server.enable}")
	private int monitorServerEnable;
	@Value("${monitor.server.infos}")
	private String monitorServerInfos;
	@Value("${monitor.server.interval.ms}")
	private long monitorServerIntervalMs;
	@Value("${monitor.threadpool.enable:1}")
	private int monitorThreadPoolEnable;
	@Value("${monitor.memory.enable:1}")
	private int monitorMemoryEnable;
	@Value("${monitor.request.enable:1}")
	private int monitorRequestEnable;
	@Value("${monitor.system.enable:1}")
	private int monitorSystemEnable;
	@Value("${monitor.server.result.save.delay.ms:1000}")
	private int monitorServerResultSaveDelayMs;

	@Value("${monitor.nginx.enable}")
	private int monitorNginxEnable;
	@Value("${monitor.nginx.ip_ports}")
	private String monitorNginxIpPorts;

	@Value("${monitor.nginx.interval.ms}")
	private long monitorNginxIntervalMs;
	@Value("${monitor.nginx.result.save.delay.ms:1000}")
	private int monitorNginxResultSaveDelayMs;

	@Value("${nginx.log.dir}")
	private String nginxLogDir;
	@Value("${nginx.log.explain.properties}")
	private String nginxLogProperties;
	@Value("${nginx.log.pattern}")
	private String nginxLogPattern;
}
