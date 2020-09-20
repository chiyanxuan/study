package com.nil.mybatisplus.mybatisplus.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nil.mybatisplus.mybatisplus.bean.PageBean;
import com.nil.mybatisplus.mybatisplus.bean.QueryDTO;
import com.nil.mybatisplus.mybatisplus.dao.PersonMapper;
import com.nil.mybatisplus.mybatisplus.entity.PersonInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PersonService {

	@Autowired
	private PersonMapper personMapper;

	/**
	 * baomidou.mybatisplus
	 * @param dto
	 * @return
	 */
	public PageBean<PersonInfo> queryByMybatisPlus(QueryDTO dto) {

		Page<PersonInfo> page = new Page<>(dto.getPageNo(), dto.getPageSize());
		if ("DESC".equals(dto.getOrder())) {
			page.addOrder(OrderItem.desc(dto.getSortBy()));
		} else {
			page.addOrder(OrderItem.asc(dto.getSortBy()));
		}
		IPage<PersonInfo> result = personMapper.getlist(page, dto);
		PageBean<PersonInfo> pageBean = new PageBean<PersonInfo>();
		pageBean.setPageData(result.getRecords());
		pageBean.setTotal(result.getTotal());
		pageBean.setPageNo((int) result.getCurrent());
		pageBean.setPageSize((int) result.getSize());
		pageBean.setPageTotal((int) result.getPages());
		return pageBean;
	}
}
