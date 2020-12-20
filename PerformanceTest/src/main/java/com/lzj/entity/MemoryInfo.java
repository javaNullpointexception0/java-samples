package com.lzj.entity;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MemoryInfo {

	//年轻代容量K
	private Integer newGenerationCapacity;
	//年轻代已使用大小K
	private Integer newGenerationUsed;
	
	//老年代容量K
	private Integer tenuredGenerationCapacity;
	//老年代已使用大小K
	private Integer tenuredGenerationUsed;
	
}
