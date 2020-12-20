package com.lzj.remote;

public interface RemoteCommand {

	public int getSyncQueueSize();
	
	public int getAcceptQueueSize();
	
	public void close();
}
