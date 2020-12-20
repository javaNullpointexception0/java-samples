package com.lzj.service.impl;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lzj.config.AppConfiguration;
import com.lzj.dao.NginxLogDao;
import com.lzj.dao.NginxStatusDao;
import com.lzj.entity.NginxLog;
import com.lzj.entity.NginxStatus;
import com.lzj.service.NginxLogService;
import com.lzj.service.NginxStatusService;
import com.lzj.util.ApplicationContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

@Service
@Slf4j
public class NginxStatusServiceImpl implements NginxStatusService {

	@Autowired
	private NginxStatusDao nginxStatusDao;


	@Override
	public Object saveNginxStatusList(List<NginxStatus> nginxStatusList) {
		if (nginxStatusList == null || nginxStatusList.size() <= 0) {
			return null;
		}
		return nginxStatusDao.saveNginxStatusList(nginxStatusList);
	}

	@Override
	public Map<String, Object> getNginxInfo(String serverIp, String startDate, String endDate, Integer groupType) {
		return nginxStatusDao.getNginxInfo(serverIp, startDate, endDate, groupType);
	}
}
