package com.nil.pagehelp1.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Table(name="t_person_info")
@Data
public class PersonInfo implements Serializable {
    /**
     * 
     */
    @Id
    private Long uuid;
 
    /**
     * 真实名称
     */
    @Column(name = "real_name")
    private String realName;

    /**
     * 账号
     */
    @Column(name = "account")
    private String account;

    /**
     * 密码
     */
    @Column(name = "password")
    private String password;

    /**
     * 手机号码
     */
    @Column(name = "telephone")
    private String telephone;

    /**
     * 固定电话
     */
    @Column(name = "line_phone")
    private String linePhone;

    /**
     * 邮箱地址
     */
    @Column(name = "email")
    private String email;

    /**
     * 是否删除 1是 0否
     */
    @Column(name = "disabled")
    private Byte disabled;

    /**
     * 创建人id
     */
    @Column(name = "created_id")
    private Long createdId;

    /**
     * 创建时间
     */
    @Column(name = "created_time")
    private Date createdTime;

    /**
     * 最后更新人id
     */
    @Column(name = "last_updated_id")
    private Long lastUpdatedId;

    /**
     * 最后更新时间
     */
    @Column(name = "last_updated_time")
    private Date lastUpdatedTime;

    private static final long serialVersionUID = 1L;
}