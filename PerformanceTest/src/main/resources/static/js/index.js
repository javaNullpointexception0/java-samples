var requestUrl="http://localhost:8080/performance";
var maxDataSize=3600 * 2;
var requestChart;
var threadChart;
var threadChartMaxDateTime = "";
var requestChartMaxDateTime = "";
var flushInterval = 0;
$(function(){
	initDateTime();
	initServerList();
	initConfigList();
	initChart();
});

function initDateTime() {
	$("#startDate").val(timeStamp2String(new Date().getTime() - 1 * 60 * 60 * 1000));
	$("#endDate").val(timeStamp2String(new Date().getTime()));
}

function initConfigList(){
	$.ajax({
		type : "GET",
		url : requestUrl + "/page/getConfig",
		contentType : "application/json",
		success : function(configList) {
			var html = "";
			for (var i in configList) {
				var config = configList[i];
				html += config.serverIp + ":&nbsp;&nbsp;";
				html += "<label>半连接队列大小=<span>" + config.backlog + "</span></label>&nbsp;&nbsp;";
				html += "<label>全连接队列大小=<span>" + config.acceptCount + "</span></label>&nbsp;&nbsp;";
				html += "<label>最大连接数=<span>" + config.maxConnections + "</span></label>&nbsp;&nbsp;";
				html += "<label>最大线程数=<span>" + config.maxThreads + "</span></label>&nbsp;&nbsp;";
				html += "<label>最小空闲线程数=<span>" + config.minSpareThreads + "</span></label>&nbsp;&nbsp;";
				html += "</br>";
			}
			$(".config").append(html);
			 $('#serverlistSelect').multipleSelect({
                 selectAll:false
                 ,placeholder:'请选择'
             }).change(function(){
                 $("#serverIp").val($(this).val()?$(this).val().join("','"):"");
             });
			//$(".diagram").height($(".container").height() - $(".config").height())
		},
		failure: function() {
			alert("请求后台获取服务器列表异常");
		}
	});
}

function initServerList(){
	$.ajax({
		type : "GET",
		url : requestUrl + "/page/getServerList",
		contentType : "application/json",
		success : function(serverList) {
			var html = "";
			for (var i in serverList) {
				html += "<option value='" + serverList[i] + "'>" + serverList[i] + "</option>";
			}
			$(".serverlist select").append(html);
		},
		failure: function() {
			alert("请求后台获取服务器列表异常");
		}
	});
}

function initChart(){
	var startDate = $("#startDate").val();
	var endDate = $("#endDate").val();
	if (startDate == "") {
		alert("开始时间必选！");
		return;
	}
	var endDateLong = 0;
	if (endDate != "") {
		endDateLong = new Date(Date.parse(endDate.replace(/-/g, "/"))).getTime();
	} else {
		endDateLong = new Date().getTime();
	}
	var startDateLong = new Date(Date.parse(startDate.replace(/-/g, "/"))).getTime();
	if ((endDateLong - startDateLong)/1000 > maxDataSize) {
		alert("查询时间范围[" + timeStamp2String(startDateLong) + "," + timeStamp2String(endDateLong) + "]不能超过两小时！");
		return;
	}
	
	
	$.ajax({
		type : "GET",
		url : requestUrl + "/page/getThreadInfo" + getQueryCondition(),
		contentType : "application/json",
		success : function(data) {
			if (data.MaxDateTime != null) {
				threadChartMaxDateTime = data.MaxDateTime;
			}
			initThreadChart(data);
		},
		failure: function() {
			alert("请求后台获取服务器列表异常");
		}
	});
	$.ajax({
		type : "GET",
		url : requestUrl + "/page/getRequestInfo" + getQueryCondition(),
		contentType : "application/json",
		success : function(data) {
			if (data.MaxDateTime != null) {
				requestChartMaxDateTime = data.MaxDateTime;
			}
			initRequestChart(data);
		},
		failure: function() {
			alert("请求后台获取服务器列表异常");
		}
	});
}

function getQueryCondition() {
	var startDate = $("#startDate").val();
	var endDate = $("#endDate").val();
	var serverIp =  $('#serverIp').val();
	var condition = "?";
	var append = false;
	if (serverIp != "") {
		condition += "serverIp='" + serverIp + "'";
		append = true;
	}
	if (startDate != "") {
		if (append) {
			condition += "&";
		}
		condition += "startDate=" + startDate;
		append = true;
	}
	if (endDate != "") {
		if (append) {
			condition += "&";
		}
		condition += "endDate=" + endDate;
		append = true;
	}
	return condition;
}

function initThreadChart(data) {
	var option = {
            backgroundColor: '#FBFBFB',
            tooltip : {
                trigger: 'axis'
            },
            legend: {
                data:['sync队列','accept队列','连接数','keepalive连接数','线程数','活动线程数']
            },
            calculable : true,
            dataZoom:{
        		type: 'slider',
        		show: false,
        		xAxisIndex: [0],
        		left: '9%',
        		bottom: -5,
        		start: 98,
        		end: 100 //初始化滚动条
            },
            xAxis : [
                {
                    axisLabel:{
                        rotate: 30,
                        interval:0
                    },
                    axisLine:{
                      lineStyle :{
                          color: '#CECECE'
                      }
                    },
                    type : 'category',
                    boundaryGap : false,
                    data : data.MonitorTime
                }
            ],
            yAxis : [
                {

                    type : 'value',
                    axisLine:{
                        lineStyle :{
                            color: '#CECECE'
                        }
                    }
                }
            ],
            series : [
                {
                    name:'sync队列',
                    type:'line',
                    symbol:'none',
                    smooth: 0.2,
                    color:'#66AEDE',
                    data:data.SyncQueue
                },
                {
                    name:'accept队列',
                    type:'line',
                    symbol:'none',
                    smooth: 0.2,
                    color:'#90EC7D',
                    data:data.AcceptQueue
                },
                {
                    name:'连接数',
                    type:'line',
                    symbol:'circle',
                    smooth: 0.2,
                    color:'#FFFF00',
                    data:data.ConnectionCount
                },
                {
                    name:'keepalive连接数',
                    type:'line',
                    symbol:'triangle',
                    smooth: 0.2,
                    color:'#31B404',
                    data:data.KeepAliveCount
                },
                {
                    name:'线程数',
                    type:'line',
                    symbol:'none',
                    smooth: 0.2,
                    color:'#0101DF',
                    data:data.ThreadCount
                },
                {
                    name:'活动线程数',
                    type:'line',
                    symbol:'none',
                    smooth: 0.2,
                    color:'#01DFD7',
                    data:data.ThreadsBusy
                }
            ]
        };
	if (data.MonitorTime.length > 50) {
		option.dataZoom.show = true;
		option.dataZoom.start = 98;
		option.dataZoom.end = 100;
	} else {
		option.dataZoom.show = false;
		option.dataZoom.start = 0;
		option.dataZoom.end = 100;
	}
	threadChart = echarts.init(document.getElementById("thread"));
	threadChart.setOption(option);
}

function initRequestChart(data) {
	var dataArray = calcRequest(data.RequestCount, data.RequestErrorCount, data.MonitorTime);
	var option = {
            backgroundColor: '#FBFBFB',
            tooltip : {
                trigger: 'axis'
            },
            legend: {
                data:['请求数','错误请求数']
            },
            calculable : true,
            dataZoom:{
        		type: 'slider',
        		show: false,
        		xAxisIndex: [0],
        		left: '9%',
        		bottom: -5,
        		start: 98,
        		end: 100 //初始化滚动条
            },
            xAxis : [
                {
                    axisLabel:{
                        rotate: 30,
                        interval:0
                    },
                    axisLine:{
                      lineStyle :{
                          color: '#CECECE'
                      }
                    },
                    type : 'category',
                    boundaryGap : false,
                    data : dataArray[2]
                }
            ],
            yAxis : [
                {

                    type : 'value',
                    axisLine:{
                        lineStyle :{
                            color: '#CECECE'
                        }
                    }
                }
            ],
            series : [
                {
                    name:'请求数',
                    type:'line',
                    symbol:'none',
                    smooth: 0.2,
                    color:['#66AEDE'],
                    data:dataArray[0]
                },
                {
                    name:'错误请求数',
                    type:'line',
                    symbol:'none',
                    smooth: 0.2,
                    color:['#90EC7D'],
                    data:dataArray[1]
                }
            ]
        };
	if (dataArray[2].length > 50) {
		option.dataZoom.show = true;
		option.dataZoom.start = 98;
		option.dataZoom.end = 100;
	} else {
		option.dataZoom.show = false;
		option.dataZoom.start = 0;
		option.dataZoom.end = 100;
	}
	requestChart = echarts.init(document.getElementById("request"));
	requestChart.setOption(option);
}

function initMemoryChart() {
	var option = {
            backgroundColor: '#FBFBFB',
            tooltip : {
                trigger: 'axis'
            },
            legend: {
                data:['充值','消费']
            },
            calculable : true,
            xAxis : [
                {
                    axisLabel:{
                        rotate: 30,
                        interval:0
                    },
                    axisLine:{
                      lineStyle :{
                          color: '#CECECE'
                      }
                    },
                    type : 'category',
                    boundaryGap : false,
                    data : function (){
                        var list = [];
                        for (var i = 10; i <= 18; i++) {
                            if(i<= 12){
                                list.push('2016-'+i + '-01');
                            }else{
                                list.push('2017-'+(i-12) + '-01');
                            }
                        }
                        return list;
                    }()
                }
            ],
            yAxis : [
                {

                    type : 'value',
                    axisLine:{
                        lineStyle :{
                            color: '#CECECE'
                        }
                    }
                }
            ],
            series : [
                {
                    name:'充值',
                    type:'line',
                    symbol:'none',
                    smooth: 0.2,
                    color:['#66AEDE'],
                    data:[800, 300, 500, 800, 300, 600,500,600]
                },
                {
                    name:'消费',
                    type:'line',
                    symbol:'none',
                    smooth: 0.2,
                    color:['#90EC7D'],
                    data:[600, 300, 400, 200, 300, 300,200,400]
                }
            ]
        };

	var threadChart = echarts.init(document.getElementById("memory"));
	threadChart.setOption(option);
}

function autoFlush() {
	if (flushInterval == 0) {
		flushInterval = setInterval(function(){
			flushChart();
		},2000);
		$("#flushButton").text("暂停刷新");
	} else {
		clearInterval(flushInterval);
		flushInterval = 0;
		$("#flushButton").text("自动刷新");
	}
	
}

function flushChart() {
	var startDate = "";
	
	var serverIpCondition = "";
	var serverIp = $('#serverIp').val();
	if (serverIp != "") {
		serverIpCondition += "serverIp='" + serverIp + "'";
	}
	
	var queryCondition = "";
	if (threadChartMaxDateTime != "") {
		var date = new Date(Date.parse(threadChartMaxDateTime.replace(/-/g, "/"))).getTime() + 1000;
		if ((new Date().getTime() - date)/1000 > maxDataSize) {
			alert("查询时间范围[" + timeStamp2String(date) + "," + "当前]不能超过两小时！");
			return;
		}
		startDate = timeStamp2String(date);
	} else {
		startDate = timeStamp2String(new Date().getTime());
	}
	
	
	queryCondition = queryCondition + "startDate=" + startDate;
	if (serverIpCondition != "") {
		queryCondition = queryCondition + "&" + serverIpCondition;
	}
	$.ajax({
		type : "GET",
		url : requestUrl + "/page/getThreadInfo?" + queryCondition,
		contentType : "application/json",
		success : function(data) {
			if (data.MonitorTime.length <= 0) {
				return;
			}
			if (data.MaxDateTime != null) {
				threadChartMaxDateTime = data.MaxDateTime;
			}
			var option = threadChart.getOption();
			var syncQueue = option.series[0].data;
			for (var i in data.SyncQueue) {
				syncQueue.push(data.SyncQueue[i]);
				if (syncQueue.length >= maxDataSize) {
					syncQueue.shift()
				}
			}
			var acceptQueue = option.series[1].data;
			for (var i in data.AcceptQueue) {
				acceptQueue.push(data.AcceptQueue[i]);
				if (acceptQueue.length >= maxDataSize) {
					acceptQueue.shift()
				}
			}
			var connectionCount = option.series[2].data;
			for (var i in data.ConnectionCount) {
				connectionCount.push(data.ConnectionCount[i]);
				if (connectionCount.length >= maxDataSize) {
					connectionCount.shift()
				}
			}
			var keepAliveCount = option.series[3].data;
			for (var i in data.KeepAliveCount) {
				keepAliveCount.push(data.KeepAliveCount[i]);
				if (keepAliveCount.length >= maxDataSize) {
					keepAliveCount.shift()
				}
			}
			var threadCount = option.series[4].data;
			for (var i in data.ThreadCount) {
				threadCount.push(data.ThreadCount[i]);
				if (threadCount.length >= maxDataSize) {
					threadCount.shift()
				}
			}
			var threadsBusy = option.series[5].data;
			for (var i in data.ThreadsBusy) {
				threadsBusy.push(data.ThreadsBusy[i]);
				if (threadsBusy.length >= maxDataSize) {
					threadsBusy.shift()
				}
			}
			var monitorTime = option.xAxis[0].data;
			for (var i in data.MonitorTime) {
				monitorTime.push(data.MonitorTime[i]);
				if (monitorTime.length > maxDataSize) {
					monitorTime.shift()
				}
			}
			if (monitorTime.length > 50) {
				option.dataZoom.show = true;
				option.dataZoom.start = 98;
				option.dataZoom.end = 100;
			} else {
				option.dataZoom.show = false;
				option.dataZoom.start = 0;
				option.dataZoom.end = 100;
			}
			threadChart.setOption(option, true);
		},
		failure: function() {
			alert("请求后台获取服务器列表异常");
		}
	});
	
	queryCondition = "";
	if (requestChartMaxDateTime != "") {
		var date = new Date(Date.parse(requestChartMaxDateTime.replace(/-/g, "/"))).getTime();
		if ((new Date().getTime() - date)/1000 > maxDataSize) {
			alert("查询时间范围[" + timeStamp2String(date) + "," + "当前]不能超过两小时！");
			return;
		}
		startDate = timeStamp2String(date);
	} else {
		startDate = timeStamp2String(new Date().getTime());
	}
	
	
	queryCondition = queryCondition + "startDate=" + startDate;
	if (serverIpCondition != "") {
		queryCondition = queryCondition + "&" + serverIpCondition;
	}
	
	$.ajax({
		type : "GET",
		url : requestUrl + "/page/getRequestInfo?" + queryCondition,
		contentType : "application/json",
		success : function(data) {
			if (data.MonitorTime.length <= 0) {
				return;
			}
			if (data.MaxDateTime != null) {
				requestChartMaxDateTime = data.MaxDateTime;
			}
			var option = requestChart.getOption();
			var dataArray = calcRequest(data.RequestCount, data.RequestErrorCount, data.MonitorTime);
			var requestCount = option.series[0].data;
			for (var i in dataArray[0]) {
				requestCount.push(dataArray[0][i]);
				if (requestCount.length >= maxDataSize) {
					requestCount.shift()
				}
			}
			
			var requestErrorCount = option.series[1].data;
			for (var i in dataArray[1]) {
				requestErrorCount.push(dataArray[1][i]);
				if (requestErrorCount.length >= maxDataSize) {
					requestErrorCount.shift()
				}
			}
			
			var monitorTime = option.xAxis[0].data;
			for (var i in dataArray[2]) {
				monitorTime.push(dataArray[2][i]);
				if (monitorTime.length > maxDataSize) {
					monitorTime.shift()
				}
			}
			if (monitorTime.length > 50) {
				option.dataZoom.show = true;
				option.dataZoom.start = 98;
				option.dataZoom.end = 100;
			} else {
				option.dataZoom.show = false;
				option.dataZoom.start = 0;
				option.dataZoom.end = 100;
			}
			requestChart.setOption(option, true);
		},
		failure: function() {
			alert("请求后台获取服务器列表异常");
		}
	});
}

function calcRequest(requestCount, requestErrorCount, monitorTime) {
	if (requestCount.length > 1) {
		var size = requestCount.length - 1;
		var showRequestCount = new Array();
		var showRequestErrorCount = new Array();
		var showMonitorTime = new Array();
		for (var i = 0; i < size; i++) {
			showRequestCount.push(requestCount[i + 1] - requestCount[i]);
			showRequestErrorCount.push(requestErrorCount[i + 1] - requestErrorCount[i]);
			showMonitorTime.push(monitorTime[i]);
		}
		requestCount = showRequestCount;
		requestErrorCount = showRequestErrorCount;
		monitorTime = showMonitorTime;
	}
	return [requestCount, requestErrorCount, monitorTime];
}

function timeStamp2String(time){
    var datetime = new Date();
    datetime.setTime(time);
    var year = datetime.getFullYear();
    var month = datetime.getMonth() + 1 < 10 ? "0" + (datetime.getMonth() + 1) : datetime.getMonth() + 1;
    var date = datetime.getDate() < 10 ? "0" + datetime.getDate() : datetime.getDate();
    var hour = datetime.getHours()< 10 ? "0" + datetime.getHours() : datetime.getHours();
    var minute = datetime.getMinutes()< 10 ? "0" + datetime.getMinutes() : datetime.getMinutes();
    var second = datetime.getSeconds()< 10 ? "0" + datetime.getSeconds() : datetime.getSeconds();
    return year + "-" + month + "-" + date+" "+hour+":"+minute+":"+second;
}