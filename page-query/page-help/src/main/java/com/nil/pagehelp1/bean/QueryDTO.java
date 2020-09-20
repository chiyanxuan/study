package com.nil.pagehelp1.bean;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;


@Data
@EqualsAndHashCode(callSuper = true)
public class QueryDTO extends QueryParamDTO {
	private String sortBy;
	private String realName;
	private String account;
	@ApiModelProperty("最后修改开始时间")
	private Date startTime;

	@ApiModelProperty("最后修改结束时间")
	private Date endTime;
}
