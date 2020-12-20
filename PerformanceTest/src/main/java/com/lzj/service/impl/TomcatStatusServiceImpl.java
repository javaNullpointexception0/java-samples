package com.lzj.service.impl;

import com.lzj.dao.NginxStatusDao;
import com.lzj.dao.TomcatStatusDao;
import com.lzj.entity.NginxStatus;
import com.lzj.entity.TomcatStatus;
import com.lzj.service.NginxStatusService;
import com.lzj.service.TomcatStatusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class TomcatStatusServiceImpl implements TomcatStatusService {

	@Autowired
	private TomcatStatusDao tomcatStatusDao;


	@Override
	public Object saveTomcatStatusList(List<TomcatStatus> tomcatStatusList) {
		if (tomcatStatusList == null || tomcatStatusList.size() <= 0) {
			return null;
		}
		return tomcatStatusDao.saveTomcatStatusList(tomcatStatusList);
	}
}
