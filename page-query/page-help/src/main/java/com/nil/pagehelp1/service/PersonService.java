package com.nil.pagehelp1.service;

import com.github.pagehelper.PageInfo;
import com.github.pagehelper.page.PageMethod;
import com.nil.pagehelp1.bean.PageBean;
import com.nil.pagehelp1.bean.PageUtil;
import com.nil.pagehelp1.bean.QueryDTO;
import com.nil.pagehelp1.dao.TKPersonMapper;
import com.nil.pagehelp1.entity.PersonInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Slf4j
@Service
public class PersonService {

	@Autowired
	private TKPersonMapper tkPersonMapper;
	/**
	 * github.pagehelper分页
	 * tkmybatis
	 * @param dto
	 * @return
	 */
	public PageBean<PersonInfo> listByUnitId(QueryDTO dto) {
		String orderBy =
				dto.getSortBy() + " " + dto.getOrder();
		log.debug("orderBy:{}", orderBy);
		// 分页、排序
		PageMethod.startPage(dto.getPageNo(), dto.getPageSize(), orderBy);
		//PageHelper.startPage(pageNum, pageSize);
		List<PersonInfo> personList = tkPersonMapper.selectPage(dto);

		// 构建返回结果
		PageInfo<PersonInfo> pageInfo = new PageInfo<>(personList);
		PageBean<PersonInfo> pageBean = new PageBean<>();
		pageBean.setPageData(personList);
		pageBean.setPageNo(pageInfo.getPageNum());
		pageBean.setPageSize(pageInfo.getPageSize());
		pageBean.setTotal(pageInfo.getTotal());
		pageBean.setPageTotal(pageInfo.getPages());
		return pageBean;
	}

	/**
	 * tkmybatis
	 * @param dto
	 * @return
	 */
	public PageBean<PersonInfo> pageList(QueryDTO dto) {
		int page = dto.getPageNo();
		int size = dto.getPageSize();
		Example personExample = new Example(PersonInfo.class);

		personExample.createCriteria().andEqualTo("realName", dto.getRealName());
		if (StringUtils.isNotBlank(dto.getAccount())) {
			personExample.and().andLike("account", "%" + dto.getAccount() + "%");
		}
		if (dto.getStartTime() != null) {
			personExample.and().andGreaterThan("lastUpdatedTime", dto.getStartTime());
		}
		if (dto.getEndTime() != null) {
			personExample.and().andLessThanOrEqualTo("lastUpdatedTime", dto.getEndTime());
		}

		personExample.setOrderByClause("last_updated_time DESC");
		PageBean<PersonInfo> resp = new PageBean<>();
		List<PersonInfo> persons = tkPersonMapper.selectByExampleAndRowBounds(personExample,
				new RowBounds(PageUtil.getOffset(page, size), PageUtil.getLimit(size)));
		int total = tkPersonMapper.selectCountByExample(personExample);

		resp.setPageData(persons);
		resp.setTotal((long) total);
		resp.setPageNo(page);
		resp.setPageSize(size);
		resp.setPageTotal(PageUtil.getPageTotal(total, size));
		return resp;
	}
}
