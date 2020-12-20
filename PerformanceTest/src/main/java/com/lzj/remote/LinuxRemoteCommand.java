package com.lzj.remote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

public class LinuxRemoteCommand implements RemoteCommand {
	
	private static final String DEFAULT_CHART = "UTF-8"; 
	private static final Logger log = LoggerFactory.getLogger(LinuxRemoteCommand.class);
	private String serverIp;
	private Integer sshPort;
	private Integer servicePort;
	private String userName;
	private String password;
	private Connection conn;
	
	public LinuxRemoteCommand(String ip, Integer sshPort, Integer servicePort, String userName, String password) {
		this.serverIp = ip;
		this.sshPort = sshPort;
		this.servicePort = servicePort;
		this.userName = userName;
		this.password = password;
		conn = initConnection();
	}
	/**
	 * 获取半连接队列当前连接数
	 */
	@Override
	public int getSyncQueueSize() {
		String cmd = "ss -n state syn-recv sport = :" + servicePort + " | wc -l";
		String result = execute(cmd);
		return StringUtils.isEmpty(result) == true ? 0 : Integer.parseInt(result) - 1;
	}

	/**
	 * 获取半连接队列当前连接数
	 */
	@Override
	public int getAcceptQueueSize() {
		String cmd = "ss -plnt sport = :" + servicePort + " | cat";
		String result = execute(cmd);
		result = result.split("\n")[1].split("\\s+")[1]; 
		return StringUtils.isEmpty(result) == true ? 0 : Integer.parseInt(result);
	}
	
	
	@Override
	public void close() {
		if (conn != null) {
			conn.close();
		}
	}
	/** 
     * 登录主机 
     * @return 
     *      登录成功返回true，否则返回false 
     */  
    private Connection initConnection() {
        try {
            conn = new Connection(serverIp, sshPort);
            conn.connect();
            boolean flg = conn.authenticateWithPassword(userName, password);  
            if(flg){
            	log.error("登录{}服务器成功", serverIp);
            } else {
            	log.error("登录{}服务器失败", serverIp);
            }
        } catch (IOException e) {  
            log.error("登录{}服务器发生异常：{}", serverIp, e.getMessage());
            e.printStackTrace();  
        }  
        return conn;  
    }
    
    
    private String execute(String cmd) {
    	String commandResult = "";
    	try {  
            if(conn != null){  
                Session session= conn.openSession(); 
                session.execCommand(cmd);  
                commandResult = processStdout(session.getStdout());
                session.close();  
            }  
        } catch (IOException e) {
            log.info("在{}服务器上执行{}命令发生异常，异常信息：{}", serverIp, cmd, e.getMessage());
            e.printStackTrace();  
        }
    	return commandResult;
    }
    /** 
     * 解析脚本执行返回的结果集 
     * @param in 输入流对象 
     * @return 
     *       以纯文本的格式返回 
     */  
     private static String processStdout(InputStream in){  
         InputStream  stdout = new StreamGobbler(in);
         BufferedReader br = null;
         StringBuffer buffer = new StringBuffer();;  
         try {  
             br = new BufferedReader(new InputStreamReader(stdout, DEFAULT_CHART));  
             String line = null;  
             while((line = br.readLine()) != null){  
            	 if (buffer.length() > 0) {
            		 buffer.append("\n");
            	 }
                 buffer.append(line);  
             }
         } catch (UnsupportedEncodingException e) { 
             log.error("解析脚本出错：" + e.getMessage());
             e.printStackTrace();  
         } catch (IOException e) {
             log.error("解析脚本出错：" + e.getMessage());
             e.printStackTrace();  
         } finally {
        	 if (br != null) {
        		 try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	 }
        	 if (stdout != null) {
        		 try {
					stdout.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	 }
         }
         return buffer.toString();  
     }

}
