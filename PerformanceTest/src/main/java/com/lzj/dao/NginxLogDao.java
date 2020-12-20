package com.lzj.dao;

import java.util.List;

import com.lzj.entity.NginxLog;

public interface NginxLogDao {

	public Object saveNginxLogs(List<NginxLog> nginxLogList);
}
