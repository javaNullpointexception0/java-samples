drop database if EXISTS tomcat_status;
create database tomcat_status default CHARACTER set utf8;
use tomcat_status;

create table tb_tomcat_status(
	ServerIp varchar(32) comment '服务端ip地址',
	SyncQueueSize int comment '半连接队列大小',
	AcceptQueueSize int comment '全连接队列大小',
	ConnectionCount int comment '连接数',
	KeepAliveCount int comment '长连接数量',
	CurrentThreadCount int comment '当前服务线程数',
	CurrentThreadsBusy int comment '当前服务正在运行线程数',
	RequestCount long comment '请求总数',
	RequestErrorCount long comment '异常的请求总数',
	MaxRequestProcessingTime long comment '最长一次请求处理时间',
	NewGenerationCapacity int comment '年轻代总容量',
	NewGenerationUsed int comment '年轻代已使用容量',
	TenuredGenerationCapacity int comment '老年代总容量',
	TenuredGenerationUsed int comment '老年代已使用容量',
	MonitorTime DateTime not null comment '采集监控数据时间'
);
create index idx_tomcat_status_ct on tb_tomcat_status(MonitorTime);

create table tb_nginx_status(
  IP varchar(32) comment '被监控服务的ip',
  Port int comment '被监控服务的端口',
	ActiveConnections int comment '当前活跃连接数',
	AcceptConnections long comment '总共处理了多少个连接',
	HandledConnections long comment '成功创建多少次握手',
	HandledRequests long comment '总共处理了多少个请求',
	Reading int comment '读取客户端的连接数.',
	Writing int comment '响应数据到客户端的数量',
	Waiting int comment '开启 keep-alive 的情况下,这个值等于 active – (reading+writing), 意思就是 Nginx 已经处理完正在等候下一次请求指令的驻留连接.',
	IncAcceptConnections int comment '与上一次检测相比，新增accept连接数',
	IncHandledConnections int comment '与上一次检测相比，新增完成握手连接数',
	IncHandledRequests int comment '与上一次检测相比，新增请求数',
	MonitorTime DateTime not null comment '采集监控数据时间'
);
create index idx_nginx_status_ct on tb_nginx_status(MonitorTime);



CREATE TABLE tb_nginx_log (
  ServerIP varchar(32) DEFAULT NULL comment 'nging服务端ip地址',
  RemoteAddr varchar(32) DEFAULT NULL comment '客户端ip地址',
  TimeLocal datetime DEFAULT NULL comment '访问时间',
  HttpMethod varchar(16) DEFAULT NULL comment 'HTTP请求方法（POST/PUT/GET等）',
  RequestUri varchar(256) DEFAULT NULL comment '请求资源路径',
  Status int(11) DEFAULT NULL comment '响应状态码',
  RequestTime float DEFAULT NULL comment '整个HTTP请求时间',
  UpstreamResponseTime float DEFAULT NULL comment '上游服务器响应时间',
  SecondRequestTimes int(11) DEFAULT NULL comment '一秒HTTP请求数',
  SecondRequestTotalTime float DEFAULT NULL comment '一秒HTTP请求总时间',
  SecondUpstreamResponseTime float DEFAULT NULL comment '一秒上游服务器响应总时间',
  KEY idx_TimeLocal (`TimeLocal`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;