var requestUrl="http://localhost:8080/performance";
var maxDataSize=1000;
var activeConnectionsChart;
var keepaliveChart;
var incChart;
var maxDateTime = "";
var flushInterval = 0;
$(function(){
	initDateTime();
	initChart();
});

function initDateTime() {
	$("#startDate").val(timeStamp2String(new Date().getTime() - 1 * 60 * 60 * 1000));
	$("#endDate").val(timeStamp2String(new Date().getTime()));
}


function initChart(){
	var startDate = $("#startDate").val();
	var endDate = $("#endDate").val();
	var groupType = $("input[name='groupType']:checked").val();
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
	if (groupType == "4" && (endDateLong - startDateLong)/1000 > maxDataSize) {
		alert("查询时间范围[" + timeStamp2String(startDateLong) + "," + timeStamp2String(endDateLong) + "]不能超过两小时！");
		return;
	}
	
	
	$.ajax({
		type : "GET",
		url : requestUrl + "/nginxSatus/getNginxSatus" + getQueryCondition(),
		contentType : "application/json",
		success : function(data) {
			if (data.MaxMonitorTimes.length > 0) {
				maxDateTime = data.MaxMonitorTimes[data.MaxMonitorTimes.length - 1];
			}
			initActiveConnectionsChart(data);
			initKeepaliveChart(data);
			initIncChart(data);
		},
		failure: function() {
			alert("请求后台获取服务器列表异常");
		}
	});
}

function initActiveConnectionsChart(data) {
	var option = {
            backgroundColor: '#FBFBFB',
            tooltip : {
                trigger: 'axis'
            },
            legend: {
                data:['ActiveConnections']
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
                    data : data.MonitorTimes
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
                    name:'ActiveConnections',
                    type:'line',
                    symbol:'none',
                    smooth: 0.2,
                    color:'#66AEDE',
                    data:data.ActiveConnections
                }
            ]
        };
	if (data.MonitorTimes.length > 50) {
		option.dataZoom.show = true;
		option.dataZoom.start = 98;
		option.dataZoom.end = 100;
	} else {
		option.dataZoom.show = false;
		option.dataZoom.start = 0;
		option.dataZoom.end = 100;
	}
	activeConnectionsChart = echarts.init(document.getElementById("ActiveConnections"));
	activeConnectionsChart.setOption(option);
}

function initKeepaliveChart(data) {
	var option = {
            backgroundColor: '#FBFBFB',
            tooltip : {
                trigger: 'axis'
            },
            legend: {
                data:['Reading','Writing','Waiting']
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
                    data : data.MonitorTimes
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
                    name:'Reading',
                    type:'line',
                    symbol:'none',
                    smooth: 0.2,
                    color:['#66AEDE'],
                    data:data.Reading
                },
                {
                    name:'Writing',
                    type:'line',
                    symbol:'none',
                    smooth: 0.2,
                    color:['#90EC7D'],
                    data:data.Writing
                },
				{
					name:'Waiting',
					type:'line',
					symbol:'none',
					smooth: 0.2,
					color:['#60EC4D'],
					data:data.Waiting
				}
            ]
        };
	if (data.MonitorTimes.length > 50) {
		option.dataZoom.show = true;
		option.dataZoom.start = 98;
		option.dataZoom.end = 100;
	} else {
		option.dataZoom.show = false;
		option.dataZoom.start = 0;
		option.dataZoom.end = 100;
	}
	keepaliveChart = echarts.init(document.getElementById("Keep-Alive-Connections"));
	keepaliveChart.setOption(option);
}

function initIncChart(data) {
	var option = {
		backgroundColor: '#FBFBFB',
		tooltip : {
			trigger: 'axis'
		},
		legend: {
			data:['IncAcceptConnections','IncHandledConnections','IncHandledRequests']
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
				data : data.MonitorTimes
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
				name:'IncAcceptConnections',
				type:'line',
				symbol:'none',
				smooth: 0.2,
				color:['#66AEDE'],
				data:data.IncAcceptConnections
			},
			{
				name:'IncHandledConnections',
				type:'line',
				symbol:'none',
				smooth: 0.2,
				color:['#90EC7D'],
				data:data.IncHandledConnections
			},
			{
				name:'IncHandledRequests',
				type:'line',
				symbol:'none',
				smooth: 0.2,
				color:['#60EC4D'],
				data:data.IncHandledRequests
			}
		]
	};
	if (data.MonitorTimes.length > 50) {
		option.dataZoom.show = true;
		option.dataZoom.start = 98;
		option.dataZoom.end = 100;
	} else {
		option.dataZoom.show = false;
		option.dataZoom.start = 0;
		option.dataZoom.end = 100;
	}
	incChart = echarts.init(document.getElementById("Inc-Requests"));
	incChart.setOption(option);
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
	var serverIp =  $('#serverIp').val();
	var groupType = $("input[name='groupType']:checked").val();
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
	if (groupType != "") {
		if (append) {
			condition += "&";
		}
		condition += "groupType=" + groupType;
	}

	$.ajax({
		type : "GET",
		url : requestUrl + "/nginxSatus/getNginxSatus" + queryCondition,
		contentType : "application/json",
		success : function(data) {
			if (data.MonitorTimes.length <= 0) {
				return;
			}
			if (data.MaxMonitorTimes.length > 0) {
				maxDateTime = data.MaxMonitorTimes[data.MaxMonitorTimes.length - 1];
			}
			var option = activeConnectionsChart.getOption();
			var monitorTime = option.xAxis[0].data;
			for (var i in data.MonitorTimes) {
				monitorTime.push(data.MonitorTimes[i]);
				if (monitorTime.length > maxDataSize) {
					monitorTime.shift()
				}
			}

			var dataQueue = option.series[0].data;
			for (var i in data.ActiveConnections) {
				dataQueue.push(data.ActiveConnections[i]);
				if (dataQueue.length >= maxDataSize) {
					dataQueue.shift()
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
			activeConnectionsChart.setOption(option, true);
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

function getQueryCondition() {
	var startDate = $("#startDate").val();
	var endDate = $("#endDate").val();
	var serverIp =  $('#serverIp').val();
	var groupType = $("input[name='groupType']:checked").val();
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
	if (groupType != "") {
		if (append) {
			condition += "&";
		}
		condition += "groupType=" + groupType;
	}
	return condition;
}