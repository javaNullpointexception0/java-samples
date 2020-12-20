package com.lzj.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.alibaba.druid.pool.DruidDataSource;

@Component
public class DataSourceConfig {

	@Autowired
	private AppConfiguration appConfig;
	
	@Bean
	@Primary
	public DataSource dataSource() {
		DruidDataSource dataSource = new DruidDataSource();
		dataSource.setDriverClassName(appConfig.getDbDriverName());
		dataSource.setUrl(appConfig.getDbUrl());
		dataSource.setUsername(appConfig.getDbUserName());
		dataSource.setPassword(appConfig.getDbPassword());
		dataSource.setInitialSize(5);
		dataSource.setMinIdle(5);
		dataSource.setMaxActive(10);
		dataSource.setMaxWait(60000L);
		dataSource.setMinEvictableIdleTimeMillis(300000L);
		return dataSource;
	}
	
	@Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
