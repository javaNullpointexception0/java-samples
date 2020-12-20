package com.lzj.tomcat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import com.lzj.entity.ConfigParam;
import com.lzj.entity.ThreadPoolInfo;

public class ThreadPoolHarverster {

	/**
	 * Thread Pool
	 * @param mbsc
	 */
	public List<ThreadPoolInfo> getThreadPoolDynamicInfo(MBeanServerConnection mbsc, String servicePort) {
		List<ThreadPoolInfo> infoList = new ArrayList<ThreadPoolInfo>();
		if (mbsc != null) {
			try {
				ObjectName objName = new ObjectName("Tomcat:type=ThreadPool,*");
				Set<ObjectName> objectNameSet = mbsc.queryNames(objName, null);
				for (ObjectName row : objectNameSet){
					if (!row.getKeyProperty("name").contains(servicePort)) {
						continue;
					}
					ThreadPoolInfo infoRow = new ThreadPoolInfo();
					infoRow.setName(row.getKeyProperty("name"));
					ObjectName canonicalName = new ObjectName(row.getCanonicalName());
					
					infoRow.setConnectionCount(Integer.valueOf(mbsc.getAttribute(canonicalName, "connectionCount").toString()));
					infoRow.setKeepAliveCount(Integer.valueOf(mbsc.getAttribute(canonicalName, "keepAliveCount").toString()));
					
					infoRow.setCurrentThreadCount(Integer.valueOf(mbsc.getAttribute( canonicalName, "currentThreadCount").toString()));
					infoRow.setCurrentThreadsBusy(Integer.valueOf(mbsc.getAttribute( canonicalName, "currentThreadsBusy").toString()));
					
					infoList.add(infoRow);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return infoList;
	}
	
	public List<ConfigParam> getThreadPoolStaticInfo(MBeanServerConnection mbsc) {
		List<ConfigParam> infoList = new ArrayList<ConfigParam>();
		if (mbsc != null) {
			try {
				ObjectName objName = new ObjectName("Tomcat:type=ThreadPool,*");
				Set<ObjectName> objectNameSet = mbsc.queryNames(objName, null);
				for (ObjectName row : objectNameSet){
					ConfigParam infoRow = new ConfigParam();
					
					ObjectName canonicalName = new ObjectName(row.getCanonicalName());
					infoRow.setAcceptCount(Integer.valueOf(mbsc.getAttribute( canonicalName, "acceptCount").toString()));
					infoRow.setBacklog(Integer.valueOf(mbsc.getAttribute( canonicalName, "backlog").toString()));
					
					infoRow.setMaxConnections(Integer.valueOf(mbsc.getAttribute( canonicalName, "maxConnections").toString()));
					
					infoRow.setMaxThreads(Integer.valueOf(mbsc.getAttribute( canonicalName, "maxThreads").toString()));
					infoRow.setMinSpareThreads(Integer.valueOf(mbsc.getAttribute( canonicalName, "minSpareThreads").toString()));
					
					infoList.add(infoRow);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return infoList;
	}
}
