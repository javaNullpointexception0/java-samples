package com.lzj.nginx;

import com.lzj.entity.NginxStatus;
import com.lzj.util.ApplicationContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Date;

public class NginxStatusHarvester {

    private static final Logger log = LoggerFactory.getLogger(NginxStatusHarvester.class);
    //nginx ip
    private String nginxIp;
    //nginx 端口，默认80
    private Integer nginxPort = 80;
    //获取nginx status url
    private String nginxStatusUrl;

    private NginxStatus nginxStatus;


    public NginxStatusHarvester(String nginxIp, Integer nginxPort) {
        this.nginxIp = nginxIp;
        this.nginxPort = nginxPort;
        this.nginxStatusUrl = "http://" + nginxIp + ":" + nginxPort + "/status";
    }

    public NginxStatus getNginxStatus() {
        RestTemplate restTemplate = ApplicationContextUtil.getBean(RestTemplate.class);
        if (restTemplate == null || StringUtils.isEmpty(nginxStatusUrl)) {
            return null;
        }
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(nginxStatusUrl, String.class, Collections.emptyMap());
        if (responseEntity.getStatusCodeValue() != HttpStatus.OK.value()) {
            log.warn("请求：{}获取监控信息时失败，HTTP响应状态码：{}，响应内容：{}",
                    nginxStatusUrl, responseEntity.getStatusCodeValue(), responseEntity.getBody());
            return null;
        }
        /*
        解析返回内容
        Active connections: 9787
        server accepts handled requests
         515611486 515611486 1487172614
        Reading: 208 Writing: 331 Waiting: 9248
         */
        String result = responseEntity.getBody();
        String[] lines = result.split("\n");

        Integer acviveConnections = Integer.valueOf(lines[0].split(":")[1].trim());

        String[] httpCounts = lines[2].trim().split(" ");
        Long acceptConnections = Long.valueOf(httpCounts[0].trim());
        Long handledConnections = Long.valueOf(httpCounts[1].trim());
        Long handledRequests = Long.valueOf(httpCounts[2].trim());

        if (nginxStatus == null) {
            nginxStatus = new NginxStatus();
            nginxStatus.setAcceptConnections(acceptConnections);
            nginxStatus.setHandledConnections(handledConnections);
            nginxStatus.setHandledRequests(handledRequests);
            return null;
        }
        NginxStatus newNginxStatus = new NginxStatus();
        newNginxStatus.setIp(nginxIp);
        newNginxStatus.setPort(nginxPort);
        newNginxStatus.setAcviveConnections(acviveConnections);
        newNginxStatus.setAcceptConnections(acceptConnections);
        newNginxStatus.setHandledConnections(handledConnections);
        newNginxStatus.setHandledRequests(handledRequests);

        String[] connectionStatusCounts = lines[3].trim().split(" ");
        Integer writing = Integer.valueOf(connectionStatusCounts[1].trim());
        Integer reading = Integer.valueOf(connectionStatusCounts[3].trim());
        Integer waiting = Integer.valueOf(connectionStatusCounts[5].trim());
        newNginxStatus.setWriting(writing);
        newNginxStatus.setReading(reading);
        newNginxStatus.setWaiting(waiting);

        //计算与上一次差值
        Integer incAcceptConnections = ((Long)(newNginxStatus.getAcceptConnections() - nginxStatus.getAcceptConnections())).intValue();
        Integer incHandledConnections = ((Long)(newNginxStatus.getHandledConnections() - nginxStatus.getHandledConnections())).intValue();
        Integer incHandledRequests = ((Long)(newNginxStatus.getHandledRequests() - nginxStatus.getHandledRequests())).intValue();
        newNginxStatus.setIncAcceptConnections(incAcceptConnections);
        newNginxStatus.setIncHandledConnections(incHandledConnections);
        newNginxStatus.setIncHandledRequests(incHandledRequests);
        newNginxStatus.setMonitorTime(new Date());
        nginxStatus = newNginxStatus;
        return newNginxStatus;
    }
}
