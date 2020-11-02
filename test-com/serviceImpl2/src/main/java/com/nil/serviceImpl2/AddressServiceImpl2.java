package com.nil.serviceImpl2;

import com.nil.serviceImpl1.AddressServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class AddressServiceImpl2 extends AddressServiceImpl {

    @Override
    public String validAddress(String address) {
        //调用父类的方法
        String validResult = super.validAddress(address);
        //扩展校验address
        return validResult + "：serviceImpl2 valid---";
    }
}
