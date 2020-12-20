package com.lzj.tomcat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import com.lzj.entity.RequestInfo;

public class RequestInfoHarverster {

	public List<RequestInfo> getRequestInfo(MBeanServerConnection mbsc, String servicePort) {
		List<RequestInfo> infoList = new ArrayList<RequestInfo>();
		if (mbsc != null) {
			try {
				ObjectName objName = new ObjectName("Tomcat:type=GlobalRequestProcessor,*");
				mbsc.queryNames(objName, null);

				Set<ObjectName> objectNameSet = mbsc.queryNames(objName, null);
				for (ObjectName row : objectNameSet){
					if (!row.getKeyProperty("name").contains(servicePort)) {
						continue;
					}
					RequestInfo infoRow = new RequestInfo();
					ObjectName canonicalName = new ObjectName(row.getCanonicalName());
					infoRow.setRequestCount(Long.valueOf(mbsc.getAttribute( canonicalName, "requestCount").toString()));
					infoRow.setErrorCount(Long.valueOf(mbsc.getAttribute( canonicalName, "errorCount").toString()));
					infoRow.setMaxTime(Long.valueOf(mbsc.getAttribute( canonicalName, "maxTime").toString()));
					infoList.add(infoRow);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return infoList;
	}
}
