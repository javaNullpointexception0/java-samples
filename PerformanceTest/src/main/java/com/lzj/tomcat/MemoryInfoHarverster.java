package com.lzj.tomcat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import com.lzj.entity.MemoryInfo;

public class MemoryInfoHarverster {
	
	Pattern newGenerationPattern = Pattern.compile("def new generation.*K");
	Pattern tenuredGenerationPattern = Pattern.compile("tenured generation.*K");
	Pattern numberPattern = Pattern.compile("\\d+");

	public MemoryInfo getMemoryInfo(MBeanServerConnection mbsc) {
		MemoryInfo infoRow = new MemoryInfo();
		if (mbsc != null) {
			try {
				ObjectName objName = new ObjectName("com.sun.management:type=DiagnosticCommand");
				String heapInfo = (String)mbsc.invoke(objName, "gcHeapInfo", null, null);
				Integer[] newGenerationInfo = getHeapInfo(newGenerationPattern, heapInfo);
				infoRow.setNewGenerationCapacity(newGenerationInfo[0]);
				infoRow.setNewGenerationUsed(newGenerationInfo[1]);
				Integer[] tenuredGenerationInfo = getHeapInfo(tenuredGenerationPattern, heapInfo);
				infoRow.setTenuredGenerationCapacity(tenuredGenerationInfo[0]);
				infoRow.setTenuredGenerationUsed(tenuredGenerationInfo[1]);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return infoRow;
	}
	
	private Integer[] getHeapInfo(Pattern pattern, String heapInfo) {
		Integer[] result = new Integer[2];
		Matcher matcher = pattern.matcher(heapInfo);
		if (matcher.find()) {
			String group = matcher.group();
			Matcher numberMatch = numberPattern.matcher(group);
			int i = 0;
			while (numberMatch.find()) {
				result[i++] = Integer.valueOf(numberMatch.group());
			}
		}
		return result;
	}
}
