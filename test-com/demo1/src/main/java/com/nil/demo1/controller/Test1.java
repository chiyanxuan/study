package com.nil.demo1.controller;

import com.nil.service.AddressService;
import com.nil.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ConditionalOnProperty(name = "Test1.ext", havingValue = "false", matchIfMissing = true)
public final class Test1 {

    //成功获取到demo2的配置
    @Value("${app.test:}")
    private String test;

    //demo2没有配置，即便demo1配置了也获取不到
    @Value("${app.test.name:}")
    private String testName;

    @Autowired
    private PersonService personService;

    @Autowired
    private AddressService addressService;

    @GetMapping("/test1")
    public String test1() {
        return "test1--" + test + "testName1--" + testName;
    }

    //根据PersonServiceImpl中的条件， 在demo2中配置PersonServiceImpl.ext=true就不会再装载PersonServiceImpl
    //demo2引入serviceImpl2后既可以使用PersonServiceImpl2的实现
    @GetMapping("/testService1")
    public String testService() {
        return personService.getPerson("testService1");
    }

    // controller extends
    @GetMapping("/testExtend1")
    public String testExtend() {
        return addressService.validAddress("testExtend1");
    }


    @GetMapping("/testOrgMethod1")
    public String testOrgMethod() {
        return addressService.getAddress();
    }
}
