package com.lzj.dao.impl;

import com.lzj.dao.NginxStatusDao;
import com.lzj.dao.TomcatStatusDao;
import com.lzj.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
public class NginxStatusDaoImpl implements NginxStatusDao {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public Object saveNginxStatusList(List<NginxStatus> nginxStatusList) {
		if (nginxStatusList == null || nginxStatusList.isEmpty()) {
			return null;
		}
		List<Object[]> batchArgs = new ArrayList<Object[]>();
		for (NginxStatus nginxStatus : nginxStatusList) {
			List<Object> arg = new ArrayList<Object>();
			arg.add(nginxStatus.getIp());
			arg.add(nginxStatus.getPort());
			arg.add(nginxStatus.getAcviveConnections() == null ? 0 : nginxStatus.getAcviveConnections());
			arg.add(nginxStatus.getAcceptConnections() == null ? 0 : nginxStatus.getAcceptConnections());
			arg.add(nginxStatus.getHandledConnections() == null ? 0 : nginxStatus.getHandledConnections());
			arg.add(nginxStatus.getHandledRequests() == null ? 0 : nginxStatus.getHandledRequests());
			arg.add(nginxStatus.getWriting() == null ? 0 : nginxStatus.getWriting());
			arg.add(nginxStatus.getReading() == null ? 0 : nginxStatus.getReading());
			arg.add(nginxStatus.getWaiting() == null ? 0 : nginxStatus.getWaiting());
			arg.add(nginxStatus.getIncAcceptConnections() == null ? 0 : nginxStatus.getIncAcceptConnections());
			arg.add(nginxStatus.getIncHandledConnections() == null ? 0 : nginxStatus.getIncHandledConnections());
			arg.add(nginxStatus.getIncHandledRequests() == null ? 0 : nginxStatus.getIncHandledRequests());
			arg.add(nginxStatus.getMonitorTime());
			batchArgs.add(arg.toArray());
		}

		String sql = "insert into tb_nginx_status(IP,Port,ActiveConnections,AcceptConnections,HandledConnections,HandledRequests,Reading,Writing,Waiting,IncAcceptConnections,IncHandledConnections,IncHandledRequests,MonitorTime) "
				+ " values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
		try {
			int[] batchUpdate = jdbcTemplate.batchUpdate(sql, batchArgs);
			return batchUpdate;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Map<String, Object> getNginxInfo(String serverIp, String startDate, String endDate, Integer groupType) {
		StringBuilder sql = new StringBuilder("select ");
		String dateFormat = "DATE_FORMAT(MonitorTime,'%Y-%m-%d %H:%i:%s')";
		if (groupType != null) {
			if (groupType == 1) {
				//日
				dateFormat = "DATE_FORMAT(MonitorTime,'%Y-%m-%d')";
			} else if (groupType == 2) {
				//时
				dateFormat = "DATE_FORMAT(MonitorTime,'%Y-%m-%d %H')";
			} else if (groupType == 3) {
				//分
				dateFormat = "DATE_FORMAT(MonitorTime,'%Y-%m-%d %H:%i')";
			}
		}
		sql.append(dateFormat).append(" as MT,");
		sql.append("sum(ActiveConnections),sum(AcceptConnections),sum(HandledConnections),sum(HandledRequests),sum(Reading),sum(Writing),sum(Waiting),sum(IncAcceptConnections),sum(IncHandledConnections),sum(IncHandledRequests),max(MonitorTime) ");
		sql.append(" from tb_nginx_status where 1=1");
		if (!StringUtils.isEmpty(startDate)) {
			sql.append(" and MonitorTime>='").append(startDate).append("'");
		}
		if (!StringUtils.isEmpty(endDate)) {
			sql.append(" and MonitorTime<='").append(endDate).append("'");
		}
		if (!StringUtils.isEmpty(serverIp)) {
			sql.append(" and IP =").append(serverIp);
		}
		sql.append(" group by MT order by MonitorTime asc ");
		List<Long> activeConnections = new ArrayList<Long>();
		List<Long> acceptConnections = new ArrayList<Long>();
		List<Long> handledConnections = new ArrayList<Long>();
		List<Long> handledRequests = new ArrayList<Long>();
		List<Long> reading = new ArrayList<Long>();
		List<Long> writing = new ArrayList<Long>();
		List<Long> waiting = new ArrayList<Long>();
		List<Long> incAcceptConnections = new ArrayList<Long>();
		List<Long> incHandledConnections = new ArrayList<Long>();
		List<Long> incHandledRequests = new ArrayList<Long>();
		List<String> monitorTime = new ArrayList<String>();
		List<String> maxMonitorTimes = new ArrayList<String>();
		jdbcTemplate.query(sql.toString(), new RowMapper<Object>() {
			@Override
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				monitorTime.add(rs.getObject(1).toString());
				activeConnections.add(Long.valueOf(rs.getObject(2).toString()));
				acceptConnections.add(Long.valueOf(rs.getObject(3).toString()));
				handledConnections.add(Long.valueOf(rs.getObject(4).toString()));
				handledRequests.add(Long.valueOf(rs.getObject(5).toString()));
				reading.add(Long.valueOf(rs.getObject(6).toString()));
				writing.add(Long.valueOf(rs.getObject(7).toString()));
				waiting.add(Long.valueOf(rs.getObject(8).toString()));
				incAcceptConnections.add(Long.valueOf(rs.getObject(9).toString()));
				incHandledConnections.add(Long.valueOf(rs.getObject(10).toString()));
				incHandledRequests.add(Long.valueOf(rs.getObject(11).toString()));
				maxMonitorTimes.add(rs.getObject(12).toString());
				return null;
			}

		});
		Map<String,Object> returnMap = new HashMap<String, Object>();
		returnMap.put("MonitorTimes", monitorTime);
		returnMap.put("ActiveConnections", activeConnections);
		returnMap.put("AcceptConnections", acceptConnections);
		returnMap.put("HandledConnections", handledConnections);
		returnMap.put("HandledRequests", handledRequests);
		returnMap.put("Reading", reading);
		returnMap.put("Writing", writing);
		returnMap.put("Waiting", waiting);
		returnMap.put("IncAcceptConnections", incAcceptConnections);
		returnMap.put("IncHandledConnections", incHandledConnections);
		returnMap.put("IncHandledRequests", incHandledRequests);
		returnMap.put("MaxMonitorTimes", maxMonitorTimes);
		return returnMap;
	}
}
