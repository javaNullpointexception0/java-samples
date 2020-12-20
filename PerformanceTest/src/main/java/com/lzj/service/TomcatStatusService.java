package com.lzj.service;

import com.lzj.entity.NginxStatus;
import com.lzj.entity.TomcatStatus;

import java.util.List;

public interface TomcatStatusService {

	public Object saveTomcatStatusList(List<TomcatStatus> tomcatStatusList);
}
