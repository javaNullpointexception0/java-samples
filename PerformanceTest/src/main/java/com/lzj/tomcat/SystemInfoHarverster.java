package com.lzj.tomcat;

import java.util.Date;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import com.lzj.entity.SystemInfo;

public class SystemInfoHarverster {

	public SystemInfo getSystemInfo(MBeanServerConnection mbsc) {
		SystemInfo infoRow = new SystemInfo();
		if (mbsc != null) {
			try {
				ObjectName objName = new ObjectName("java.lang:type=Runtime");
				infoRow.setName(String.valueOf(mbsc.getAttribute(objName, "VmName")));
				infoRow.setVendor(String.valueOf(mbsc.getAttribute(objName, "VmVendor")));
				infoRow.setVersion(String.valueOf(mbsc.getAttribute(objName, "VmVersion")));

				Date startTime=new Date((Long)mbsc.getAttribute(objName, "StartTime"));
				infoRow.setStartTime(startTime);

				Long uptime=(Long)mbsc.getAttribute(objName, "Uptime");
				infoRow.setUptime(uptime);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return infoRow;
	}
}
