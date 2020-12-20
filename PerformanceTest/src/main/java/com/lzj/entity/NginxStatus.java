package com.lzj.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class NginxStatus {
    private String ip;
    private Integer port;
    private Integer acviveConnections;
    //总共处理了多少个连接
    private Long acceptConnections;
    //成功创建多少次握手
    private Long handledConnections;
    //总共处理了多少个请求
    private Long handledRequests;
    //读取客户端的连接数.
    private Integer reading;
    //writing — 响应数据到客户端的数量
    private Integer waiting;
    //waiting — 开启 keep-alive 的情况下,这个值等于 active – (reading+writing), 意思就是 Nginx 已经处理完正在等候下一次请求指令的驻留连接.
    private Integer writing;

    //与上一次检测相比，新增accept连接数
    private Integer incAcceptConnections;
    //与上一次检测相比，新增完成握手连接数
    private Integer incHandledConnections;
    //与上一次检测相比，新增请求数
    private Integer incHandledRequests;

    private Date monitorTime;
}
