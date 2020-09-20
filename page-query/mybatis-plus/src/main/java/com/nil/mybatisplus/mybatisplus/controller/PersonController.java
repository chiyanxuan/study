package com.nil.mybatisplus.mybatisplus.controller;

import com.nil.mybatisplus.mybatisplus.bean.PageBean;
import com.nil.mybatisplus.mybatisplus.bean.QueryDTO;
import com.nil.mybatisplus.mybatisplus.entity.PersonInfo;
import com.nil.mybatisplus.mybatisplus.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/person")
public class PersonController {

	@Autowired
	private PersonService personService;

	@PostMapping(value="/list2")
	public PageBean<PersonInfo> listByUnitId2(@RequestBody QueryDTO queryDto){
		return personService.queryByMybatisPlus(queryDto);
	}
}
