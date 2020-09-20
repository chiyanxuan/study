package com.nil.mybatisplus.mybatisplus.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ApiModel(description = "分页响应对象")
public class PageBean<T> implements Serializable{
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /**
     * 当前页码
     */
    @ApiModelProperty(value = "当前页码",example = "0")
    private Integer pageNo;
    /**
     * 每页记录数
     */
    @ApiModelProperty(value = "每页记录数",example = "10")
    private Integer pageSize;
    /**
     * 总页数
     */
    @ApiModelProperty(value = "总页数",example = "0")
    private Integer pageTotal;

    /**
     * 总记录数
     */
    @ApiModelProperty(value = "总记录数",example = "0")
    private Long total;

    /**
     * 列表数据
     */
    @ApiModelProperty(value = "列表数据")
    private List<T> pageData;

}
