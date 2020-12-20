package com.lzj.dao.impl;

import java.text.SimpleDateFormat;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.lzj.dao.NginxLogDao;
import com.lzj.entity.NginxLog;

@Repository
public class NginxLogDaoImpl implements NginxLogDao {

	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public Object saveNginxLogs(List<NginxLog> nginxLogList) {
		if (nginxLogList == null || nginxLogList.isEmpty()) {
			return null;
		}
		try {
			StringBuilder sql = new StringBuilder("insert into tb_nginx_log(ServerIP,RemoteAddr,TimeLocal,HttpMethod,ResouceType,Status,RequestTime,UpstreamResponseTime, SecondRequestTimes,SecondRequestTotalTime,SecondUpstreamResponseTime) values ");
			for (NginxLog nginxLog : nginxLogList) {
				sql.append("('").append(nginxLog.getServerIp()).append("',");
				sql.append("'").append(nginxLog.getRemoteAddr()).append("',");
				sql.append("'").append(sdf.format(nginxLog.getTimeLocal())).append("',");
				sql.append("'").append(nginxLog.getHttpMethod()).append("',");
				sql.append(nginxLog.getRequestUri()).append(",");
				sql.append(nginxLog.getStatus()).append(",");
				sql.append(nginxLog.getRequestTime()).append(",");
				sql.append(nginxLog.getUpstreamResponseTime()).append(",");
				sql.append(nginxLog.getSecondRequestTimes()).append(",");
				sql.append(nginxLog.getSecondRequestTotalTime()).append(",");
				sql.append(nginxLog.getSecondUpstreamResponseTime()).append("),");
			}
			sql.deleteCharAt(sql.length() - 1);
			int batchUpdate = jdbcTemplate.update(sql.toString());
			return batchUpdate;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
