package com.nil.mybatisplus.mybatisplus.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nil.mybatisplus.mybatisplus.bean.QueryDTO;
import com.nil.mybatisplus.mybatisplus.entity.PersonInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonMapper extends BaseMapper<PersonInfo>{

	IPage<PersonInfo> getlist(
			@Param("page") Page<PersonInfo> page,
			@Param("dto") QueryDTO dto);
}
