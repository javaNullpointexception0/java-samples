package com.lzj.dao.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.lzj.dao.TomcatStatusDao;
import com.lzj.entity.MemoryInfo;
import com.lzj.entity.RequestInfo;
import com.lzj.entity.ThreadPoolInfo;
import com.lzj.entity.TomcatStatus;

@Repository
public class TomcatStatusDaoImpl implements TomcatStatusDao {

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Override
	public Object saveTomcatStatus(TomcatStatus tomcatStatus) {
		this.saveTomcatStatusList(Arrays.asList(tomcatStatus));
		return null;
	}

	@Override
	public Object saveTomcatStatusList(List<TomcatStatus> tomcatStatusList) {
		if (tomcatStatusList == null || tomcatStatusList.isEmpty()) {
			return null;
		}
		List<Object[]> batchArgs = new ArrayList<Object[]>();
		for (TomcatStatus tomcatStatus : tomcatStatusList) {
			List<Object> arg = new ArrayList<Object>();
			arg.add(tomcatStatus.getServerIp());
			setThreadColumnValue(arg, tomcatStatus);
			setRequestColumnValue(arg, tomcatStatus);
			setMemoryColumnValue(arg, tomcatStatus);
			arg.add(tomcatStatus.getMonitorTime());
			batchArgs.add(arg.toArray());
		}
		String sql = "insert into tb_tomcat_status(ServerIp,SyncQueueSize,AcceptQueueSize,ConnectionCount,KeepAliveCount,CurrentThreadCount,CurrentThreadsBusy,RequestCount,RequestErrorCount,MaxRequestProcessingTime,NewGenerationCapacity,NewGenerationUsed,TenuredGenerationCapacity,TenuredGenerationUsed,MonitorTime) "
				+ " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		try {
			int[] batchUpdate = jdbcTemplate.batchUpdate(sql, batchArgs);
			return batchUpdate;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void setThreadColumnValue(List<Object> arg, TomcatStatus tomcatStatus) {
		if (tomcatStatus.getThreadPoolInfos() == null || tomcatStatus.getThreadPoolInfos().isEmpty()) {
			arg.add(0);
			arg.add(0);
			arg.add(0);
			arg.add(0);
			arg.add(0);
			arg.add(0);
		} else {
			ThreadPoolInfo threadPoolInfo = tomcatStatus.getThreadPoolInfos().get(0);
			arg.add(threadPoolInfo.getSyncQueueSize() == null ? 0 : threadPoolInfo.getSyncQueueSize());
			arg.add(threadPoolInfo.getAcceptQueueSize() == null ? 0 : threadPoolInfo.getAcceptQueueSize());
			arg.add(threadPoolInfo.getConnectionCount() == null ? 0 : threadPoolInfo.getConnectionCount());
			arg.add(threadPoolInfo.getKeepAliveCount() == null ? 0 : threadPoolInfo.getKeepAliveCount());
			arg.add(threadPoolInfo.getCurrentThreadCount() == null ? 0 : threadPoolInfo.getCurrentThreadCount());
			arg.add(threadPoolInfo.getCurrentThreadsBusy() == null ? 0 : threadPoolInfo.getCurrentThreadsBusy());
		}
	}
	
	private void setRequestColumnValue(List<Object> arg, TomcatStatus tomcatStatus) {
		if (tomcatStatus.getRequestInfos() == null || tomcatStatus.getRequestInfos().isEmpty()) {
			arg.add(0);
			arg.add(0);
			arg.add(0);
		} else {
			RequestInfo requestInfo = tomcatStatus.getRequestInfos().get(0);
			arg.add(requestInfo.getRequestCount() == null ? 0 : requestInfo.getRequestCount());
			arg.add(requestInfo.getErrorCount() == null ? 0 : requestInfo.getErrorCount());
			arg.add(requestInfo.getMaxTime() == null ? 0 : requestInfo.getMaxTime());
		}
	}
	
	private void setMemoryColumnValue(List<Object> arg, TomcatStatus tomcatStatus) {
		if (tomcatStatus.getMemoryInfo() == null) {
			arg.add(0);
			arg.add(0);
			arg.add(0);
			arg.add(0);
		} else {
			MemoryInfo memoryInfo = tomcatStatus.getMemoryInfo();
			arg.add(memoryInfo.getNewGenerationCapacity() == null ? 0 : memoryInfo.getNewGenerationCapacity());
			arg.add(memoryInfo.getNewGenerationUsed() == null ? 0 : memoryInfo.getNewGenerationUsed());
			arg.add(memoryInfo.getTenuredGenerationCapacity() == null ? 0 : memoryInfo.getTenuredGenerationCapacity());
			arg.add(memoryInfo.getTenuredGenerationUsed() == null ? 0 : memoryInfo.getTenuredGenerationUsed());
		}
	}

}
