package com.lzj.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lzj.config.AppConfiguration;
import com.lzj.dao.NginxLogDao;
import com.lzj.entity.NginxLog;
import com.lzj.service.NginxLogService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NginxLogServiceImpl implements NginxLogService {

	@Autowired
	private NginxLogDao nginxLogDao;
	@Autowired
	private AppConfiguration appConfiguration;
	private ObjectMapper objectMapper = new ObjectMapper();
	private JavaType mapType = objectMapper.getTypeFactory().constructParametricType(Map.class, String.class, Object.class);
	private Map<String, Integer> logPropertiesIndex = new HashMap<String, Integer>();
	private ReentrantLock locked = new ReentrantLock();
	//保存正在解析的日志目录，避免重复请求重复解析
	private Map<String, Object> explainingLogDirs = new HashMap<String, Object>();
	
	private Thread saveToDBThread = null;
	//已统计好的每一条记录存到该队列
	private ConcurrentLinkedQueue<NginxLog> nginxLogQueue = new ConcurrentLinkedQueue<NginxLog>();
	
	private SimpleDateFormat sdf = new SimpleDateFormat("dd/MMM/yyyy:hh:mm:ss Z", Locale.ENGLISH);
	private SimpleDateFormat mspsdf= new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	
	//按秒统计访问量及耗时
	private String timeLocal = null;
	//key:时间，精确到秒，value:key:客户端ip：value：日志实体
	private Map<String, Map<String, NginxLog>> nginxLogMap = new HashMap<String, Map<String, NginxLog>>();
	
	private int errorLine = 0;
	
	@Override
	public void explainNginxLog() {
		try {
			locked.lock();
			String nginxLogDir = appConfiguration.getNginxLogDir();
			if (StringUtils.isEmpty(nginxLogDir)) {
				return ;
			}
			getLogPropertiesIndex();
			boolean explained = false;
			for (String explainingLogDir : explainingLogDirs.keySet()) {
				if (nginxLogDir.length() < explainingLogDir.length() && explainingLogDir.startsWith(nginxLogDir)) {
					explained = true;
					break;
				}
			}
			if (!explained) {
				startExplainTask();
				startSaveTask();
			}
		} finally {
			locked.unlock();
		}
	}
	
	@Override
	public String findTextFromFile(String text) {
		String nginxLogDir = appConfiguration.getNginxLogDir();
		String[] filePaths = nginxLogDir.split(",");
		for (String filePath : filePaths) {
			File logDirFile = new File(filePath);
			List<File> fileList = getLogFiles(logDirFile);
			explainNginxLogFromFiles(fileList, new LineHandler() {
				@Override
				void handle(File file, String line, int lineNum) {
					try {
						fileTextWithLine(file, line, lineNum, text);
					} catch (Exception e) {
						errorLine++;
						e.printStackTrace();
					}
				}
			});
		}
		return null;
	}



	private void startExplainTask() {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				String nginxLogDir = appConfiguration.getNginxLogDir();
				File logDirFile = new File(nginxLogDir);
				List<File> fileList = getLogFiles(logDirFile);
				explainNginxLogFromFiles(fileList, new LineHandler() {
					@Override
					void handle(File file, String line, int lineNum) {
						try {
							matchNginxLogWithLine(file, line, lineNum);
						} catch (Exception e) {
							errorLine++;
							e.printStackTrace();
						}
					}
				});
				if (nginxLogMap.size() > 0) {
					for (String time : nginxLogMap.keySet()) {
						Map<String, NginxLog> map = nginxLogMap.get(time);
						for (Entry<String, NginxLog> entry : map.entrySet()) {
							nginxLogQueue.add(entry.getValue());
						}
						nginxLogMap.clear();
					}
				}
				System.out.println("错误说条数：" + errorLine);
			}
		});
		thread.start();
	}
	
	private void startSaveTask() {
		if (saveToDBThread == null) {
			saveToDBThread = new Thread(new Runnable() {
				@Override
				public void run() {
					while (true) {
						if (nginxLogQueue.size() <= 0) {
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							continue;
						}
						int size = nginxLogQueue.size() > 10000 ? 10000 : nginxLogQueue.size();
						List<NginxLog> sginxLogs = new ArrayList<NginxLog>(size);
						for (int i = 0; i < size; i++) {
							sginxLogs.add(nginxLogQueue.poll());
						}
						long start = System.currentTimeMillis();
						nginxLogDao.saveNginxLogs(sginxLogs);
						long end = System.currentTimeMillis();
						log.info("保存{}日志信息到数据库完成，耗时：{}毫秒", sginxLogs.size(), (end - start));
					}
				}
			});
			saveToDBThread.start();
		}
	}
	
	private void getLogPropertiesIndex() {
		if (logPropertiesIndex.size() > 0) {
			return;
		}
		String nginxLogPropertie = appConfiguration.getNginxLogProperties();
		String[] nginxLogProperties = nginxLogPropertie.split(",");
		Set<String> properties = new HashSet<String>();
		for (String property : nginxLogProperties) {
			properties.add(property);
		}
		
		String nginxLogPattern = appConfiguration.getNginxLogPattern();
		nginxLogPattern = nginxLogPattern.replaceAll("\\$", "");
		nginxLogPattern = nginxLogPattern.replaceAll("\"", "");
		nginxLogPattern = nginxLogPattern.replaceAll("\\[", "");
		nginxLogPattern = nginxLogPattern.replaceAll("\\]", "");
		String[] nginxLogPatterns = nginxLogPattern.split(" ");
		int len = nginxLogPatterns.length;
		for (int i = 0;i < len; i++) {
			String property = nginxLogPatterns[i].trim();
			if (properties.contains(property)) {
				logPropertiesIndex.put(property, i);
			}
		}
	}
	
	private List<File> getLogFiles(File logDirFile) {
		List<File> fileList = new ArrayList<File>();
		if (!logDirFile.exists()) {
			return fileList;
		}
		File[] files = logDirFile.listFiles();
		for (File file : files) {
			
			if (file.isFile()) {
				if (file.getName().equals("error.log")) {
					continue;
				}
				fileList.add(file);
			} else {
				fileList.addAll(getLogFiles(file));
			}
		}
		return fileList;
	}
	
	private void explainNginxLogFromFiles(List<File> fileList, LineHandler lineHandler) {
		if (fileList == null || fileList.size() <= 0) {
			return;
		}
		for (File file : fileList) {
			InputStream inputStream = null;
			BufferedReader br = null;
			String line = null;
			 int lineNum = 0;
			try {
				if (file.getName().endsWith("zip")) {
					//压缩包
					inputStream = new ZipInputStream(new FileInputStream(file));
					ZipInputStream zipInputStream = (ZipInputStream) inputStream;
				    //循环遍历
				    while (zipInputStream.getNextEntry() != null) {
				        //读取
				        br = new BufferedReader(new InputStreamReader(zipInputStream,Charset.forName("UTF-8")));
				        //内容不为空，输出
				        while ((line = br.readLine()) != null) {
				        	lineNum++;
				        	lineHandler.handle(file, line, lineNum);
				        }
				    }
				} else if (file.getName().endsWith("gz")) {
					//压缩包
					inputStream = new GZIPInputStream(new FileInputStream(file));
					GZIPInputStream gZIPInputStream = (GZIPInputStream) inputStream;
					 //读取
			        br = new BufferedReader(new InputStreamReader(gZIPInputStream, Charset.forName("UTF-8")));
			        //内容不为空，输出
			        while ((line = br.readLine()) != null) {
			        	lineNum++;
			        	lineHandler.handle(file, line, lineNum);
			        }
				} else {
					br = new BufferedReader(new FileReader(file));
					while ((line = br.readLine()) != null) {
						lineNum++;
			        	lineHandler.handle(file, line, lineNum);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	private void matchNginxLogWithLine(File file, String line, int lineNum) {
		if (StringUtils.isEmpty(line)) {
			return;
		}
		if (line.endsWith("}")) {
			if (!line.startsWith("{")) {
				//分隔后会缺少左大括号
				line = "{" + line;
			}
			matchMapLine(file, line, lineNum);
		} else {
			matchSimpleLine(file, line, lineNum);
		}
	}
	
	private void matchMapLine(File file, String line, int lineNum) {
		try {
			Map<String, Object> dataMap = objectMapper.readValue(line, mapType);
			if (!dataMap.containsKey("uri") 
					|| !dataMap.get("uri").toString().endsWith("SubscribeNotifications")) {
				return;
			}
			NginxLog nginxLog = new NginxLog();
			//remote_addr,time_local,request,status,request_time,upstream_response_time
			//{"@timestamp": "2020-03-29T10:00:02+08:00", "time": "2020-03-29T10:00:02+08:00", "remote_addr": "68.220.60.166", "remote_user": "-", "request": "POST /VIID/SubscribeNotifications HTTP/1.1", "status": "201", "body_bytes_sent": "264", "http_referrer": "-", "http_user_agent": "Java/1.8.0_181",  "http_x_forwarded_for": "-", "request_time": "0.005", "host": "68.26.12.119", "request_method": "POST", "uri": "/VIID/SubscribeNotifications"}
			nginxLog.setRemoteAddr(dataMap.get("remote_addr").toString());
			nginxLog.setTimeLocal(mspsdf.parse(dataMap.get("time").toString()));
			nginxLog.setStatus(Integer.valueOf(dataMap.get("status").toString()));
			nginxLog.setRequestTime(Float.valueOf(dataMap.get("request_time").toString()));
			nginxLog.setServerIp(dataMap.get("host").toString());
			String rowTimeLocal = dataMap.get("time").toString().split("\\+")[0];
			if (!rowTimeLocal.equals(timeLocal)) {
				if (nginxLogMap.size() > 0) {
					Map<String, NginxLog> map = nginxLogMap.get(timeLocal);
					for (Entry<String, NginxLog> entry : map.entrySet()) {
						nginxLogQueue.add(entry.getValue());
					}
					nginxLogMap.clear();
				}
				nginxLogMap.put(rowTimeLocal, new HashMap<String, NginxLog>());
				timeLocal = rowTimeLocal;
			}
			if (nginxLogMap.get(rowTimeLocal).get(nginxLog.getRemoteAddr()) == null) {
				nginxLogMap.get(rowTimeLocal).put(nginxLog.getRemoteAddr(), nginxLog);
				
			}
			NginxLog cacheNginxLog = nginxLogMap.get(rowTimeLocal).get(nginxLog.getRemoteAddr());
			cacheNginxLog.getSecondRequestTimes().add(1);
			cacheNginxLog.getSecondRequestTotalTime().add(nginxLog.getRequestTime());
			cacheNginxLog.getSecondUpstreamResponseTime().add(nginxLog.getUpstreamResponseTime());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void matchSimpleLine(File file, String line, int lineNum) {
		List<String> segments = new ArrayList<String>();
		int len = line.length();
		char endChar = ' ';
		int start = 0;
		for (int i = 0;i < len; i++) {
			char c = line.charAt(i);
			if ((c == endChar || (i == len - 1))) {
				if (line.charAt(start) == '[' && line.charAt(i - 1) != ']') {
					continue;
				}
				if (line.charAt(start) == '\"' && line.charAt(i - 1) != '\"') {
					continue;
				}
				if (i == (len - 1)) {
					i++;
				}
				String segment = line.substring(start, i).trim();
				if (!StringUtils.isEmpty(segment)) {
					segments.add(segment);
					endChar = ' ';
				}
				start = i + 1;
			}
		}
		if (segments.size() > 0) {
			String rowTimeLocal = "";
			NginxLog nginxLog = new NginxLog();
			nginxLog.setServerIp(file.getParentFile().getName());
			for (String property : logPropertiesIndex.keySet()) {
				if (logPropertiesIndex.get(property) > segments.size()) {
					continue;
				}
				String value = segments.get(logPropertiesIndex.get(property)).trim();
				if ("remote_addr".equals(property)) {
					nginxLog.setRemoteAddr(value);
				} else if ("time_local".equals(property)) {
					try {
						value = value.replace("[", "");
						value = value.replace("]", "");
						rowTimeLocal = value.split(" ")[0];
						nginxLog.setTimeLocal(sdf.parse(value));
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else if ("request".equals(property)) {
					value = value.replace("\"", "");
					String[] values = value.split(" ");

					nginxLog.setHttpMethod(values[0]);
					nginxLog.setRequestUri(values[1]);

				} else if ("status".equals(property)) {
					nginxLog.setStatus(Integer.valueOf(value));
				} else if ("request_time".equals(property)) {
					nginxLog.setRequestTime(Float.valueOf(value));
				} else if ("upstream_response_time".equals(property)) {
					nginxLog.setUpstreamResponseTime(Float.valueOf(value));
				}
			}
			if (!rowTimeLocal.equals(timeLocal)) {
				if (nginxLogMap.size() > 0) {
					Map<String, NginxLog> map = nginxLogMap.get(timeLocal);
					for (Entry<String, NginxLog> entry : map.entrySet()) {
						nginxLogQueue.add(entry.getValue());
					}
					nginxLogMap.clear();
				}
				Map<String, NginxLog> dataMap = new HashMap<String, NginxLog>();
				nginxLogMap.put(rowTimeLocal, dataMap);
				timeLocal = rowTimeLocal;
			}
			if (nginxLogMap.get(rowTimeLocal).get(nginxLog.getRemoteAddr()) == null) {
				nginxLogMap.get(rowTimeLocal).put(nginxLog.getRemoteAddr(), nginxLog);
				
			}
			NginxLog cacheNginxLog = nginxLogMap.get(rowTimeLocal).get(nginxLog.getRemoteAddr());
			cacheNginxLog.getSecondRequestTimes().add(1);
			cacheNginxLog.getSecondRequestTotalTime().add(nginxLog.getRequestTime());
			cacheNginxLog.getSecondUpstreamResponseTime().add(nginxLog.getUpstreamResponseTime());
		}
	}
	
	private void fileTextWithLine(File file, String line, int lineNum, String text) {
		if (!line.contains(text)) {
			return;
		}
		FileWriter fw = null;
		try {
			String dir = System.getProperty("user.dir");
			File resultFile = new File(dir + "/" + "findTextResult.txt");
			if (!resultFile.exists()) {
				resultFile.createNewFile();
			}
			fw = new FileWriter(resultFile, true);
			fw.write(file.getAbsolutePath() + ":" + lineNum);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fw != null) {
				try {
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	abstract class LineHandler {
		abstract void handle(File file, String line, int lineNum);
	}
}
