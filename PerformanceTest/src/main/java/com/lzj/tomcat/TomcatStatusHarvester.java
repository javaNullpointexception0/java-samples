package com.lzj.tomcat;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.lzj.config.AppConfiguration;
import com.lzj.entity.ConfigParam;
import com.lzj.entity.MemoryInfo;
import com.lzj.entity.RequestInfo;
import com.lzj.entity.SystemInfo;
import com.lzj.entity.ThreadPoolInfo;
import com.lzj.entity.TomcatStatus;
import com.lzj.remote.LinuxRemoteCommand;
import com.lzj.remote.RemoteCommand;

public class TomcatStatusHarvester {
	
	private String serverIp;
	private Integer sshPort;
	private Integer servicePort;
	private String userName;
	private String password;
	
	private Integer jmxPort;
	private String jmxUsername;
	private String jmxPassword;
	
	private AppConfiguration appConfiguration;
	
	private JMXConnector jmxConnector;
	protected MBeanServerConnection mbsc;
	private ConfigParam configParam;
			
	private ThreadPoolHarverster threadPoolHarverster;
	private RequestInfoHarverster requestInfoHarverster;
	private MemoryInfoHarverster memoryInfoHarverster;
	private SystemInfoHarverster systemInfoHarverster;
	
	private RemoteCommand remoteCommand;
	
	public TomcatStatusHarvester(String serverIp, Integer sshPort, String userName, String password, Integer servicePort, 
			Integer jmxPort, String jmxUsername, String jmxPassword, AppConfiguration appConfiguration) {
		this.serverIp = serverIp;
		this.sshPort = sshPort;
		this.servicePort = servicePort;
		this.userName = userName;
		this.password = password;
		this.jmxPort = jmxPort;
		this.jmxUsername = jmxUsername;
		this.jmxPassword = jmxPassword;
		
		this.appConfiguration = appConfiguration;
		mbsc = getMBeanServerConnection();
		remoteCommand = new LinuxRemoteCommand(this.serverIp, this.sshPort, this.servicePort, this.userName, this.password);
		threadPoolHarverster = new ThreadPoolHarverster();
		requestInfoHarverster = new RequestInfoHarverster();
		memoryInfoHarverster = new MemoryInfoHarverster();
		systemInfoHarverster = new SystemInfoHarverster();
	}
	
	public TomcatStatus getTomcatStatus(boolean returnDefaultConfig) {
		TomcatStatus tomcatStatus = new TomcatStatus();
		if (appConfiguration.getMonitorThreadPoolEnable() == 1) {
			tomcatStatus.setThreadPoolInfos(getThreadPoolInfo());
		}
		if (appConfiguration.getMonitorMemoryEnable() == 1) {
			tomcatStatus.setMemoryInfo(getMemoryInfo());
		}
		if (appConfiguration.getMonitorRequestEnable() == 1) {
			tomcatStatus.setRequestInfos(getRequestInfo());
		}
		if (appConfiguration.getMonitorSystemEnable() == 1) {
			tomcatStatus.setSystemInfo(getSystemInfo());
		}
		if (returnDefaultConfig) {
			tomcatStatus.setConfigParam(getConfigParam());
		}
		return tomcatStatus;
	}
	
	public List<ThreadPoolInfo> getThreadPoolInfo() {
		List<ThreadPoolInfo> threadPoolInfo = threadPoolHarverster.getThreadPoolDynamicInfo(mbsc, servicePort.toString());
		int syncQueueSize = remoteCommand.getSyncQueueSize();
		int acceptQueueSize = remoteCommand.getAcceptQueueSize();
		threadPoolInfo.get(0).setSyncQueueSize(syncQueueSize);
		threadPoolInfo.get(0).setAcceptQueueSize(acceptQueueSize);
		return threadPoolInfo;
	}
	
	
	public List<RequestInfo> getRequestInfo() {
		return requestInfoHarverster.getRequestInfo(mbsc, servicePort.toString());
	}
	
	
	public MemoryInfo getMemoryInfo() {
		return memoryInfoHarverster.getMemoryInfo(mbsc);
	}
	
	public SystemInfo getSystemInfo() {
		return systemInfoHarverster.getSystemInfo(mbsc);
	}
	
	public ConfigParam getConfigParam() {
		if (configParam != null) {
			return configParam;
		}
		configParam = new ConfigParam();
		configParam.setServerIp(serverIp);
		List<ConfigParam> threadPoolStaticInfo = threadPoolHarverster.getThreadPoolStaticInfo(mbsc);
		configParam.setBacklog(threadPoolStaticInfo.get(0).getBacklog());
		configParam.setAcceptCount(threadPoolStaticInfo.get(0).getAcceptCount());
		configParam.setMaxConnections(threadPoolStaticInfo.get(0).getMaxConnections());
		configParam.setMaxThreads(threadPoolStaticInfo.get(0).getMaxThreads());
		configParam.setMinSpareThreads(threadPoolStaticInfo.get(0).getMinSpareThreads());
		return configParam;
	}
	
	public void releaseReource() {
		if (jmxConnector != null) {
			try {
				jmxConnector.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (remoteCommand != null) {
			remoteCommand.close();
		}
	}
	
	public String getServerIp() {
		return serverIp;
	}


	/**
	 * 	获取MBeanServerConnection
	 * @return
	 */
	private MBeanServerConnection getMBeanServerConnection() {
		if (mbsc == null && jmxConnector == null) {
			try {
				//tomcat jmx url
				String jmxURL = "service:jmx:rmi:///jndi/rmi://" + serverIp + ":" + jmxPort + "/jmxrmi";
				JMXServiceURL serviceURL = new JMXServiceURL(jmxURL);
				Map<String, String[]> map = new HashMap<String, String[]>();
				String[] credentials = new String[] { "monitorRole" , "QED" };
				map.put("jmx.remote.credentials", credentials);
				jmxConnector = JMXConnectorFactory.connect(serviceURL, map);
				mbsc = jmxConnector.getMBeanServerConnection();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return mbsc;
	}
}
