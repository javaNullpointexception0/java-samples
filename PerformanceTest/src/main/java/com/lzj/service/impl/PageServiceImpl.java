package com.lzj.service.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lzj.dao.PageDao;
import com.lzj.service.PageService;

@Service
public class PageServiceImpl implements PageService {
	
	@Autowired
	private PageDao pageDao;

	@Override
	public Map<String, Object> getThreadInfo(String serverIp, String startDate, String endDate) {
		return pageDao.getThreadInfo(serverIp, startDate, endDate);
	}

	@Override
	public Map<String, Object> getRequestInfo(String serverIp, String startDate, String endDate) {
		return pageDao.getRequestInfo(serverIp, startDate, endDate);
	}

}
