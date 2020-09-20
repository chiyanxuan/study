package com.nil.pagehelp1.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
@ApiModel(description = "通用查询信息请求参数 带上的sort的枚举类参数")
public class QueryParamDTO {

    @ApiModelProperty("排序方式 desc/asc,默认为asc")
    private String order = "ASC";

    @ApiModelProperty(value="第几页",example = "0")
    @Min(value = 1, message = "页码不能小于1")
    @Max(value = 1000, message = "页码不能小于1000")
    private int pageNo = 1;

    @Min(value = 0, message = "页大小不能小于0")
    @Max(value = 1000, message = "页大小不能小于1000")
    @ApiModelProperty(value="一页展示多少行",example = "10")
    private int pageSize = 10;
    
}
