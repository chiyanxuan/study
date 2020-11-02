package com.nil.serviceImpl1;

import com.nil.service.AddressService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "AddressServiceImpl.ext", havingValue = "false", matchIfMissing = true)
public class AddressServiceImpl implements AddressService {

    @Override public String getAddress() {
        return "nil serviceImpl1 address";
    }

    @Override
    public String validAddress(String address) {
        //校验address
        return address + "：serviceImpl1 valid---";
    }
}
