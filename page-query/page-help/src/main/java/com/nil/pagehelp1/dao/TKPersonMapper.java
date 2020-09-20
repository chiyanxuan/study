package com.nil.pagehelp1.dao;

import com.nil.pagehelp1.bean.QueryDTO;
import com.nil.pagehelp1.entity.PersonInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface TKPersonMapper extends Mapper<PersonInfo>{

	List<PersonInfo> selectPage(QueryDTO dto);
}
