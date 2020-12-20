package com.lzj.dao;

import java.util.List;

import com.lzj.entity.TomcatStatus;

public interface TomcatStatusDao {

	public Object saveTomcatStatus(TomcatStatus tomcatStatus);
	
	public Object saveTomcatStatusList(List<TomcatStatus> tomcatStatusList);
	
}
