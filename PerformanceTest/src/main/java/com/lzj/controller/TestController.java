package com.lzj.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/test")
public class TestController {
	
	@Autowired
	private ObjectMapper objectMapper;

	@RequestMapping("/test")
	public String explainNginxLog() {
		long startTime = System.currentTimeMillis();
		Random r = new Random();
		try {
			Thread.sleep(r.nextInt(3) * 1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		long endTime = System.currentTimeMillis();
		System.out.println(endTime - startTime);
		return "test";
	}
	
	@PostMapping("/receiveData")
	public String receiveData(@RequestBody String data) {
		System.out.println(data.length());
		return "success";
	}
	
	@RequestMapping("/postData")
	public String postData() {
		String data = getData();
		for (int i = 0; i < 16; i++) {
			new Thread(new Runnable() {
				private RestTemplate restTemplate = new RestTemplate();
				@SuppressWarnings("unchecked")
				@Override
				public void run() {
					for (int j = 0; j < 100; j++) {
						try {
							restTemplate.postForEntity("http://192.168.6.128:17700/performance/test/receiveData", data, String.class, Collections.EMPTY_MAP);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}).start();
		}
		return "success";
	}
	
	private String getData() {
		try {
			return FileCopyUtils.copyToString(new FileReader(new File("data.txt")));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args) {
		String data;
		try {
			data = FileCopyUtils.copyToString(new FileReader(new File("1.jpeg")));
			System.out.println(Base64.getEncoder().encodeToString(data.getBytes()));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@RequestMapping("/readFile")
	public String findTextFromFile(String text) {
		try {
			String content = FileCopyUtils.copyToString(new FileReader(new File("findTextResult.txt")));
			objectMapper.readTree(content);
			Thread.sleep(new Random(1000).nextLong());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "成功";
	}
}
