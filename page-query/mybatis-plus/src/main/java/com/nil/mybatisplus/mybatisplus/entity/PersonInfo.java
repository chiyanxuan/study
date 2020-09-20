package com.nil.mybatisplus.mybatisplus.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@TableName("t_person_info")
@Data
public class PersonInfo implements Serializable {
    /**
     * 
     */
	@TableId("uuid")
    private Long uuid;
 
    /**
     * 真实名称
     */
    private String realName;

    /**
     * 账号
     */
    private String account;

    /**
     * 密码
     */
    private String password;

    /**
     * 手机号码
     */
    private String telephone;

    /**
     * 固定电话
     */
    private String linePhone;

    /**
     * 邮箱地址
     */
    private String email;

    /**
     * 是否删除 1是 0否
     */
    private Byte disabled;

    /**
     * 创建人id
     */
    private Long createdId;

    /**
     * 创建时间
     */
    private Date createdTime;

    /**
     * 最后更新人id
     */
    private Long lastUpdatedId;

    /**
     * 最后更新时间
     */
    private Date lastUpdatedTime;

    private static final long serialVersionUID = 1L;
}