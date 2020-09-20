package com.nil.pagehelp1.controller;

import com.nil.pagehelp1.bean.PageBean;
import com.nil.pagehelp1.bean.QueryDTO;
import com.nil.pagehelp1.entity.PersonInfo;
import com.nil.pagehelp1.service.PersonService;
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

	@PostMapping(value="/list")
	public PageBean<PersonInfo> listByUnitId(@RequestBody QueryDTO queryDto){
		return personService.listByUnitId(queryDto);
	}

	@PostMapping(value="/list2")
	public PageBean<PersonInfo> tkmybatisQuery(@RequestBody QueryDTO queryDto){
		return personService.pageList(queryDto);
	}
}
