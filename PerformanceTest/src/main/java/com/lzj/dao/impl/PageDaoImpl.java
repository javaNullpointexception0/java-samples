package com.lzj.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.lzj.dao.PageDao;

@Repository
public class PageDaoImpl implements PageDao {

	private static final String dateFormat = "DATE_FORMAT(MonitorTime,'%Y-%m-%d %H:%i:%s')";
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Override
	public Map<String, Object> getThreadInfo(String serverIp, String startDate, String endDate) {
		StringBuilder sql = new StringBuilder();
		sql.append("select sum(SyncQueueSize),sum(AcceptQueueSize),sum(ConnectionCount),sum(KeepAliveCount),sum(CurrentThreadCount),sum(CurrentThreadsBusy), ");
		sql.append(dateFormat).append(" as MT");
		sql.append(" from tb_tomcat_status where 1=1");
		if (!StringUtils.isEmpty(serverIp)) {
			sql.append(" and ServerIp in (").append(serverIp).append(")");
		}
		if (!StringUtils.isEmpty(startDate)) {
			sql.append(" and MonitorTime>='").append(startDate).append("'");
		}
		if (!StringUtils.isEmpty(endDate)) {
			sql.append(" and MonitorTime<='").append(endDate).append("'");
		}
		sql.append(" group by MT order by MonitorTime asc ");
		List<Integer> syncQueue = new ArrayList<Integer>();
		List<Integer> acceptQueue = new ArrayList<Integer>();
		List<Integer> connectionCount = new ArrayList<Integer>();
		List<Integer> keepAliveCount = new ArrayList<Integer>();
		List<Integer> threadCount = new ArrayList<Integer>();
		List<Integer> threadsBusy = new ArrayList<Integer>();
		List<String> monitorTime = new ArrayList<String>();
		Map<String, String> maxDateTimeMap = new HashMap<String, String>();
		jdbcTemplate.query(sql.toString(), new RowMapper<Object>() {
			@Override
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				syncQueue.add(rs.getInt(1));
				acceptQueue.add(rs.getInt(2));
				connectionCount.add(rs.getInt(3));
				keepAliveCount.add(rs.getInt(4));
				threadCount.add(rs.getInt(5));
				threadsBusy.add(rs.getInt(6));
				String maxDateTime = rs.getString(7);
				maxDateTimeMap.put("MaxDateTime", maxDateTime);
				monitorTime.add(maxDateTime.substring(14, 19));
				return null;
			}
			
		});
		Map<String,Object> returnMap = new HashMap<String, Object>();
		returnMap.put("SyncQueue", syncQueue);
		returnMap.put("AcceptQueue", acceptQueue);
		returnMap.put("ConnectionCount", connectionCount);
		returnMap.put("KeepAliveCount", keepAliveCount);
		returnMap.put("ThreadCount", threadCount);
		returnMap.put("ThreadsBusy", threadsBusy);
		returnMap.put("MonitorTime", monitorTime);
		returnMap.putAll(maxDateTimeMap);
		return returnMap;
	}

	@Override
	public Map<String, Object> getRequestInfo(String serverIp, String startDate, String endDate) {
		StringBuilder sql = new StringBuilder();
		sql.append("select sum(RequestCount),sum(RequestErrorCount), ");
		sql.append(dateFormat).append(" as MT");
		sql.append(" from tb_tomcat_status where 1=1");
		if (!StringUtils.isEmpty(serverIp)) {
			sql.append(" and ServerIp in (").append(serverIp).append(")");
		}
		if (!StringUtils.isEmpty(startDate)) {
			sql.append(" and MonitorTime>='").append(startDate).append("'");
		}
		if (!StringUtils.isEmpty(endDate)) {
			sql.append(" and MonitorTime<='").append(endDate).append("'");
		}
		sql.append(" group by MT order by MonitorTime asc ");
		List<Long> requestCount = new ArrayList<Long>();
		List<Long> requestErrorCount = new ArrayList<Long>();
		List<Object> monitorTime = new ArrayList<Object>();
		Map<String, String> maxDateTimeMap = new HashMap<String, String>();
		jdbcTemplate.query(sql.toString(), new RowMapper<Object>() {
			@Override
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				requestCount.add(rs.getLong(1));
				requestErrorCount.add(rs.getLong(2));
				String maxDateTime = rs.getString(3);
				maxDateTimeMap.put("MaxDateTime", maxDateTime);
				monitorTime.add(maxDateTime.substring(14, 19));
				return null;
			}
		});
		Map<String,Object> returnMap = new HashMap<String, Object>();
		returnMap.put("RequestCount", requestCount);
		returnMap.put("RequestErrorCount", requestErrorCount);
		returnMap.put("MonitorTime", monitorTime);
		returnMap.putAll(maxDateTimeMap);
		return returnMap;
	}

}
